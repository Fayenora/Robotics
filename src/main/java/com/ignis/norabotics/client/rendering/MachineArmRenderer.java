package com.ignis.norabotics.client.rendering;

import au.edu.federation.caliko.FabrikBone3D;
import au.edu.federation.utils.Vec3f;
import com.ignis.norabotics.Robotics;
import com.ignis.norabotics.common.content.blockentity.MachineArmBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class MachineArmRenderer implements BlockEntityRenderer<MachineArmBlockEntity> {

    ResourceLocation MACHINE_ARM_TEXTURE = Robotics.rl("textures/machine_arm/texture.png");
    MachineArmModel<Entity> model;
    int color = FastColor.ARGB32.color(255, 155, 0, 0);

    public MachineArmRenderer(BlockEntityRendererProvider.Context context) {
        model = new MachineArmModel<>(context.bakeLayer(MachineArmModel.LAYER_LOCATION));
    }

    @Override
    public void render(MachineArmBlockEntity arm, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        VertexConsumer debug = pBuffer.getBuffer(RenderType.debugLineStrip(5));
        Vec3 cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        Vec3 pos = Vec3.atLowerCornerOf(arm.getBlockPos()).subtract(cameraPos).add(MachineArmModel.LOWER_LEFT_CORNER_OFFSET.scale(2));
        if(arm.getPose() != null && arm.getPose().getNumBones() > 0) {
            model.setPlatformRotation(arm.getPose(), arm.getTarget());
            for(FabrikBone3D bone : arm.getPose().getChain()) {
                vertex(debug, pPoseStack.last().pose(), pos, bone.getStartLocation().times(1 / 8f));
                vertex(debug, pPoseStack.last().pose(), pos, bone.getEndLocation().times(1 / 8f));
            }
        }
        VertexConsumer vertexconsumer = pBuffer.getBuffer(RenderType.entityCutout(MACHINE_ARM_TEXTURE));
        model.renderToBuffer(pPoseStack, vertexconsumer, LevelRenderer.getLightColor(arm.getLevel(), arm.getBlockPos().above()), pPackedOverlay, 1, 1, 1, 1);
    }

    private void vertex(VertexConsumer vertexConsumer, Matrix4f pose, Vec3 base, Vec3f pos) {
        vertexConsumer.vertex(pose, (float) base.x + pos.x, (float) base.y + pos.y, (float) base.z + pos.z).color(1f, 0, 0, 0).endVertex();
        vertexConsumer.vertex(pose, (float) base.x + pos.x, (float) base.y + pos.y, (float) base.z + pos.z).color(color).endVertex();
    }
}
