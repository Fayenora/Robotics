package com.ignis.igrobotics.client.rendering.layers;

import com.google.common.collect.ImmutableMap;
import com.ignis.igrobotics.Robotics;
import com.ignis.igrobotics.client.ClientSetup;
import com.ignis.igrobotics.client.RoboticsRenderTypes;
import com.ignis.igrobotics.common.entity.RobotEntity;
import com.ignis.igrobotics.core.capabilities.ModCapabilities;
import com.ignis.igrobotics.core.capabilities.parts.IPartBuilt;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.geometry.StandaloneGeometryBakingContext;
import net.minecraftforge.client.model.obj.ObjLoader;
import net.minecraftforge.client.model.obj.ObjModel;
import net.minecraftforge.client.model.renderable.CompositeRenderable;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class ShieldLayer extends GeoRenderLayer<RobotEntity> {

    public static final ResourceLocation MODEL = new ResourceLocation(Robotics.MODID, "models/uvsphere.obj");
    private static final float WIDTHMOD = 3 / 2f;
    private static final float HEIGHTMOD = 2 / 3f;

    private final CompositeRenderable renderable;

    public ShieldLayer(GeoRenderer<RobotEntity> renderer) {
        super(renderer);
        ObjModel model = new ObjLoader().loadModel(new ObjModel.ModelSettings(MODEL, false, false, false, true, null));
        renderable = model.bakeRenderable(StandaloneGeometryBakingContext.create(MODEL));
    }

    @Override
    public void render(PoseStack poseStack, RobotEntity animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        float[] color = animatable.getCapability(ModCapabilities.PARTS).orElse(ModCapabilities.NO_PARTS).getColor().getTextureDiffuseColors();

        AABB aabb = animatable.getBoundingBox();
        float widthX = (float) (aabb.maxX - aabb.minX) * WIDTHMOD;
        float height = (float) (aabb.maxY - aabb.minY) * HEIGHTMOD;
        float widthZ = (float) (aabb.maxZ - aabb.minZ) * WIDTHMOD;
        Vec3 centerOffset = aabb.getCenter().subtract(animatable.position());

        Matrix4f modelMat = translateAndScale(animatable.position(), 1, 1, 1);

        Vector3f cameraPos = Minecraft.getInstance().player.getEyePosition().toVector3f();

        poseStack.pushPose();
        renderable.render(poseStack, bufferSource, name -> {
            ClientSetup.SHADER_SHIELD.get().safeGetUniform("Color").set(color);
            ClientSetup.SHADER_SHIELD.get().safeGetUniform("CameraPos").set(cameraPos);
            return RoboticsRenderTypes.RENDER_TYPE_SHIELD.apply(name);
        }, packedLight, packedOverlay, partialTick, transformOf(translateAndScale(centerOffset, widthX, height, widthZ)));
        poseStack.popPose();
    }

    private CompositeRenderable.Transforms transformOf(Matrix4f operation) {
        return  CompositeRenderable.Transforms.of(ImmutableMap.<String, Matrix4f>builder().put("Sphere", operation).build());
    }

    private Matrix4f translateAndScale(Vec3 translation, float xScale, float yScale, float zScale) {
        return new Matrix4f(xScale, 0, 0, 0, 0, yScale, 0, 0, 0, 0, zScale, 0, (float) translation.x, (float) translation.y, (float) translation.z, 1);
    }
}
