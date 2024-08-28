package com.ignis.norabotics.client.rendering;

import com.ignis.norabotics.Robotics;
import com.ignis.norabotics.common.content.blockentity.MachineArmBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class MachineArmRenderer implements BlockEntityRenderer<MachineArmBlockEntity> {

    ResourceLocation MACHINE_ARM_TEXTURE = Robotics.rl("textures/machine_arm/texture.png");
    MachineArmModel<Entity> model;
    int something;

    public MachineArmRenderer(BlockEntityRendererProvider.Context context) {
        model = new MachineArmModel<>(context.bakeLayer(MachineArmModel.LAYER_LOCATION));
    }

    @Override
    public void render(MachineArmBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        VertexConsumer vertexconsumer = pBuffer.getBuffer(RenderType.entityCutout(MACHINE_ARM_TEXTURE));
        model.setPlatformRotation(0, (float) Math.toRadians(something++ % 360), (float) Math.toRadians(something % 360), 0);
        model.renderToBuffer(pPoseStack, vertexconsumer, LevelRenderer.getLightColor(pBlockEntity.getLevel(), pBlockEntity.getBlockPos().above()), pPackedOverlay, 1, 1, 1, 1);
    }
}
