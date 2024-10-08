package com.io.norabotics.common.helpers.util;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.joml.Quaternionf;

import java.awt.*;

@OnlyIn(Dist.CLIENT)
public class RenderUtil {

    public static void enableScissor(GuiGraphics graphics, Rectangle rect) {
        graphics.enableScissor(rect.x, rect.y, rect.x + rect.width, rect.y + rect.height);
    }

    public static void disableScissor(GuiGraphics graphics) {
        graphics.disableScissor();
    }

    public static void drawString(GuiGraphics graphics, String text, int x, int y, int color, float scale) {
        graphics.pose().pushPose();
        graphics.pose().scale(scale, scale, 1);
        graphics.drawString(Minecraft.getInstance().font, text, (int) (x / scale), (int) (y / scale), color);
        graphics.pose().popPose();
    }

    public static void drawCenteredString(GuiGraphics graphics, String text, int x, int y, int color, float scale) {
        Font font = Minecraft.getInstance().font;
        graphics.pose().pushPose();
        graphics.pose().scale(scale, scale, 1);
        graphics.drawString(Minecraft.getInstance().font, text, (int) ((x - font.width(text)) / scale), (int) (y /scale), color);
        graphics.pose().popPose();
    }

    public static void drawString(GuiGraphics graphics, String text, int x, int y, int color, float scaleY, int maxWidth) {
        Font font = Minecraft.getInstance().font;
        graphics.pose().pushPose();
        float scale = Math.min(scaleY, maxWidth / (float)font.width(text));
        graphics.pose().scale(scale, scaleY, 1);
        graphics.drawString(Minecraft.getInstance().font, text, (int) (x / scale), (int) (y / scaleY), color);
        graphics.pose().popPose();
    }

    public static void drawCenteredString(GuiGraphics graphics, String text, int x, int y, int color, float scaleY, int maxWidth) {
        Font font = Minecraft.getInstance().font;
        graphics.pose().pushPose();
        float scale = Math.min(scaleY, maxWidth / (float)font.width(text));
        graphics.pose().scale(scale, scaleY, 1);
        graphics.drawString(Minecraft.getInstance().font, text, (int) (x / scale - (float) font.width(text) / 2), (int) (y / scaleY), color);
        graphics.pose().popPose();
    }

    public static void drawString(GuiGraphics poseStack, Component text, int x, int y, int color, float scale) {
        drawString(poseStack, text.getString(), x, y, color, scale);
    }

    public static void drawCenteredString(GuiGraphics poseStack, Component text, int x, int y, int color, float scale) {
        drawCenteredString(poseStack, text.getString(), x, y, color, scale);
    }

    public static void drawString(GuiGraphics poseStack, Component text, int x, int y, int color, float scaleY, int maxWidth) {
        drawString(poseStack, text.getString(), x, y, color, scaleY, maxWidth);
    }

    public static void drawCenteredString(GuiGraphics poseStack, Component text, int x, int y, int color, float scaleY, int maxWidth) {
        drawCenteredString(poseStack, text.getString(), x, y, color, scaleY, maxWidth);
    }

    public static void drawEntityOnScreen(GuiGraphics graphics, int x, int y, int mouseX, int mouseY, LivingEntity entity) {
        drawEntityOnScreen(graphics, x, y, mouseX, mouseY, 30, false, entity);
    }

    public static void drawEntityOnScreen(GuiGraphics graphics, int x, int y, int mouseX, int mouseY, int scale, boolean ignoreMouse, LivingEntity entity) {
        int intmouseX = x - mouseX + (int)(5/6F * scale);
        int intmouseY = y - mouseY + (int)(17/30F * scale);
        if(ignoreMouse) {
            intmouseX = 0;
            intmouseY = 0;
        }

        drawEntityOnScreen(graphics, x + (int)(13/15F * scale), y + (int)(34/15F * scale), scale, intmouseX, intmouseY, entity);
    }

    public static void drawEntityOnScreen(GuiGraphics graphics, int posX, int posY, int scale, float mouseX, float mouseY, LivingEntity ent) {
        drawEntityOnScreen(graphics, posX, posY, scale, mouseX, mouseY, ent, false);
    }

    public static void drawEntityOnScreen(GuiGraphics graphics, int posX, int posY, int scale, float mouseX, float mouseY, LivingEntity ent, boolean renderNameTag) {
        Component f1 = ent.getCustomName();
        if(!renderNameTag) ent.setCustomName(null);
        renderEntityInInventoryFollowsMouse(graphics, posX, posY, scale, mouseX, mouseY, ent);
        if(!renderNameTag) ent.setCustomName(f1);
    }

    public static void renderEntityInInventoryFollowsMouse(GuiGraphics pGuiGraphics, int pX, int pY, int pScale, float pMouseX, float pMouseY, LivingEntity pEntity) {
        float f = (float)Math.atan((double)(pMouseX / 40.0F));
        float f1 = (float)Math.atan((double)(pMouseY / 40.0F));
        renderEntityInInventoryFollowsAngle(pGuiGraphics, pX, pY, pScale, f, f1, pEntity);
    }

