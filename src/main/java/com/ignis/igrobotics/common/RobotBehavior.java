package com.ignis.igrobotics.common;

import com.ignis.igrobotics.Robotics;
import com.ignis.igrobotics.client.menu.RobotCommandMenu;
import com.ignis.igrobotics.client.menu.RobotInfoMenu;
import com.ignis.igrobotics.client.menu.RobotMenu;
import com.ignis.igrobotics.core.capabilities.ModCapabilities;
import com.ignis.igrobotics.core.capabilities.energy.EnergyStorage;
import com.ignis.igrobotics.core.capabilities.energy.ModifiableEnergyStorage;
import com.ignis.igrobotics.core.robot.RobotCommand;
import com.ignis.igrobotics.core.util.Lang;
import com.ignis.igrobotics.definitions.ModMenuTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

@Mod.EventBusSubscriber(modid = Robotics.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RobotBehavior {

    public static final EnergyStorage NO_ENERGY = new EnergyStorage(0);
    public static final RegistryObject<MenuType<?>>[] ALL_ROBOT_MENUS = new RegistryObject[]{
            ModMenuTypes.ROBOT,
            ModMenuTypes.ROBOT_INFO,
            ModMenuTypes.ROBOT_INVENTORY,
            ModMenuTypes.ROBOT_COMMANDS
    };

    @SubscribeEvent
    public static void onRobotRightClick(PlayerInteractEvent.EntityInteractSpecific event) {
        openRobotMenu(event.getEntity(), ModMenuTypes.ROBOT.get(), event.getTarget());
    }

    public static void openRobotMenu(Player player, MenuType type, Entity target) {
        if(!(player instanceof ServerPlayer serverPlayer)) return;
        if(!target.getCapability(ModCapabilities.ROBOT).isPresent()) return;
        if(type == ModMenuTypes.ROBOT.get()) {
            NetworkHooks.openScreen(serverPlayer,
                    new SimpleMenuProvider((id, playerInv, f3) -> new RobotMenu(id, playerInv, target, constructContainerData(target)), Lang.localise("container.robot")),
                    buf -> buf.writeInt(target.getId()));
        }
        if(type == ModMenuTypes.ROBOT_INFO.get()) {
            NetworkHooks.openScreen(serverPlayer,
                    new SimpleMenuProvider((id, f2, f3) -> new RobotInfoMenu(id, target, constructContainerData(target)), Lang.localise("container.robot_info")),
                    buf -> buf.writeInt(target.getId()));
        }
        if(type == ModMenuTypes.ROBOT_COMMANDS.get()) {
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

    public static List<MenuType> possibleMenus(Entity entity) {
        return List.of(ModMenuTypes.ROBOT.get(), ModMenuTypes.ROBOT_INFO.get(), ModMenuTypes.ROBOT_COMMANDS.get());
    }

    private static ContainerData constructContainerData(Entity entity) {
        return new ContainerData() {
            @Override
            public int get(int key) {
                switch(key) {
                    case 0: return entity.getCapability(ForgeCapabilities.ENERGY).orElse(NO_ENERGY).getEnergyStored();
                    case 1: return entity.getCapability(ForgeCapabilities.ENERGY).orElse(NO_ENERGY).getMaxEnergyStored();
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
