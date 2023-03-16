package com.ignis.igrobotics.client.menu;

import com.ignis.igrobotics.definitions.ModBlocks;
import com.ignis.igrobotics.Reference;
import com.ignis.igrobotics.common.blockentity.AssemblerBlockEntity;
import com.ignis.igrobotics.definitions.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.SlotItemHandler;

import java.awt.*;

public class AssemblerMenu extends AbstractContainerMenu {
    public final AssemblerBlockEntity blockEntity;
    private final Level level;
    public final ContainerData data;

    public static final Point slot_top = new Point(80, 11);
    public static final Point slot_right = new Point(128, 59);
    public static final Point slot_bot = new Point(80, 107);
    public static final Point slot_left = new Point(32, 59);
    public static final Point slot_out = new Point(80, 59);

    public AssemblerMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        this(id, inv, inv.player.level.getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(6));
    }

    public AssemblerMenu(int id, Inventory playerInv, BlockEntity blockEntity, ContainerData data) {
        super(ModMenuTypes.ASSEMBLER_MENU.get(), id);
        this.blockEntity = (AssemblerBlockEntity) blockEntity;
        this.level = playerInv.player.level;
        this.data = data;

        addPlayerInv(playerInv, Reference.GUI_ASSEMBLER_DIMENSIONS);
        addDataSlots(data);

        blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
            //Input-Slots
            this.addSlot(new SlotItemHandler(handler, 0, slot_top.x, slot_top.y));
            this.addSlot(new SlotItemHandler(handler, 1, slot_right.x, slot_right.y));
            this.addSlot(new SlotItemHandler(handler, 2, slot_bot.x, slot_bot.y));
            this.addSlot(new SlotItemHandler(handler, 3, slot_left.x, slot_left.y));

            //Output-Slot
            this.addSlot(new SlotItemHandler(handler, 4, slot_out.x, slot_out.y));
        });
    }

    protected void addPlayerInv(Inventory playerInv, Dimension size) {
        int offsetX = size.width / 2 - (9 * 17 / 2) + 1;
        int offsetY = size.height - 82;
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
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), player, ModBlocks.ASSEMBLER.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int quickMovedSlotIndex) {
        // The quick moved slot stack
        ItemStack quickMovedStack = ItemStack.EMPTY;
        // The quick moved slot
        Slot quickMovedSlot = this.slots.get(quickMovedSlotIndex);

        // If the slot is in the valid range and the slot is not empty
        if (quickMovedSlot != null && quickMovedSlot.hasItem()) {
            // Get the raw stack to move
            ItemStack rawStack = quickMovedSlot.getItem();
            // Set the slot stack to a copy of the raw stack
            quickMovedStack = rawStack.copy();

            // If the quick move was performed on the data inventory result slot
            if (quickMovedSlotIndex == 0) {
                // Try to move the result slot into the player inventory/hotbar
                if (!this.moveItemStackTo(rawStack, 5, 41, true)) {
                    // If cannot move, no longer quick move
                    return ItemStack.EMPTY;
                }
            }
            // Else if the quick move was performed on the player inventory or hotbar slot
            else if (quickMovedSlotIndex >= 5 && quickMovedSlotIndex < 41) {
                // Try to move the inventory/hotbar slot into the data inventory input slots
                if (!this.moveItemStackTo(rawStack, 1, 5, false)) {
                    // If cannot move and in player inventory slot, try to move to hotbar
                    if (quickMovedSlotIndex < 32) {
                        if (!this.moveItemStackTo(rawStack, 32, 41, false)) {
                            // If cannot move, no longer quick move
                            return ItemStack.EMPTY;
                        }
                    }
                    // Else try to move hotbar into player inventory slot
                    else if (!this.moveItemStackTo(rawStack, 5, 32, false)) {
                        // If cannot move, no longer quick move
                        return ItemStack.EMPTY;
                    }
                }
            }
            // Else if the quick move was performed on the data inventory input slots, try to move to player inventory/hotbar
            else if (!this.moveItemStackTo(rawStack, 5, 41, false)) {
                // If cannot move, no longer quick move
                return ItemStack.EMPTY;
            }

            if (rawStack.isEmpty()) {
                // If the raw stack has completely moved out of the slot, set the slot to the empty stack
                quickMovedSlot.set(ItemStack.EMPTY);
            } else {
                // Otherwise, notify the slot that that the stack count has changed
                quickMovedSlot.setChanged();
            }
            if (rawStack.getCount() == quickMovedStack.getCount()) {
                // If the raw stack was not able to be moved to another slot, no longer quick move
                return ItemStack.EMPTY;
            }
            // Execute logic on what to do post move with the remaining stack
            quickMovedSlot.onTake(player, rawStack);
        }

        return quickMovedStack; // Return the slot stack
    }
}
