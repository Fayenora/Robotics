package com.ignis.igrobotics.common;

import com.ignis.igrobotics.Robotics;
import com.ignis.igrobotics.client.menu.RobotCommandMenu;
import com.ignis.igrobotics.client.menu.RobotInfoMenu;
import com.ignis.igrobotics.client.menu.RobotMenu;
import com.ignis.igrobotics.core.access.AccessConfig;
import com.ignis.igrobotics.core.access.EnumPermission;
import com.ignis.igrobotics.core.access.WorldAccessData;
import com.ignis.igrobotics.core.capabilities.ModCapabilities;
import com.ignis.igrobotics.core.capabilities.energy.EnergyStorage;
import com.ignis.igrobotics.core.capabilities.energy.ModifiableEnergyStorage;
import com.ignis.igrobotics.core.capabilities.inventory.RobotInventory;
import com.ignis.igrobotics.core.robot.RobotCommand;
import com.ignis.igrobotics.core.util.ItemStackUtils;
import com.ignis.igrobotics.core.util.Lang;
import com.ignis.igrobotics.definitions.ModAttributes;
import com.ignis.igrobotics.definitions.ModMenuTypes;
import com.ignis.igrobotics.integration.config.RoboticsConfig;
import com.ignis.igrobotics.network.NetworkHandler;
import com.ignis.igrobotics.network.messages.server.PacketSetAccessConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.event.entity.EntityStruckByLightningEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Mod.EventBusSubscriber(modid = Robotics.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RobotBehavior {

    public static final RegistryObject<MenuType<?>>[] ALL_ROBOT_MENUS = new RegistryObject[]{
            ModMenuTypes.ROBOT,
            ModMenuTypes.ROBOT_INFO,
            ModMenuTypes.ROBOT_INVENTORY,
            ModMenuTypes.ROBOT_COMMANDS
    };

    @SubscribeEvent
    public static void onRobotSpawn(MobSpawnEvent event) {
        if(!event.getEntity().getCapability(ModCapabilities.ROBOT).isPresent()) return;
        onRobotCreated(event.getEntity());
    }

    @SubscribeEvent
    public static void onRobotTick(LivingEvent.LivingTickEvent event) {
        if(event.getEntity().level.isClientSide()) return;
        event.getEntity().getCapability(ModCapabilities.ROBOT).ifPresent(robot -> {
                event.getEntity().getCapability(ForgeCapabilities.ENERGY).ifPresent(energy -> {
                        robot.setActivation(energy.getEnergyStored() > 0);

                        if(!robot.isActive()) return;
                        double consumption = event.getEntity().getAttributeValue(ModAttributes.ENERGY_CONSUMPTION);
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

    @SubscribeEvent
    public static void onRobotDeath(LivingDeathEvent event) {
        if(event.getEntity().level.isClientSide()) return;
        Entity entity = event.getEntity();
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

    @SubscribeEvent
    public static void onRobotRightClick(PlayerInteractEvent.EntityInteract event) {
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

    public static void openRobotMenu(Player player, MenuType type, Entity target) {
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
    }

    public static void onRobotCreated(LivingEntity entity) {
        if(entity instanceof Mob mob) {
            mob.setPersistenceRequired();
        }
        entity.getCapability(ForgeCapabilities.ENERGY).ifPresent(energy -> {
                if(!(energy instanceof EnergyStorage storage)) return;
                storage.setEnergy(Integer.MAX_VALUE);
            });
    }

    public static List<MenuType> possibleMenus(Entity entity) {
        return List.of(ModMenuTypes.ROBOT.get(), ModMenuTypes.ROBOT_INFO.get(), ModMenuTypes.ROBOT_COMMANDS.get());
    }

    public static boolean hasAccess(Player player, Entity entity, EnumPermission permission) {
        AtomicBoolean access = new AtomicBoolean(false);
        entity.getCapability(ModCapabilities.ROBOT).ifPresent(robot -> access.set(robot.hasAccess(player, permission)));
        return access.get();
    }

    public static void setAccess(WorldAccessData.EnumAccessScope scope, Entity entity, AccessConfig access) {
        if(entity.level.isClientSide()) {
            NetworkHandler.sendToServer(new PacketSetAccessConfig(scope, entity, access));
        } else entity.getCapability(ModCapabilities.ROBOT).ifPresent(robot -> robot.setAccess(access));
    }

    private static ContainerData constructContainerData(Entity entity) {
        return new ContainerData() {
            @Override
            public int get(int key) {
                switch(key) {
                    case 0: return entity.getCapability(ForgeCapabilities.ENERGY).orElse(ModCapabilities.NO_ENERGY).getEnergyStored();
                    case 1: return entity.getCapability(ForgeCapabilities.ENERGY).orElse(ModCapabilities.NO_ENERGY).getMaxEnergyStored();
                    default: return 0;
                }
            }

            @Override
            public void set(int key, int value) {
                IEnergyStorage storage = entity.getCapability(ForgeCapabilities.ENERGY).orElse(null);
                if(!(storage instanceof ModifiableEnergyStorage energy)) return;
                switch(key) {
                    case 0 -> energy.setEnergy(value);
                    case 1 -> energy.setMaxEnergyStored(value);
                }
            }

            @Override
            public int getCount() {
                return 2;
            }
        };
    }
}
