package com.ignis.igrobotics.client.rendering.layers;

import com.ignis.igrobotics.common.entity.RobotEntity;
import com.ignis.igrobotics.core.capabilities.ModCapabilities;
import com.ignis.igrobotics.core.capabilities.parts.IPartBuilt;
import com.ignis.igrobotics.core.robot.EnumRobotMaterial;
import com.ignis.igrobotics.core.robot.EnumRobotPart;
import com.ignis.igrobotics.core.robot.RobotPart;
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
public class ColorLayer extends GeoRenderLayer<RobotEntity> {

    private final EnumRobotPart part;

    public ColorLayer(GeoRenderer<RobotEntity> entityRendererIn, EnumRobotPart part) {
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

        ResourceLocation texture = limb.getColorResourceLocation(parts.getTemporaryColor());
        RenderType armorRenderType = RenderType.entityCutoutNoCull(texture);

        getRenderer().reRender(getDefaultBakedModel(robot), poseStack, bufferSource, robot, armorRenderType,
                bufferSource.getBuffer(armorRenderType), partialTick, packedLight, packedOverlay,
                1, 1, 1, 1);
    }
}
