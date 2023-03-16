package com.ignis.igrobotics.client.renderer;

import com.ignis.igrobotics.common.blockentity.StorageBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class RobotStorageRenderer implements BlockEntityRenderer<StorageBlockEntity> {

    public RobotStorageRenderer(BlockEntityRendererProvider.Context context) {
        //modelRobotFactory = new ModelRobotFactory(context.getModelSet().bakeLayer(LAYER_LOCATION));
    }

    @Override
    public void render(StorageBlockEntity storage, float pPartialTick, PoseStack poseStack, MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
        //Render the entity here
    }
}
