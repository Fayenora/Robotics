package com.ignis.igrobotics.client.menu;

import com.ignis.igrobotics.definitions.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;

public class RobotInfoMenu extends BaseMenu {
    public final LivingEntity robot;
    public final ContainerData data;

    public RobotInfoMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        this(id, inv.player.level.getEntity(extraData.readInt()), new SimpleContainerData(2));
    }

    public RobotInfoMenu(int id, Entity entity, ContainerData data) {
        super(ModMenuTypes.ROBOT_INFO.get(), id);
        this.robot = (LivingEntity) entity;
        this.data = data;

        addDataSlots(data);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int quickMovedSlotIndex) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return !robot.isDeadOrDying();
    }
}
