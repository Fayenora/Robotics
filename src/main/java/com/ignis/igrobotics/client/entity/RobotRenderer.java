package com.ignis.igrobotics.client.entity;

import com.ignis.igrobotics.common.entity.RobotEntity;
import com.ignis.igrobotics.core.RobotPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.ItemArmorGeoLayer;

public class RobotRenderer extends GeoEntityRenderer<RobotEntity> {

    public RobotRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new RobotModel());
        shadowRadius = 0.4f;
        for(RobotPart.EnumRobotPart part : RobotPart.EnumRobotPart.values()) {
            addRenderLayer(new ColorLayer(this, part));
            addRenderLayer(new LimbLayer(this, part));
        }
        addRenderLayer(new ItemArmorGeoLayer<>(this));
    }

    @Override
    public RenderType getRenderType(RobotEntity animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        //Do additional stuff here
        return super.getRenderType(animatable, texture, bufferSource, partialTick);
    }
}
