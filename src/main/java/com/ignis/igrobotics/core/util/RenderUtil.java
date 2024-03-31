package com.ignis.igrobotics.core.util;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
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

    public static void enableScissor(Rectangle rect) {
        GuiComponent.enableScissor(rect.x, rect.y, rect.x + rect.width, rect.y + rect.height);
    }

    public static void disableScissor() {
        GuiComponent.disableScissor();
    }

    public static void drawString(PoseStack poseStack, String text, int x, int y, int color, float scale) {
        poseStack.pushPose();
        poseStack.scale(scale, scale, 1);
        GuiComponent.drawString(poseStack, Minecraft.getInstance().font, text, (int) (x / scale), (int) (y / scale), color);
        poseStack.popPose();
    }

    public static void drawCenteredString(PoseStack poseStack, String text, int x, int y, int color, float scale) {
        Font font = Minecraft.getInstance().font;
        poseStack.pushPose();
        poseStack.scale(scale, scale, 1);
        GuiComponent.drawString(poseStack, Minecraft.getInstance().font, text, (int) ((x - font.width(text)) / scale), (int) (y /scale), color);
        poseStack.popPose();
    }

    public static void drawString(PoseStack poseStack, String text, int x, int y, int color, float scaleY, int maxWidth) {
        Font font = Minecraft.getInstance().font;
        poseStack.pushPose();
        float scale = Math.min(scaleY, maxWidth / (float)font.width(text));
        poseStack.scale(scale, scaleY, 1);
        GuiComponent.drawString(poseStack, Minecraft.getInstance().font, text, (int) (x / scale), (int) (y / scaleY), color);
        poseStack.popPose();
    }

    public static void drawCenteredString(PoseStack poseStack, String text, int x, int y, int color, float scaleY, int maxWidth) {
        Font font = Minecraft.getInstance().font;
        poseStack.pushPose();
        float scale = Math.min(scaleY, maxWidth / (float)font.width(text));
        poseStack.scale(scale, scaleY, 1);
        GuiComponent.drawString(poseStack, Minecraft.getInstance().font, text, (int) (x / scale - font.width(text) / 2), (int) (y / scaleY), color);
        poseStack.popPose();
    }

    public static void drawString(PoseStack poseStack, Component text, int x, int y, int color, float scale) {
        drawString(poseStack, text.getString(), x, y, color, scale);
    }

    public static void drawCenteredString(PoseStack poseStack, Component text, int x, int y, int color, float scale) {
        drawCenteredString(poseStack, text.getString(), x, y, color, scale);
    }

    public static void drawString(PoseStack poseStack, Component text, int x, int y, int color, float scaleY, int maxWidth) {
        drawString(poseStack, text.getString(), x, y, color, scaleY, maxWidth);
    }

    public static void drawCenteredString(PoseStack poseStack, Component text, int x, int y, int color, float scaleY, int maxWidth) {
        drawCenteredString(poseStack, text.getString(), x, y, color, scaleY, maxWidth);
    }

    public static void drawEntityOnScreen(PoseStack poseStack, int x, int y, int mouseX, int mouseY, LivingEntity entity) {
        drawEntityOnScreen(poseStack, x, y, mouseX, mouseY, 30, false, entity);
    }

    public static void drawEntityOnScreen(PoseStack poseStack, int x, int y, int mouseX, int mouseY, int scale, boolean ignoreMouse, LivingEntity entity) {
        int intmouseX = x - mouseX + (int)(5/6F * scale);
        int intmouseY = y - mouseY + (int)(17/30F * scale);
        if(ignoreMouse) {
            intmouseX = 0;
            intmouseY = 0;
        }

        drawEntityOnScreen(poseStack, x + (int)(13/15F * scale), y + (int)(34/15F * scale), scale, intmouseX, intmouseY, entity);
    }

    public static void drawEntityOnScreen(PoseStack poseStack, int posX, int posY, int scale, float mouseX, float mouseY, LivingEntity ent) {
        drawEntityOnScreen(poseStack, posX, posY, scale, mouseX, mouseY, ent, false);
    }

    public static void drawEntityOnScreen(PoseStack poseStack, int posX, int posY, int scale, float mouseX, float mouseY, LivingEntity ent, boolean renderNameTag) {
        Component f1 = ent.getCustomName();
        if(!renderNameTag) ent.setCustomName(null);
        InventoryScreen.renderEntityInInventoryFollowsMouse(poseStack, posX, posY, scale, mouseX, mouseY, ent);
        if(!renderNameTag) ent.setCustomName(f1);
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

    public static void drawInactiveRobot(PoseStack poseStack, int posX, int posY, int scale, LivingEntity entity, boolean renderNameTag) {
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
        InventoryScreen.renderEntityInInventory(poseStack, posX, posY, scale, quaternionf, quaternionf1, entity);
        entity.yBodyRot = f2;
        entity.setYRot(f3);
        entity.setXRot(f4);
        entity.yHeadRotO = f5;
        entity.yHeadRot = f6;
        if(!renderNameTag) entity.setCustomName(f7);
    }

    public static void drawItemStack(PoseStack poseStack, ItemStack stack, int x, int y) {
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        poseStack.pushPose();
        poseStack.translate(0.0F, 0.0F, 232.0F);
        itemRenderer.renderAndDecorateItem(poseStack, stack, x, y);
        var font = IClientItemExtensions.of(stack).getFont(stack, IClientItemExtensions.FontContext.ITEM_COUNT);
        itemRenderer.renderGuiItemDecorations(poseStack, font == null ? Minecraft.getInstance().font : font, stack, x, y - (stack.isEmpty() ? 0 : 8), "");
        poseStack.popPose();
    }
}
