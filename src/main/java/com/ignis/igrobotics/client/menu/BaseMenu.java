package com.ignis.igrobotics.client.menu;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

public abstract class BaseMenu extends AbstractContainerMenu {

    protected BaseMenu(@Nullable MenuType<?> pMenuType, int pContainerId) {
        super(pMenuType, pContainerId);
    }

    protected void addPlayerInv(Inventory playerInv, Dimension size) {
        int offsetX = size.width / 2 - (9 * 18 / 2) + 1;
        int offsetY = size.height - 82;
        addPlayerInv(playerInv, offsetX, offsetY);
    }

    protected void addPlayerInv(Inventory playerInv, int offsetX, int offsetY) {
        //Inventory
        for(int x = 0; x < 9; x++) {
            for(int y = 0; y < 3; y++) {
                this.addSlot(new Slot(playerInv, x + y * 9 + 9, x * 18 + offsetX, y * 18 + offsetY));
            }
        }

        //Hotbar
        for(int i = 0; i < 9; i++) {
            this.addSlot(new Slot(playerInv, i, i * 18 + offsetX, 58 + offsetY));
        }
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int quickMovedSlotIndex) {
        Slot quickMovedSlot = this.slots.get(quickMovedSlotIndex);

        if(!quickMovedSlot.hasItem()) return ItemStack.EMPTY;

        ItemStack rawStack = quickMovedSlot.getItem(); //Stack inside the slot
        ItemStack quickMovedStack = rawStack.copy(); //Stack to move

        // CASE 1: Clicked slot was in the opened container
        if (quickMovedSlotIndex >= 36) {
            if (!this.moveItemStackTo(rawStack, 0, 36, false)) {
                return ItemStack.EMPTY;
            }
        }
        // CASE 2: Clicked slot was in the players inventory
        else {
            // Try INVENTORY/HOTBAR -> CONTAINER
            if (!this.moveItemStackTo(rawStack, 36, slots.size(), false)) {
                // Try INVENTORY -> HOTBAR
                if (quickMovedSlotIndex < 27) {
                    if (!this.moveItemStackTo(rawStack, 27, 36, false)) {
                        return ItemStack.EMPTY;
                    }
                }
                // Try HOTBAR -> INVENTORY
                else if (!this.moveItemStackTo(rawStack, 0, 27, false)) {
                    return ItemStack.EMPTY;
                }
            }
        }

        if (rawStack.isEmpty()) {
            // If the raw stack has completely moved out of the slot, set the slot to the empty stack
            quickMovedSlot.set(ItemStack.EMPTY);
        } else {
            // Otherwise, notify the slot that the stack count has changed
            quickMovedSlot.setChanged();
        }
        if (rawStack.getCount() == quickMovedStack.getCount()) {
            // If the raw stack was not movable to another slot, no longer quick move
            return ItemStack.EMPTY;
        }
        // Execute logic on what to do post move with the remaining stack
        quickMovedSlot.onTake(player, rawStack);

        return quickMovedStack; // Return the slot stack
    }
}
