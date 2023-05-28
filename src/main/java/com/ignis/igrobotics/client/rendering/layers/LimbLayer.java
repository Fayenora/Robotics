package com.ignis.igrobotics.client.rendering.layers;

import com.ignis.igrobotics.common.entity.RobotEntity;
import com.ignis.igrobotics.core.robot.EnumRobotMaterial;
import com.ignis.igrobotics.core.robot.EnumRobotPart;
import com.ignis.igrobotics.core.robot.RobotPart;
import com.ignis.igrobotics.core.capabilities.ModCapabilities;
import com.ignis.igrobotics.core.capabilities.parts.IPartBuilt;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class LimbLayer extends GeoRenderLayer<RobotEntity> {

    private final EnumRobotPart part;

    public LimbLayer(GeoRenderer<RobotEntity> entityRendererIn, EnumRobotPart part) {
        super(entityRendererIn);
        this.part = part;
    }

    @Override
    public void render(PoseStack poseStack, RobotEntity robot, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        if(robot.isInvisible()) return;
        IPartBuilt parts = robot.getCapability(ModCapabilities.PARTS).orElse(ModCapabilities.NO_PARTS);

        RobotPart limb = parts.getBodyPart(part);

        if(limb.getMaterial() == EnumRobotMaterial.NONE) {
            return;
        }

        ResourceLocation texture = limb.getLimbResourceLocation();
        RenderType armorRenderType = RenderType.armorCutoutNoCull(texture);

        getRenderer().reRender(getDefaultBakedModel(robot), poseStack, bufferSource, robot, armorRenderType,
                bufferSource.getBuffer(armorRenderType), partialTick, packedLight, OverlayTexture.NO_OVERLAY,
                1, 1, 1, 1);
    }
}
