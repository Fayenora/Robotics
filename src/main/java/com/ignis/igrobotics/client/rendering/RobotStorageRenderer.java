package com.ignis.igrobotics.client.rendering;

import com.ignis.igrobotics.common.blockentity.StorageBlockEntity;
import com.ignis.igrobotics.common.blocks.MachineBlock;
import com.ignis.igrobotics.common.blocks.StorageBlock;
import com.ignis.igrobotics.common.entity.RobotEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import org.joml.Quaternionf;

public class RobotStorageRenderer implements BlockEntityRenderer<StorageBlockEntity> {

    EntityRenderDispatcher entityRenderer;

    public RobotStorageRenderer(BlockEntityRendererProvider.Context context) {
        entityRenderer = context.getEntityRenderer();
    }

    @Override
    public void render(StorageBlockEntity storage, float pPartialTick, PoseStack poseStack, MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
        if(!storage.containsRobot()) return;
        Direction facing = storage.getBlockState().getValue(MachineBlock.FACING);
        drawRobot(entityRenderer, storage.getEntity(), facing, poseStack, pPartialTick, pBufferSource, pPackedLight);
    }

    public static void drawRobot(EntityRenderDispatcher renderDispatcher, Entity entity, Direction facing, PoseStack poseStack, float partialTicks, MultiBufferSource bufferSource, int packetLight) {
        Quaternionf rotation = facing.getRotation();
        rotation.mul(new Quaternionf().rotateX((float) Math.toRadians(-90)));
        poseStack.translate(0.5, 0.05, 0.5);
        poseStack.mulPose(rotation);
        poseStack.scale(0.9f, 0.9f, 0.9f);
        renderDispatcher.render(entity, 0, 0, 0, 0, partialTicks, poseStack, bufferSource, packetLight);
    }
}
