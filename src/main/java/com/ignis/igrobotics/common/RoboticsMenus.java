package com.ignis.igrobotics.common;

import com.ignis.igrobotics.Reference;
import com.ignis.igrobotics.client.menu.*;
import com.ignis.igrobotics.common.blockentity.FactoryBlockEntity;
import com.ignis.igrobotics.common.handlers.RobotBehavior;
import com.ignis.igrobotics.core.access.EnumPermission;
import com.ignis.igrobotics.core.capabilities.ModCapabilities;
import com.ignis.igrobotics.core.capabilities.energy.ModifiableEnergyStorage;
import com.ignis.igrobotics.core.robot.RobotCommand;
import com.ignis.igrobotics.core.util.Lang;
import com.ignis.igrobotics.definitions.ModMenuTypes;
import com.ignis.igrobotics.integration.cc.vanilla.ScreenInvokator;
import dan200.computercraft.shared.network.container.ComputerContainerData;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;

public class RoboticsMenus {

    public static void openMenu(Player player, MenuType<?> type, Object extraData) {
        if(!(player instanceof ServerPlayer serverPlayer)) return;
        if(extraData instanceof Integer entityId) {
            openRobotMenu(player, type, player.level.getEntity(entityId));
        }
        if(extraData instanceof BlockPos pos) {
            BlockEntity be = player.level.getBlockEntity(pos);
            if(!(be instanceof FactoryBlockEntity factory)) return;
            if(type == ModMenuTypes.FACTORY.get()) {
                NetworkHooks.openScreen(serverPlayer,
                        new SimpleMenuProvider((id, playerInv, f3) -> new FactoryMenu(id, playerInv, factory, factory.getDataAccess()), Lang.localise("container.robot_factory")),
                        buf -> buf.writeBlockPos(pos));
            }
            if(type == ModMenuTypes.FACTORY_MODULES.get()) {
                factory.getEntity().ifPresent(ent -> ent.getCapability(ModCapabilities.ROBOT).ifPresent(robot -> {
                    NetworkHooks.openScreen(serverPlayer,
                            new SimpleMenuProvider((id, playerInv, f3) -> new FactoryModulesMenu(id, playerInv, robot.getModuleSlots(), factory, factory.getDataAccess()), Lang.localise("container.robot_factory.modules")),
                            buf -> {
                                buf.writeMap(robot.getModuleSlots(), FriendlyByteBuf::writeEnum, FriendlyByteBuf::writeInt);
                                buf.writeBlockPos(pos);
                            });
                }));
            }
        }
    }

    public static void openRobotMenu(Player player, MenuType<?> type, Entity target) {
        if(!(player instanceof ServerPlayer serverPlayer)) return;
        if(target == null || !target.getCapability(ModCapabilities.ROBOT).isPresent()) return;
        if(!RobotBehavior.hasAccess(player, target, EnumPermission.VIEW)) return;
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
            if(!RobotBehavior.hasAccess(player, target, EnumPermission.COMMANDS)) return;
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
            if(!RobotBehavior.hasAccess(player, target, EnumPermission.COMMANDS)) return;
            if(!ModList.get().isLoaded(Reference.CC_MOD_ID)) return;
            target.getCapability(ModCapabilities.COMPUTERIZED).ifPresent(computer -> {
                NetworkHooks.openScreen(serverPlayer,
                        new SimpleMenuProvider(
                                ScreenInvokator.invokeProgrammingMenu(target, computer),
                                Lang.localise("container.computer")),
                        buf -> {
                            new ComputerContainerData(computer.getComputer(), Items.APPLE.getDefaultInstance()).toBytes(buf);
                            buf.writeInt(target.getId());
                        });
            });
        }
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
