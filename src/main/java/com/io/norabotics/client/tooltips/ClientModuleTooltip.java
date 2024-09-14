package com.io.norabotics.client.tooltips;

import com.io.norabotics.Reference;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientModuleTooltip implements ClientTooltipComponent {

    private final NonNullList<ItemStack> modules;

    public ClientModuleTooltip(ModuleTooltip tooltip) {
        this.modules = tooltip.getItems();
    }

    @Override
    public int getHeight() {
        return 19;
    }

    @Override
    public int getWidth(Font pFont) {
        return 16;
    }

    @Override
    public void renderImage(Font pFont, int pX, int pY, GuiGraphics graphics) {
        int i = 0;
        for(ItemStack stack : modules) {
            graphics.blit(Reference.MISC, pX + i, pY, 107, 0, 17, 17);
            graphics.renderItem(stack, pX + 1 + i, pY + 1);
            i += 19;
        }
    }
}
