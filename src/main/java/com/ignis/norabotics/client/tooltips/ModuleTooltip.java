package com.ignis.norabotics.client.tooltips;

import com.ignis.norabotics.common.robot.EnumModuleSlot;
import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

public class ModuleTooltip implements TooltipComponent {

    private final EnumModuleSlot slotType;
    private final NonNullList<ItemStack> items;

    public ModuleTooltip(EnumModuleSlot slotType, NonNullList<ItemStack> items) {
        this.slotType = slotType;
        this.items = items;
    }

    public EnumModuleSlot getSlotType() {
        return slotType;
    }

    public NonNullList<ItemStack> getItems() {
        return items;
    }
}
