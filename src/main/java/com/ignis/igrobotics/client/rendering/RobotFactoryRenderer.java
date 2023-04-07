package com.ignis.igrobotics.client.rendering;

import com.ignis.igrobotics.common.blockentity.FactoryBlockEntity;
import com.ignis.igrobotics.common.blockentity.StorageBlockEntity;
import com.ignis.igrobotics.common.blocks.MachineBlock;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.Direction;

public class RobotFactoryRenderer implements BlockEntityRenderer<FactoryBlockEntity> {

    EntityRenderDispatcher entityRenderer;

    public RobotFactoryRenderer(BlockEntityRendererProvider.Context context) {
        entityRenderer = context.getEntityRenderer();
    }

    @Override
    public void render(FactoryBlockEntity storage, float pPartialTick, PoseStack poseStack, MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
        if(!storage.containsRobot()) return;
        Direction facing = storage.getBlockState().getValue(MachineBlock.FACING);
        RobotStorageRenderer.drawRobot(entityRenderer, storage.getRobot(), facing, poseStack, pPartialTick, pBufferSource, pPackedLight);
    }
}
