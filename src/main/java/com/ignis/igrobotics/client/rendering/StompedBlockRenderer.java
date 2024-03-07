package com.ignis.igrobotics.client.rendering;

import com.ignis.igrobotics.common.entity.StompedUpBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;

public class StompedBlockRenderer extends EntityRenderer<StompedUpBlockEntity> {

    private final BlockRenderDispatcher dispatcher;

    public StompedBlockRenderer(EntityRendererProvider.Context p_174112_) {
        super(p_174112_);
        this.shadowRadius = 0.5F;
        this.dispatcher = p_174112_.getBlockRenderDispatcher();
    }

    @Override
    public ResourceLocation getTextureLocation(StompedUpBlockEntity entity) {
        return InventoryMenu.BLOCK_ATLAS;
    }

    @Override
    public void render(StompedUpBlockEntity stompedBlock, float p_114635_, float p_114636_, PoseStack p_114637_, MultiBufferSource p_114638_, int p_114639_) {
        BlockState blockstate = stompedBlock.getBlockState();
        if (blockstate != null && blockstate.getRenderShape() == RenderShape.MODEL) {
            p_114637_.pushPose();
            BlockPos blockpos = BlockPos.containing(stompedBlock.getX(), stompedBlock.getBoundingBox().maxY, stompedBlock.getZ());
            p_114637_.translate(-0.5D, 0.0D, -0.5D);
            var model = this.dispatcher.getBlockModel(blockstate);
            for (var renderType : model.getRenderTypes(blockstate, RandomSource.create(blockstate.getSeed(stompedBlock.getStartPos())), net.minecraftforge.client.model.data.ModelData.EMPTY))
                this.dispatcher.getModelRenderer().tesselateBlock(stompedBlock.level, model, blockstate, blockpos, p_114637_, p_114638_.getBuffer(renderType), false, RandomSource.create(), blockstate.getSeed(stompedBlock.getStartPos()), OverlayTexture.NO_OVERLAY, net.minecraftforge.client.model.data.ModelData.EMPTY, renderType);
            p_114637_.popPose();
            super.render(stompedBlock, p_114635_, p_114636_, p_114637_, p_114638_, p_114639_);
        }
    }
}
