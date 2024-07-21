package com.ignis.igrobotics.common.menu.slots;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class CustomSlot extends SlotItemHandler {

    private boolean active = true, mayPickup = true, mayPlace = true;

    public CustomSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
    }

    @Override
    public boolean mayPickup(Player playerIn) {
        return super.mayPickup(playerIn) && mayPickup;
    }

    @Override
    public boolean mayPlace(@NotNull ItemStack stack) {
        return super.mayPlace(stack) && mayPlace;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public CustomSlot setInteractable(boolean interactable) {
        return setPickable(interactable).setPlaceable(interactable);
    }

    public CustomSlot setPickable(boolean pickable) {
        this.mayPickup = pickable;
        return this;
    }

    public CustomSlot setPlaceable(boolean placeable) {
        this.mayPlace = placeable;
        return this;
    }
}
