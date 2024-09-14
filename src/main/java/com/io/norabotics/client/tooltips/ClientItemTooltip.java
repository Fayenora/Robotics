package com.io.norabotics.client.tooltips;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientItemTooltip implements ClientTooltipComponent {

    private ItemStack stack;

    public ClientItemTooltip(ItemTooltip tooltip) {
        this.stack = tooltip.getStack();
    }

    @Override
    public int getHeight() {
        return 0;
    }

    @Override
    public int getWidth(Font pFont) {
        return 0;
    }

    @Override
    public void renderImage(Font pFont, int pX, int pY, GuiGraphics pGuiGraphics) {
        pGuiGraphics.renderItem(stack, pX - 19, pY - 16);
    }
}
