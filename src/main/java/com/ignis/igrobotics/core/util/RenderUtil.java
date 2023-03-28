package com.ignis.igrobotics.core.util;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.joml.Quaternionf;

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

    public static void drawRotatingEntity(int posX, int posY, int scale, LivingEntity entity, int angle) {
        float f = 0;
        float f1 = 0;
        PoseStack posestack = RenderSystem.getModelViewStack();
        posestack.pushPose();
        posestack.translate((float)posX, (float)posY, 1050.0F);
        posestack.scale(1.0F, 1.0F, -1.0F);
        RenderSystem.applyModelViewMatrix();
        PoseStack posestack1 = new PoseStack();
        posestack1.translate(0.0F, 0.0F, 1000.0F);
        posestack1.scale((float)scale, (float)scale, (float)scale);
        Quaternionf quaternionf = (new Quaternionf()).rotateZ((float)Math.PI);
        Quaternionf quaternionf1 = (new Quaternionf()).rotateX(f1 * 20.0F * ((float)Math.PI / 180F) + 30); // +30 To view slightly from above
        Quaternionf quaternion_rotate = new Quaternionf().rotateY(angle - 180); //Keep rotating the entity
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
        RenderSystem.applyModelViewMatrix();
        Lighting.setupFor3DItems();
    }

    protected void drawItemStack(ItemStack stack, int x, int y) {
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        PoseStack posestack = RenderSystem.getModelViewStack();
        posestack.translate(0.0F, 0.0F, 32.0F);
        RenderSystem.applyModelViewMatrix();
        //this.setBlitOffset(200);
        itemRenderer.blitOffset = 200.0F;
        var otherFont = IClientItemExtensions.of(stack).getFont(stack, IClientItemExtensions.FontContext.ITEM_COUNT);
        if (otherFont == null) otherFont = Minecraft.getInstance().font;
        itemRenderer.renderAndDecorateItem(stack, x, y);
        itemRenderer.renderGuiItemDecorations(otherFont, stack, x, y - (stack.isEmpty() ? 0 : 8), "");
        //this.setBlitOffset(0);
        itemRenderer.blitOffset = 0.0F;
    }
}
