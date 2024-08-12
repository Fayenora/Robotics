package com.ignis.norabotics.integration.cc.vanilla;

import com.ignis.norabotics.definitions.ModMenuTypes;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class VProgrammingMenu extends AbstractContainerMenu implements IProgrammingMenu {
    public VProgrammingMenu(int i, Inventory inventory, FriendlyByteBuf friendlyByteBuf) {
        super(ModMenuTypes.COMPUTER.get(), i);
    }

    @Override
    public ItemStack quickMoveStack(Player p_38941_, int p_38942_) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player p_38874_) {
        return false;
    }


}
