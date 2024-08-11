package com.ignis.igrobotics.client.rendering.layers;

import com.ignis.igrobotics.common.capabilities.IRobot;
import com.ignis.igrobotics.common.capabilities.ModCapabilities;
import com.ignis.igrobotics.common.content.entity.RobotEntity;
import com.ignis.igrobotics.common.robot.RobotModule;
import com.ignis.igrobotics.definitions.ModModules;
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
        if(robot.getCapability(ModCapabilities.ROBOT).resolve().isEmpty()) return;
        IRobot robotics = robot.getCapability(ModCapabilities.ROBOT).resolve().get();

        if(!robotics.hasRenderLayer(layerId)) return;
        RobotModule module = ModModules.byOverlayID(layerId);
        RenderType armorRenderType = RenderType.entityCutoutNoCull(module.getOverlay());

        getRenderer().reRender(getDefaultBakedModel(robot), poseStack, bufferSource, robot, armorRenderType,
                bufferSource.getBuffer(armorRenderType), partialTick, packedLight, packedOverlay,
                1, 1, 1, 1);
    }
}
