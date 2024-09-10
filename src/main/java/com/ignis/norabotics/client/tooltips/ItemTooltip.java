package com.ignis.norabotics.client.tooltips;

import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

public class ItemTooltip implements TooltipComponent {

    private ItemStack stack;

    public ItemTooltip(ItemStack stack) {
        this.stack = stack;
    }

    public ItemStack getStack() {
        return stack;
    }
}
