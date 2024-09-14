package com.io.norabotics.client.rendering;

import com.io.norabotics.common.content.blockentity.FactoryBlockEntity;
import com.io.norabotics.common.content.blocks.MachineBlock;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.ParametersAreNonnullByDefault;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
public class RobotFactoryRenderer implements BlockEntityRenderer<FactoryBlockEntity> {

    EntityRenderDispatcher entityRenderer;

    public RobotFactoryRenderer(BlockEntityRendererProvider.Context context) {
        entityRenderer = context.getEntityRenderer();
    }

    @Override
    public void render(FactoryBlockEntity storage, float pPartialTick, PoseStack poseStack, MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
        if(storage.getEntity().isEmpty()) return;
        Direction facing = storage.getBlockState().getValue(MachineBlock.FACING);
        poseStack.translate(0, 0.075, 0);
        RobotStorageRenderer.drawRobot(entityRenderer, storage.getEntity().get(), facing, poseStack, pPartialTick, pBufferSource, pPackedLight);
    }
}
