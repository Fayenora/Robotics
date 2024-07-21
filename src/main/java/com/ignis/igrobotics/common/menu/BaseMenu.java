package com.ignis.igrobotics.common.menu;

import com.ignis.igrobotics.Robotics;
import com.ignis.igrobotics.network.NetworkHandler;
import com.ignis.igrobotics.network.container.*;
import com.ignis.igrobotics.network.messages.client.PacketContainerData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseMenu extends AbstractContainerMenu {

    protected Inventory playerInv;
    private final List<ISyncableData> trackedData = new ArrayList<>();

    protected BaseMenu(@Nullable MenuType<?> pMenuType, Inventory playerInv, int pContainerId) {
        super(pMenuType, pContainerId);
        this.playerInv = playerInv;
    }

    ///////////////////////////////
    // Syncing. Credit to Mekanism
    ///////////////////////////////

    public void handleWindowProperty(short property, int value) {
        ISyncableData data = getTrackedData(property);
        if(data instanceof SyncableInt syncable) {
            syncable.set(value);
        }
    }

    public void handleWindowProperty(short property, byte value) {
        ISyncableData data = getTrackedData(property);
        if(data instanceof SyncableByte syncable) {
            syncable.set(value);
        }
    }

    public void handleWindowProperty(short property, short value) {
        ISyncableData data = getTrackedData(property);
        if(data instanceof SyncableShort syncable) {
            syncable.set(value);
        }
    }

    public void track(ISyncableData data) {
        trackedData.add(data);
    }

    @Nullable
    private ISyncableData getTrackedData(short property) {
        if (property < 0 || property >= trackedData.size()) {
            Robotics.LOGGER.warn("Received out of bounds window property {} for container. There are currently {} tracked properties.", property, trackedData.size());
        }
        return trackedData.get(property);
    }

    private void sendInitialDataToRemote(List<ISyncableData> syncableData) {
        if (playerInv.player instanceof ServerPlayer player) {
            //Send all contents to the listener when it first gets added
            List<PropertyData> dirtyData = new ArrayList<>();
            for (short i = 0; i < syncableData.size(); i++) {
                ISyncableData data = syncableData.get(i);
                //Query if the data is dirty or not so that we update our last known value to the initial values
                data.isDirty();
                //And then add the property data as if it was dirty regardless of if it was in case the value is the same as the default
                // as the client may not actually know about it
                dirtyData.add(data.getPropertyData(i));
            }
            if (!dirtyData.isEmpty()) {
                NetworkHandler.sendToPlayer(new PacketContainerData((short) containerId, dirtyData), player);
            }
        }
    }

    @NotNull
    @Override
    protected DataSlot addDataSlot(@NotNull DataSlot referenceHolder) {
        //Override vanilla's int tracking so that if for some reason this method gets called for our container
        // it properly adds it to our tracking
        track(SyncableInt.create(referenceHolder::get, referenceHolder::set));
        return referenceHolder;
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        //Note: We don't bother firing data changed listeners as we have no use for them,
        // and if someone wants to attach one to our containers they can explain what use
        // they need it for before we add a bunch of extra logic to handle them
        if (playerInv.player instanceof ServerPlayer player) {
            //Only check tracked data for changes if we actually have any listeners
            List<PropertyData> dirtyData = new ArrayList<>();
            for (short i = 0; i < trackedData.size(); i++) {
                ISyncableData data = trackedData.get(i);
                if (data.isDirty()) {
                    dirtyData.add(data.getPropertyData(i));
                }
            }
            if (!dirtyData.isEmpty()) {
                NetworkHandler.sendToPlayer(new PacketContainerData((short) containerId, dirtyData), player);
            }
        }
    }

    @Override
    public void sendAllDataToRemote() {
        super.sendAllDataToRemote();
        sendInitialDataToRemote(trackedData);
    }

    /////////////////
    // Inventory Utils
    /////////////////

    protected void addPlayerInv(Dimension size) {
        int offsetX = size.width / 2 - (9 * 18 / 2) + 1;
        int offsetY = size.height - 82;
        addPlayerInv(offsetX, offsetY);
    }

    protected void addPlayerInv(int offsetX, int offsetY) {
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
