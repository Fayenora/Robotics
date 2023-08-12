package com.ignis.igrobotics.common;

import com.ignis.igrobotics.Reference;
import com.ignis.igrobotics.Robotics;
import com.ignis.igrobotics.client.menu.RobotCommandMenu;
import com.ignis.igrobotics.client.menu.RobotInfoMenu;
import com.ignis.igrobotics.client.menu.RobotMenu;
import com.ignis.igrobotics.common.entity.RobotEntity;
import com.ignis.igrobotics.common.entity.ai.RetrieveGoal;
import com.ignis.igrobotics.core.access.AccessConfig;
import com.ignis.igrobotics.core.access.EnumPermission;
import com.ignis.igrobotics.core.access.WorldAccessData;
import com.ignis.igrobotics.core.capabilities.ModCapabilities;
import com.ignis.igrobotics.core.capabilities.energy.EnergyStorage;
import com.ignis.igrobotics.core.capabilities.energy.ModifiableEnergyStorage;
import com.ignis.igrobotics.core.capabilities.inventory.RobotInventory;
import com.ignis.igrobotics.core.capabilities.robot.IRobot;
import com.ignis.igrobotics.core.robot.EnumRobotMaterial;
import com.ignis.igrobotics.core.robot.EnumRobotPart;
import com.ignis.igrobotics.core.robot.RobotCommand;
import com.ignis.igrobotics.core.util.ItemStackUtils;
import com.ignis.igrobotics.core.util.Lang;
import com.ignis.igrobotics.definitions.ModAttributes;
import com.ignis.igrobotics.definitions.ModMenuTypes;
import com.ignis.igrobotics.definitions.ModSounds;
import com.ignis.igrobotics.integration.cc.ComputerizedBehavior;
import com.ignis.igrobotics.integration.cc.ProgrammingMenu;
import com.ignis.igrobotics.integration.config.RoboticsConfig;
import com.ignis.igrobotics.network.NetworkHandler;
import com.ignis.igrobotics.network.messages.server.PacketSetAccessConfig;
import dan200.computercraft.shared.network.container.ComputerContainerData;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.event.entity.EntityStruckByLightningEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Mod.EventBusSubscriber(modid = Robotics.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RobotBehavior {

    public static final RegistryObject<MenuType<?>>[] ALL_ROBOT_MENUS = new RegistryObject[]{
            ModMenuTypes.ROBOT,
            ModMenuTypes.ROBOT_INFO,
            ModMenuTypes.ROBOT_INVENTORY,
            ModMenuTypes.ROBOT_COMMANDS,
            ModMenuTypes.COMPUTER
    };

    @SubscribeEvent
    public static void onRobotSpawn(MobSpawnEvent event) {
        if(!event.getEntity().getCapability(ModCapabilities.ROBOT).isPresent()) return;
        onRobotCreated(event.getEntity());
    }

    @SubscribeEvent
    public static void onRobotTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if(entity.level.isClientSide()) return;
        entity.getCapability(ModCapabilities.ROBOT).ifPresent(robot -> {
            entity.getCapability(ForgeCapabilities.ENERGY).ifPresent(energy -> {
                    if(energy.getEnergyStored() <= 0) {
                        robot.setActivation(false);
                    }

                    //Keep the Computer alive
                    entity.getCapability(ModCapabilities.COMPUTERIZED).ifPresent(computer -> {
                        if(computer.hasComputer()) {
                            ComputerizedBehavior.onComputerTick(entity, robot, computer.getComputer());
                        }
                    });

                    if(!robot.isActive()) return;

                    //Consume/Receive Energy through passive sources
                    double consumption = entity.getAttributeValue(ModAttributes.ENERGY_CONSUMPTION);
                    if(consumption > 0) {
                        float configMultiplier = RoboticsConfig.general.robotBaseConsumption.get() / 100f;
                        energy.extractEnergy((int) (consumption * configMultiplier), false);
                    } else {
                        energy.receiveEnergy((int) -consumption, false);
                    }
                });
            });
    }

    @SubscribeEvent
    public static void onRobotStruckByLightning(EntityStruckByLightningEvent event) {
        if(!(event.getEntity() instanceof LivingEntity living)) return;
        event.getEntity().getCapability(ModCapabilities.ROBOT).ifPresent(robot -> {
            living.getCapability(ForgeCapabilities.ENERGY).ifPresent(energy -> {
                energy.receiveEnergy(2000000, false);
                living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 300, 1));
            });
        });
    }

    /**
     * Damage Armor for entities that are robotic but don't do it themselves
     * NOTE: Code taken from {@link net.minecraft.world.entity.player.Inventory#hurtArmor(DamageSource, float, int[])}
     * @param event The event
     */
    @SubscribeEvent
    public static void afterRobotDamaged(LivingDamageEvent event) {
        if(!event.getEntity().getCapability(ModCapabilities.ROBOT).isPresent()) return;
        if(event.getSource().is(DamageTypeTags.BYPASSES_ARMOR)) return;
        if(event.getAmount() <= 0) return;
        if(event.getEntity() instanceof Player) return; //Players armor already gets damaged by default
        float amount = Math.max(1, event.getAmount());
        event.getEntity().getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(inventory -> {
            for(int i : new int[] {2, 3, 4, 5}) {
                ItemStack stack = inventory.getStackInSlot(i);
                if ((!event.getSource().is(DamageTypeTags.IS_FIRE) || !stack.getItem().isFireResistant()) && stack.getItem() instanceof ArmorItem) {
                    stack.hurtAndBreak((int) amount, event.getEntity(), (p_35997_) -> {
                        p_35997_.broadcastBreakEvent(EquipmentSlot.byTypeAndIndex(EquipmentSlot.Type.ARMOR, i));
                    });
                }
            }
        });
    }

    @SubscribeEvent
    public static void onRobotDeath(LivingDeathEvent event) {
        if(event.getEntity().level.isClientSide()) return;
        Entity entity = event.getEntity();
        if(event.getEntity().level.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) {
            entity.getCapability(ModCapabilities.ROBOT).ifPresent(robot ->
                    entity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(inventory -> {
                        if(inventory instanceof RobotInventory robotInv) {
                            robotInv.dropItems();
                        }
                        for(ItemStack stack : robot.getModules()) {
                            ItemStackUtils.dropItem(entity, stack);
                        }
                    })
            );
        }
        if(entity instanceof Mob mob && mob.getCapability(ModCapabilities.COMMANDS).isPresent()) {
            for(WrappedGoal goal : mob.goalSelector.getAvailableGoals()) {
                if(goal.getGoal() instanceof RetrieveGoal retrieveGoal) {
                    retrieveGoal.closeContainer();
                }
            }
        }
    }

    @SubscribeEvent
    public static void onRobotRightClick(PlayerInteractEvent.EntityInteract event) {
        if(event.getSide().isClient()) return;
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
        Player player = event.getEntity();
        Entity target = event.getTarget();
        ItemStack stack = event.getItemStack();
        InteractionHand hand = event.getHand();
        if(event.getTarget().interact(player, hand).consumesAction()) return;
        if(target instanceof LivingEntity living && stack.getItem().interactLivingEntity(stack, player, living, hand).consumesAction()) return;
        openRobotMenu(player, ModMenuTypes.ROBOT.get(), target);
    }

    public static void openRobotMenu(Player player, MenuType<?> type, Entity target) {
        if(!(player instanceof ServerPlayer serverPlayer)) return;
        if(!target.getCapability(ModCapabilities.ROBOT).isPresent()) return;
        if(!hasAccess(player, target, EnumPermission.VIEW)) return;
        if(type == ModMenuTypes.ROBOT.get()) {
            NetworkHooks.openScreen(serverPlayer,
                    new SimpleMenuProvider((id, playerInv, f3) -> new RobotMenu(id, playerInv, target, constructContainerData(target)), Lang.localise("container.robot")),
                    buf -> buf.writeInt(target.getId()));
        }
        if(type == ModMenuTypes.ROBOT_INFO.get()) {
            if(!(target instanceof LivingEntity living)) return;
            target.getCapability(ModCapabilities.ROBOT).ifPresent(robot -> {
                NetworkHooks.openScreen(serverPlayer,
                    new SimpleMenuProvider((id, f2, f3) -> new RobotInfoMenu(id, target, constructContainerData(target)), Lang.localise("container.robot_info")),
                    buf -> {
                        buf.writeInt(target.getId());
                        robot.getAccess().write(buf);
                        for(Map.Entry<ResourceKey<Attribute>, Attribute> entry : ForgeRegistries.ATTRIBUTES.getEntries()) {
                            if(living.getAttributes().hasAttribute(entry.getValue())) {
                                buf.writeResourceKey(entry.getKey());
                                buf.writeFloat((float) living.getAttributes().getValue(entry.getValue()));
                            }
                        }
                    });
            });
        }
        if(type == ModMenuTypes.ROBOT_COMMANDS.get()) {
            if(!hasAccess(player, target, EnumPermission.COMMANDS)) return;
            target.getCapability(ModCapabilities.COMMANDS).ifPresent(robot -> {
                NetworkHooks.openScreen(serverPlayer,
                        new SimpleMenuProvider((id, f2, f3) -> new RobotCommandMenu(id, target), Lang.localise("container.robot_commands")),
                        buf -> {
                            buf.writeInt(target.getId());
                            CompoundTag tag = new CompoundTag();
                            RobotCommand.writeToNBT(tag, robot.getCommands()); //TODO: Optimize
                            buf.writeNbt(tag);
                        });
            });
        }
        if(type == ModMenuTypes.COMPUTER.get()) {
            if(!hasAccess(player, target, EnumPermission.COMMANDS)) return;
            target.getCapability(ModCapabilities.COMPUTERIZED).ifPresent(computer -> {
                NetworkHooks.openScreen(serverPlayer,
                        new SimpleMenuProvider(
                                (id, playerInv, f3) -> new ProgrammingMenu(id, playerInv, target, p -> hasAccess(p, target, EnumPermission.COMMANDS), computer.getComputer()),
                                Lang.localise("container.computer")),
                        buf -> {
                            new ComputerContainerData(computer.getComputer(), Items.APPLE.getDefaultInstance()).toBytes(buf);
                            buf.writeInt(target.getId());
                        });
            });
        }
    }

    public static void onRobotCreated(LivingEntity entity) {
        if(entity instanceof Mob mob) {
            mob.setPersistenceRequired();
            mob.setCanPickUpLoot(true);
            mob.setLeftHanded(false);
            for(EquipmentSlot slot : EquipmentSlot.values()) {
                mob.setDropChance(slot, 0); //We do this manually to not randomly damage anything!
            }
        }
        //If the robot has no body parts, initialize with iron
        entity.getCapability(ModCapabilities.PARTS).ifPresent(parts -> {
            if(!parts.hasAnyBodyPart()) {
                for(EnumRobotPart part : EnumRobotPart.values()) {
                    parts.setBodyPart(part, EnumRobotMaterial.IRON);
                }
            }
        });
        entity.getCapability(ForgeCapabilities.ENERGY).ifPresent(energy -> {
                if(!(energy instanceof EnergyStorage storage)) return;
                storage.setEnergy(Integer.MAX_VALUE);
            });
        entity.getCapability(ModCapabilities.ROBOT).ifPresent(robot -> robot.setActivation(true));
    }

    public static List<MenuType<?>> possibleMenus(Entity entity) {
        ArrayList<MenuType<?>> menus = new ArrayList<>();
        menus.add(ModMenuTypes.ROBOT.get());
        menus.add(ModMenuTypes.ROBOT_INFO.get());
        menus.add(ModMenuTypes.ROBOT_COMMANDS.get());
        if(ModList.get().isLoaded(Reference.CC_MOD_ID)) {
            menus.add(ModMenuTypes.COMPUTER.get());
        }
        return menus;
    }

    public static boolean hasAccess(Player player, Entity entity, EnumPermission permission) {
        return hasAccess(player.getUUID(), entity, permission);
    }

    public static boolean hasAccess(UUID player, Entity entity, EnumPermission permission) {
        AtomicBoolean access = new AtomicBoolean(false);
        entity.getCapability(ModCapabilities.ROBOT).ifPresent(robot -> access.set(robot.hasAccess(player, permission)));
        return access.get();
    }

    public static void setAccess(WorldAccessData.EnumAccessScope scope, Entity entity, AccessConfig access) {
        if(entity.level.isClientSide()) {
            NetworkHandler.sendToServer(new PacketSetAccessConfig(scope, entity, access));
        } else entity.getCapability(ModCapabilities.ROBOT).ifPresent(robot -> robot.setAccess(access));
    }

    public static boolean canReach(LivingEntity entity, BlockPos pos) {
        double reach = entity.getAttributes().hasAttribute(ForgeMod.BLOCK_REACH.get()) ? entity.getAttributes().getValue(ForgeMod.BLOCK_REACH.get()) : 4.5;
        return entity.getEyePosition().distanceToSqr(Vec3.atCenterOf(pos)) < reach * reach;
    }

    public static void playAggressionSound(LivingEntity entity) {
        Optional<IRobot> robot = entity.getCapability(ModCapabilities.ROBOT).resolve();
        if(robot.isPresent() && !robot.get().isActive()) return;
        if(entity instanceof RobotEntity) {
            entity.playSound(ModSounds.ROBOT_KILL_COMMAND.get());
        } else if(entity instanceof Mob mob) {
            mob.playAmbientSound();
        }
    }

    /**
     * Speed modifier for any entity attempting to break a block
     * NOTE: These calculations are copied from {@link Player#getDigSpeed(BlockState, BlockPos)}
     * @param entity the entity attempting to break a block
     * @param selected the item stack the entity is using
     * @param state the blocks state
     * @return the multiplier to the destroy speed
     */
    public static float destroySpeed(LivingEntity entity, ItemStack selected, BlockState state) {
        float f = selected.getDestroySpeed(state);
        if (f > 1.0F) {
            int i = EnchantmentHelper.getBlockEfficiency(entity);
            if (i > 0 && !selected.isEmpty()) {
                f += (float)(i * i + 1);
            }
        }

        if (MobEffectUtil.hasDigSpeed(entity)) {
            f += (float)(MobEffectUtil.getDigSpeedAmplification(entity) + 1) * 0.2F;
        }

        if (entity.hasEffect(MobEffects.DIG_SLOWDOWN)) {
            float f1 = switch (entity.getEffect(MobEffects.DIG_SLOWDOWN).getAmplifier()) {
                case 0 -> 0.3F;
                case 1 -> 0.09F;
                case 2 -> 0.0027F;
                default -> 8.1E-4F;
            };

            f *= f1;
        }
        //TODO: Post to event with attached fake player
        //f = net.minecraftforge.event.ForgeEventFactory.getBreakSpeed(entity, state, f, pos);
        return f;
    }

    private static ContainerData constructContainerData(Entity entity) {
        return new ContainerData() {
            @Override
            public int get(int key) {
                return switch (key) {
                    case 0 -> entity.getCapability(ForgeCapabilities.ENERGY).orElse(ModCapabilities.NO_ENERGY).getEnergyStored();
                    case 1 -> entity.getCapability(ForgeCapabilities.ENERGY).orElse(ModCapabilities.NO_ENERGY).getMaxEnergyStored();
                    default -> 0;
                };
            }

            @Override
            public void set(int key, int value) {
                entity.getCapability(ForgeCapabilities.ENERGY).ifPresent(storage -> {
                    if(!(storage instanceof ModifiableEnergyStorage energy)) return;
                    switch(key) {
                        case 0 -> energy.setEnergy(value);
                        case 1 -> energy.setMaxEnergyStored(value);
                    }
                });
            }

            @Override
            public int getCount() {
                return 2;
            }
        };
    }
}
