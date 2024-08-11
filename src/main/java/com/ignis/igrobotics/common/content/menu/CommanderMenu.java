package com.ignis.igrobotics.common.content.menu;

import com.ignis.igrobotics.common.robot.RobotView;
import com.ignis.igrobotics.definitions.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class CommanderMenu extends AbstractContainerMenu {

    Collection<RobotView> robots;

    public CommanderMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        this(id, RobotView.readViews(extraData));
    }

    public CommanderMenu(int id, Collection<RobotView> robots) {
        super(ModMenuTypes.COMMANDER.get(), id);
        this.robots = robots;
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player pPlayer, int pIndex) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(@NotNull Player pPlayer) {
        return true;
    }

    public Collection<RobotView> getRobots() {
        return robots;
    }
}
