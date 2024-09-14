package com.io.norabotics.client.rendering.layers;

import com.io.norabotics.common.capabilities.IPartBuilt;
import com.io.norabotics.common.capabilities.ModCapabilities;
import com.io.norabotics.common.content.entity.RobotEntity;
import com.io.norabotics.common.robot.EnumRobotMaterial;
import com.io.norabotics.common.robot.EnumRobotPart;
import com.io.norabotics.common.robot.RobotPart;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

@OnlyIn(Dist.CLIENT)
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

        EnumRobotMaterial material = parts.materialForSlot(part.toModuleSlot());

        if(material == EnumRobotMaterial.NONE) return;

        RobotPart limb = RobotPart.get(part, material);
        ResourceLocation texture = limb.getLimbResourceLocation();
        RenderType armorRenderType = RenderType.entityCutoutNoCull(texture);

        getRenderer().reRender(getDefaultBakedModel(robot), poseStack, bufferSource, robot, armorRenderType,
                bufferSource.getBuffer(armorRenderType), partialTick, packedLight, packedOverlay,
                1, 1, 1, 1);
    }
}
