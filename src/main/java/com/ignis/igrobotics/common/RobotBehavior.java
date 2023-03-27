package com.ignis.igrobotics.common;

import com.ignis.igrobotics.Robotics;
import com.ignis.igrobotics.client.menu.RobotMenu;
import com.ignis.igrobotics.core.capabilities.ModCapabilities;
import com.ignis.igrobotics.core.capabilities.energy.EnergyStorage;
import com.ignis.igrobotics.core.capabilities.energy.ModifiableEnergyStorage;
import com.ignis.igrobotics.core.util.Lang;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.ContainerData;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkHooks;

@Mod.EventBusSubscriber(modid = Robotics.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RobotBehavior {

    public static final EnergyStorage NO_ENERGY = new EnergyStorage(0);

    @SubscribeEvent
    public static void onRobotRightClick(PlayerInteractEvent.EntityInteractSpecific event) {
        if(event.getEntity().level.isClientSide()) return;
        if(!event.getTarget().getCapability(ModCapabilities.ROBOT).isPresent()) return;
        NetworkHooks.openScreen((ServerPlayer) event.getEntity(),
                new SimpleMenuProvider((id, playerInv, player) -> new RobotMenu(id, playerInv, event.getTarget(), constructContainerData(event.getTarget())), Lang.localise("container.robot")),
                buf -> buf.writeInt(event.getTarget().getId()));
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
