package com.ignis.igrobotics.common.handlers;

import com.ignis.igrobotics.Reference;
import com.ignis.igrobotics.Robotics;
import com.ignis.igrobotics.common.access.AccessConfig;
import com.ignis.igrobotics.common.access.EnumPermission;
import com.ignis.igrobotics.common.access.WorldAccessData;
import com.ignis.igrobotics.common.capabilities.IRobot;
import com.ignis.igrobotics.common.capabilities.ModCapabilities;
import com.ignis.igrobotics.common.capabilities.impl.EnergyStorage;
import com.ignis.igrobotics.common.capabilities.impl.inventory.RobotInventory;
import com.ignis.igrobotics.common.content.entity.RobotEntity;
import com.ignis.igrobotics.common.content.entity.ai.AbstractRangedAttackGoal;
import com.ignis.igrobotics.common.content.entity.ai.RetrieveGoal;
import com.ignis.igrobotics.common.helpers.RoboticsMenus;
import com.ignis.igrobotics.common.helpers.util.InventoryUtil;
import com.ignis.igrobotics.common.robot.EnumModuleSlot;
import com.ignis.igrobotics.common.robot.EnumRobotMaterial;
import com.ignis.igrobotics.common.robot.EnumRobotPart;
import com.ignis.igrobotics.definitions.ModAttributes;
import com.ignis.igrobotics.definitions.ModMenuTypes;
import com.ignis.igrobotics.definitions.ModSounds;
import com.ignis.igrobotics.integration.cc.ComputerizedBehavior;
import com.ignis.igrobotics.integration.config.RoboticsConfig;
import com.ignis.igrobotics.network.NetworkHandler;
import com.ignis.igrobotics.network.messages.server.PacketSetAccessConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.event.entity.EntityStruckByLightningEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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
        if(entity.level().isClientSide()) return;
        entity.getCapability(ModCapabilities.ROBOT).ifPresent(robot -> {
            entity.getCapability(ForgeCapabilities.ENERGY).ifPresent(energy -> {
                    if(energy.getEnergyStored() <= 0) {
                        robot.setActivation(false);
                    }

                    //Keep the Computer alive
                    if(ModList.get().isLoaded(Reference.CC_MOD_ID)) {
                        entity.getCapability(ModCapabilities.COMPUTERIZED).ifPresent(computer -> {
                            if(computer.hasComputer()) {
                                ComputerizedBehavior.onComputerTick(entity, robot, computer.getComputer());
                            }
                        });
                    }

                    if(!robot.isActive()) return;

                    //Consume/Receive Energy through passive sources
                    double consumption = entity.getAttributeValue(ModAttributes.ENERGY_CONSUMPTION);
                    if(consumption > 0) {
                        float configMultiplier = RoboticsConfig.general.robotBaseConsumption.get() / 100f;
                        energy.extractEnergy((int) (consumption * configMultiplier), false);
                    } else {
                        energy.receiveEnergy((int) -consumption, false);
                    }

                    if(robot.isSwelling()) {
                        robot.swell();
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
        if(event.getEntity().level().isClientSide()) return;
        Entity entity = event.getEntity();
        // Drop Inventory
        if(event.getEntity().level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
            entity.getCapability(ModCapabilities.ROBOT).ifPresent(robot ->
                    entity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(inventory -> {
                        if(inventory instanceof RobotInventory robotInv) {
                            robotInv.dropItems();
                        }
                        for(EnumModuleSlot slot : EnumModuleSlot.values()) {
                            for(ItemStack stack : robot.getModules(slot)) {
                                InventoryUtil.dropItem(entity, stack);
                            }
                        }
                    })
            );
        }
        // Close any open containers
        if(entity instanceof Mob mob && mob.getCapability(ModCapabilities.COMMANDS).isPresent()) {
            for(WrappedGoal goal : mob.goalSelector.getAvailableGoals()) {
                if(goal.getGoal() instanceof RetrieveGoal retrieveGoal) {
                    retrieveGoal.closeContainer();
                }
            }
        }
        // Close the computer
        event.getEntity().getCapability(ModCapabilities.COMPUTERIZED).ifPresent(computer -> {
            if(computer.hasComputer()) {
                computer.getComputer().close();
            }
        });
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

        if(target instanceof Mob mob && mob.getCapability(ModCapabilities.ROBOT).isPresent()) {
            if(equipIfPossible(mob, player, hand, EquipmentSlot.HEAD, stack) ||
                    equipIfPossible(mob, player, hand, EquipmentSlot.CHEST, stack) ||
                    equipIfPossible(mob, player, hand, EquipmentSlot.LEGS, stack) ||
                    equipIfPossible(mob, player, hand, EquipmentSlot.FEET, stack)) {
                return;
            }
        }
        RoboticsMenus.openRobotMenu(player, ModMenuTypes.ROBOT.get(), target);
    }

    @SubscribeEvent
    public static void determineProjectile(LivingGetProjectileEvent event) {
        LivingEntity living = event.getEntity();
        if(!living.getCapability(ModCapabilities.ROBOT).isPresent()) return;
        ItemStack weapon = event.getProjectileWeaponItemStack();
        ItemStack ammunition = AbstractRangedAttackGoal.retrieveAmmunitionFromInventory(living, weapon);
        if(!ammunition.isEmpty() && !event.getProjectileWeaponItemStack().isEmpty()) {
            event.setProjectileItemStack(ammunition);
        }
    }

    private static boolean equipIfPossible(Mob mob, Player player, InteractionHand hand, EquipmentSlot slot, ItemStack stack) {
        if(!stack.getAttributeModifiers(slot).isEmpty()) {
            if(!player.isCreative()) {
                player.setItemInHand(hand, mob.getItemBySlot(slot));
            }
            mob.setItemSlot(slot, stack.copy());
            return true;
        }
        return false;
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
        entity.setHealth(entity.getMaxHealth());
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
        if(entity.level().isClientSide()) {
            NetworkHandler.sendToServer(new PacketSetAccessConfig(scope, entity, access));
        } else entity.getCapability(ModCapabilities.ROBOT).ifPresent(robot -> robot.setAccess(access));
    }

    public static boolean canReach(LivingEntity entity, BlockPos pos) {
        double reach = entity.getAttributes().hasAttribute(ForgeMod.BLOCK_REACH.get()) ? entity.getAttributes().getValue(ForgeMod.BLOCK_REACH.get()) : 4.5;
        return entity.getEyePosition().distanceToSqr(Vec3.atCenterOf(pos)) < reach * reach;
    }

    public static boolean canReach(LivingEntity entity, GlobalPos pos) {
        if(!entity.level().dimension().equals(pos.dimension())) return false;
        return canReach(entity, pos.pos());
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

    public static int swingSpeed(LivingEntity entity) {
        if (MobEffectUtil.hasDigSpeed(entity)) {
            return 6 - (1 + MobEffectUtil.getDigSpeedAmplification(entity));
        } else {
            return entity.hasEffect(MobEffects.DIG_SLOWDOWN) ? 6 + (1 + entity.getEffect(MobEffects.DIG_SLOWDOWN).getAmplifier()) * 2 : 6;
        }
    }
}
