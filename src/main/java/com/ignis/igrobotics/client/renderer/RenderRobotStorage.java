package com.ignis.igrobotics.client.renderer;

import com.ignis.igrobotics.Robotics;
import com.ignis.igrobotics.common.blockentity.BlockEntityStorage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

public class RenderRobotStorage implements BlockEntityRenderer<BlockEntityStorage> {

    public RenderRobotStorage(BlockEntityRendererProvider.Context context) {
        //modelRobotFactory = new ModelRobotFactory(context.getModelSet().bakeLayer(LAYER_LOCATION));
    }

    @Override
    public void render(BlockEntityStorage storage, float pPartialTick, PoseStack poseStack, MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
        //Render the entity here
    }
}
