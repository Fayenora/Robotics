package com.ignis.igrobotics.core.util;

import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.LivingEntity;

public class RenderUtil {

    public static void drawEntityOnScreen(int x, int y, int mouseX, int mouseY, LivingEntity entity) {
        drawEntityOnScreen(x, y, mouseX, mouseY, 30, false, entity);
    }

    public static void drawEntityOnScreen(int x, int y, int mouseX, int mouseY, int scale, boolean ignoreMouse, LivingEntity entity) {
        int intmouseX = x - mouseX + (int)(5/6F * scale);
        int intmouseY = y - mouseY + (int)(17/30F * scale);
        if(ignoreMouse) {
            intmouseX = 0;
            intmouseY = 0;
        }

        drawEntityOnScreen(x + (int)(13/15F * scale), y + (int)(34/15F * scale), scale, intmouseX, intmouseY, entity);
    }

    public static void drawEntityOnScreen(int posX, int posY, int scale, float mouseX, float mouseY, LivingEntity ent) {
        drawEntityOnScreen(posX, posY, scale, mouseX, mouseY, ent, false);
    }

    public static void drawEntityOnScreen(int posX, int posY, int scale, float mouseX, float mouseY, LivingEntity ent, boolean renderNameTag) {
        boolean f1 = ent.shouldShowName();
        ent.setCustomNameVisible(renderNameTag);
        InventoryScreen.renderEntityInInventory(posX, posY, scale, mouseX, mouseY, ent);
        ent.setCustomNameVisible(f1);
    }
}
