package com.ignis.igrobotics.common.content.menu;

import com.ignis.igrobotics.common.capabilities.ModCapabilities;
import com.ignis.igrobotics.common.robot.RobotCommand;
import com.ignis.igrobotics.definitions.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class RobotCommandMenu extends AbstractContainerMenu {

    public final LivingEntity robot;

    public RobotCommandMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        this(id, inv.player.level().getEntity(extraData.readInt()));
        robot.getCapability(ModCapabilities.COMMANDS).ifPresent(commands -> {
            commands.setCommands(RobotCommand.readFromNBT(extraData.readNbt()));
        });
    }

    public RobotCommandMenu(int id, Entity entity) {
        super(ModMenuTypes.ROBOT_COMMANDS.get(), id);
        this.robot = (LivingEntity) entity;
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player pPlayer, int pIndex) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(@NotNull Player pPlayer) {
        return !robot.isDeadOrDying();
    }
}
