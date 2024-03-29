package com.ignis.igrobotics.client.rendering;

import com.ignis.igrobotics.Reference;
import com.ignis.igrobotics.client.rendering.layers.*;
import com.ignis.igrobotics.common.entity.RobotEntity;
import com.ignis.igrobotics.core.capabilities.ModCapabilities;
import com.ignis.igrobotics.core.capabilities.robot.RobotCapability;
import com.ignis.igrobotics.core.robot.EnumRobotPart;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

@OnlyIn(Dist.CLIENT)
public class RobotRenderer extends GeoEntityRenderer<RobotEntity> {

    public RobotRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new RobotModel());
        shadowRadius = 0.4f;
        for(EnumRobotPart part : EnumRobotPart.values()) {
            addRenderLayer(new LimbLayer(this, part));
            addRenderLayer(new ColorLayer(this, part)); //TODO: A color layer for every part might not be necessary as one should be able to hide the bones in the layer
        }
        for(int i = 0; i < Reference.MAX_MODULES; i++) {
            addRenderLayer(new ModuleRenderLayer(this, i));
        }
        addRenderLayer(new ArmorRenderer<>(this));
        addRenderLayer(new HeldItemRenderer<>(this));
        addRenderLayer(new ShieldLayer(this));
    }

    @Override
    public void scaleModelForRender(float widthScale, float heightScale, PoseStack poseStack, RobotEntity animatable, BakedGeoModel model, boolean isReRender, float partialTick, int packedLight, int packedOverlay) {
        if(animatable.getCapability(ModCapabilities.ROBOT).isPresent()) {
            animatable.getCapability(ModCapabilities.ROBOT).ifPresent(robot -> {
                float f = (float) robot.getSwell() / RobotCapability.MAX_SWELL;
                float f1 = 1.0F + Mth.sin(f * 100.0F) * f * 0.01F;
                f = Mth.clamp(f, 0.0F, 1.0F);
                f *= f;
                f *= f;
                float f2 = (1.0F + f * 0.4F) * f1;
                float f3 = (1.0F + f * 0.1F) / f1;
                super.scaleModelForRender(f2, f3, poseStack, animatable, model, isReRender, partialTick, packedLight, packedOverlay);
            });
        } else super.scaleModelForRender(widthScale, heightScale, poseStack, animatable, model, isReRender, partialTick, packedLight, packedOverlay);
    }

    @Override
    public void actuallyRender(PoseStack poseStack, RobotEntity animatable, BakedGeoModel model, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        if(animatable.getCapability(ModCapabilities.ROBOT).isPresent()) {
            animatable.getCapability(ModCapabilities.ROBOT).ifPresent(robot -> {
                float swell = robot.getSwell();
                float f = swell / RobotCapability.MAX_SWELL;
                float whiteOverlay =  (int)(f * 10.0F) % 2 == 0 ? 0.0F : Mth.clamp(f, 0.5F, 1.0F);
                super.actuallyRender(poseStack, animatable, model, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, getPackedOverlay(animatable, whiteOverlay), red, green, blue, alpha);
            });
        } else super.actuallyRender(poseStack, animatable, model, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);

    }
}
