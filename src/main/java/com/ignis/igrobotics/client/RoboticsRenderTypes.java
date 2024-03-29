package com.ignis.igrobotics.client;

import com.google.common.collect.ImmutableMap;
import com.ignis.igrobotics.Robotics;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public class RoboticsRenderTypes extends RenderType {

    public static final VertexFormat SURFACE = new VertexFormat(ImmutableMap.<String, VertexFormatElement>builder().put("Position", DefaultVertexFormat.ELEMENT_POSITION).put("Normal", DefaultVertexFormat.ELEMENT_NORMAL).build());

    public RoboticsRenderTypes(String p_173178_, VertexFormat p_173179_, VertexFormat.Mode p_173180_, int p_173181_, boolean p_173182_, boolean p_173183_, Runnable p_173184_, Runnable p_173185_) {
        super(p_173178_, p_173179_, p_173180_, p_173181_, p_173182_, p_173183_, p_173184_, p_173185_);
        throw new IllegalCallerException("Class not meant to be constructed");
    }

    public static final Function<ResourceLocation, RenderType> RENDER_TYPE_SHIELD = Util.memoize(resourceLocation -> {
        RenderType.CompositeState state = RenderType.CompositeState.builder()
                .setShaderState(ClientSetup.SHADER_SHIELD.shard)
                .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                .createCompositeState(true);
        return create(Robotics.MODID + ":shield", SURFACE, VertexFormat.Mode.QUADS, 256, true, false, state);
    });
}
