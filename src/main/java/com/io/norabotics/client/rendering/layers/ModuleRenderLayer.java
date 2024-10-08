package com.io.norabotics.client.rendering.layers;

import com.io.norabotics.common.capabilities.IPartBuilt;
import com.io.norabotics.common.capabilities.ModCapabilities;
import com.io.norabotics.common.content.entity.RobotEntity;
import com.io.norabotics.common.robot.RobotModule;
import com.io.norabotics.definitions.robotics.ModModules;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class ModuleRenderLayer extends GeoRenderLayer<RobotEntity> {

    private final int layerId;

    public ModuleRenderLayer(GeoRenderer<RobotEntity> entityRendererIn, int layerId) {
        super(entityRendererIn);
        this.layerId = layerId;
    }

    @Override
    public void render(PoseStack poseStack, RobotEntity robot, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        if(robot.isInvisible()) return;
        if(robot.getCapability(ModCapabilities.PARTS).resolve().isEmpty()) return;
        IPartBuilt parts = robot.getCapability(ModCapabilities.PARTS).resolve().get();

        if(!parts.hasRenderLayer(layerId)) return;
        RobotModule module = ModModules.byOverlayID(layerId);
        RenderType armorRenderType = RenderType.entityCutoutNoCull(module.getOverlay());

        getRenderer().reRender(getDefaultBakedModel(robot), poseStack, bufferSource, robot, armorRenderType,
                bufferSource.getBuffer(armorRenderType), partialTick, packedLight, packedOverlay,
                1, 1, 1, 1);
    }
}