    public static void renderEntityInInventoryFollowsAngle(GuiGraphics pGuiGraphics, int pX, int pY, int pScale, float angleXComponent, float angleYComponent, LivingEntity pEntity) {
        float f = angleXComponent;
        float f1 = angleYComponent;
        Quaternionf quaternionf = (new Quaternionf()).rotateZ((float)Math.PI);
        Quaternionf quaternionf1 = (new Quaternionf()).rotateX(f1 * 20.0F * ((float)Math.PI / 180F));
        quaternionf.mul(quaternionf1);
        float f2 = pEntity.yBodyRot;
        float f3 = pEntity.getYRot();
        float f4 = pEntity.getXRot();
        float f5 = pEntity.yHeadRotO;
        float f6 = pEntity.yHeadRot;
        pEntity.yBodyRot = 180.0F + f * 20.0F;
        pEntity.setYRot(180.0F + f * 40.0F);
        pEntity.setXRot(-f1 * 20.0F);
        pEntity.yHeadRot = 180.0F + f * 40.0F;  //ROBOTICS: Manually set head rotation to desired angle as head & body rotations are disconnected for robots
        pEntity.yHeadRotO = 180.0F + f * 40.0F; //ROBOTICS: Manually set head rotation to desired angle as head & body rotations are disconnected for robots
        InventoryScreen.renderEntityInInventory(pGuiGraphics, pX, pY, pScale, quaternionf, quaternionf1, pEntity);
        pEntity.yBodyRot = f2;
        pEntity.setYRot(f3);
        pEntity.setXRot(f4);
        pEntity.yHeadRotO = f5;
        pEntity.yHeadRot = f6;
    }

    public static void drawRotatingEntity(PoseStack posestack1, int posX, int posY, int scale, LivingEntity entity, float angle) {
        float f = 0;
        float f1 = 0;
        PoseStack posestack = RenderSystem.getModelViewStack();
        posestack.pushPose();
        posestack.translate((float)posX, (float)posY, 10500.0F);
        posestack.scale(1.0F, 1.0F, -1.0F);
        RenderSystem.applyModelViewMatrix();
        posestack1.pushPose();
        posestack1.translate(0.0F, 0.0F, 10000.0F);
        posestack1.scale((float)scale, (float)scale, (float)scale);
        Quaternionf quaternionf = (new Quaternionf()).rotateZ((float)Math.PI);
        // Add -30 deg to view slightly from above
        Quaternionf quaternionf1 = (new Quaternionf()).rotateX(f1 * 20.0F * ((float)Math.PI / 180F) - (float)Math.toRadians(30));
        //Keep rotating the entity
        Quaternionf quaternion_rotate = new Quaternionf().rotateY((float) Math.toRadians(angle - 180));
        quaternionf.mul(quaternionf1);
        quaternionf.mul(quaternion_rotate);
        posestack1.mulPose(quaternionf);
        float f2 = entity.yBodyRot;
        float f3 = entity.getYRot();
        float f4 = entity.getXRot();
        float f5 = entity.yHeadRotO;
        float f6 = entity.yHeadRot;
        entity.yBodyRot = 180.0F + f * 20.0F;
        entity.setYRot(180.0F + f * 40.0F);
        entity.setXRot(-f1 * 20.0F);
        entity.yHeadRot = entity.getYRot();
        entity.yHeadRotO = entity.getYRot();
        Lighting.setupForEntityInInventory();
        EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        quaternionf1.conjugate();
        entityrenderdispatcher.overrideCameraOrientation(quaternionf1);
        entityrenderdispatcher.setRenderShadow(false);
        MultiBufferSource.BufferSource multibuffersource$buffersource = Minecraft.getInstance().renderBuffers().bufferSource();
        RenderSystem.runAsFancy(() -> entityrenderdispatcher.render(entity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, posestack1, multibuffersource$buffersource, 15728880));
        multibuffersource$buffersource.endBatch();
        entityrenderdispatcher.setRenderShadow(true);
        entity.yBodyRot = f2;
        entity.setYRot(f3);
        entity.setXRot(f4);
        entity.yHeadRotO = f5;
        entity.yHeadRot = f6;
        posestack.popPose();
        posestack1.popPose();
        RenderSystem.applyModelViewMatrix();
        Lighting.setupFor3DItems();
    }

    public static void drawInactiveRobot(GuiGraphics graphics, int posX, int posY, int scale, LivingEntity entity, boolean renderNameTag) {
        float f = 0;
        float f1 = 0;
        posX += (int)(13/15F * scale);
        posY += (int)(34/15F * scale);
        Quaternionf quaternionf = (new Quaternionf()).rotateZ((float)Math.PI);
        Quaternionf quaternionf1 = (new Quaternionf()).rotateX(f1 * 20.0F * ((float)Math.PI / 180F));
        quaternionf.mul(quaternionf1);
        float f2 = entity.yBodyRot;
        float f3 = entity.getYRot();
        float f4 = entity.getXRot();
        float f5 = entity.yHeadRotO;
        float f6 = entity.yHeadRot;
        Component f7 = entity.getCustomName();
        if(!renderNameTag) entity.setCustomName(null);
        entity.yBodyRot = 180.0F + f * 20.0F;
        entity.setYRot(180.0F + f * 40.0F);
        entity.setXRot(-f1 * 20.0F + 45);
        entity.yHeadRot = entity.getYRot();
        entity.yHeadRotO = entity.getYRot();
        InventoryScreen.renderEntityInInventory(graphics, posX, posY, scale, quaternionf, quaternionf1, entity);
        entity.yBodyRot = f2;
        entity.setYRot(f3);
        entity.setXRot(f4);
        entity.yHeadRotO = f5;
        entity.yHeadRot = f6;
        if(!renderNameTag) entity.setCustomName(f7);
    }

    public static void drawItemStack(GuiGraphics graphics, ItemStack stack, int x, int y) {
        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        poseStack.translate(0.0F, 0.0F, 232.0F);
        graphics.renderItem(stack, x, y);
        var font = IClientItemExtensions.of(stack).getFont(stack, IClientItemExtensions.FontContext.ITEM_COUNT);
        graphics.renderItemDecorations(font == null ? Minecraft.getInstance().font : font, stack, x, y - (stack.isEmpty() ? 0 : 8), "");
        poseStack.popPose();
    }
}
