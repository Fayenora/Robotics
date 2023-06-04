package com.ignis.igrobotics.client.rendering;

import com.ignis.igrobotics.client.rendering.layers.*;
import com.ignis.igrobotics.common.entity.RobotEntity;
import com.ignis.igrobotics.core.robot.EnumRobotPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
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
        addRenderLayer(new ArmorRenderer<>(this));
        addRenderLayer(new HeldItemRenderer<>(this));
    }

    @Override
    public RenderType getRenderType(RobotEntity animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        //Do additional stuff here
        return super.getRenderType(animatable, texture, bufferSource, partialTick);
    }
}
