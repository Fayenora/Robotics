package com.ignis.igrobotics.client.menu;

import com.ignis.igrobotics.definitions.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public class RobotCommandMenu extends AbstractContainerMenu {

    public final LivingEntity robot;

    public RobotCommandMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        this(id, inv.player.level.getEntity(extraData.readInt()));
    }

    public RobotCommandMenu(int id, Entity entity) {
        super(ModMenuTypes.ROBOT_COMMANDS.get(), id);
        this.robot = (LivingEntity) entity;
    }

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return !robot.isDeadOrDying();
    }
}
