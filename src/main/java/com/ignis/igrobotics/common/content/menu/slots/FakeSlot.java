package com.ignis.igrobotics.common.content.menu.slots;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class FakeSlot extends Slot {
    public FakeSlot(int pSlot) {
        super(new SimpleContainer(0), pSlot, 0, 0);
    }

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public boolean mayPlace(ItemStack pStack) {
        return false;
    }

    @Override
    public boolean mayPickup(Player pPlayer) {
        return false;
    }
}
