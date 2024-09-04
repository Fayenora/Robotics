package com.ignis.norabotics.client.rendering;

import au.edu.federation.caliko.FabrikBone3D;
import au.edu.federation.caliko.FabrikChain3D;
import au.edu.federation.utils.Vec3f;
import com.ignis.norabotics.Robotics;
import com.ignis.norabotics.common.content.blockentity.MachineArmBlockEntity;
import com.ignis.norabotics.common.helpers.types.Tuple;
import com.ignis.norabotics.common.helpers.util.MathUtil;
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

import static com.ignis.norabotics.client.rendering.MachineArmModel.Y_AXIS;

public class MachineArmRenderer implements BlockEntityRenderer<MachineArmBlockEntity> {

    public static final ResourceLocation MACHINE_ARM_TEXTURE = Robotics.rl("textures/machine_arm/texture.png");
    public static final double[] SPEEDS = new double[] {Math.toRadians(1), Math.toRadians(1), Math.toRadians(1), Math.toRadians(2)};
    public static final boolean[] ALLOW_FULL_ROTATION = new boolean[] {true, false, false, true};
    private static final int color = FastColor.ARGB32.color(255, 155, 0, 0);

    MachineArmModel<Entity> model;
    private float[] currentPose, targetPose;

    public MachineArmRenderer(BlockEntityRendererProvider.Context context) {
        model = new MachineArmModel<>(context.bakeLayer(MachineArmModel.LAYER_LOCATION));
    }

    @Override
    public void render(MachineArmBlockEntity arm, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        VertexConsumer debug = pBuffer.getBuffer(RenderType.debugLineStrip(5));
        Vec3 cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        Vec3 pos = Vec3.atLowerCornerOf(arm.getBlockPos()).subtract(cameraPos).add(MachineArmModel.LOWER_LEFT_CORNER_OFFSET.scale(2));
        if(arm.getPose() != null && arm.getPose().getNumBones() > 0) {
            targetPose = chainToRotations(arm.getPose(), arm.getTarget());
            if(currentPose == null) currentPose = targetPose;
            moveToPose(currentPose, targetPose);
            model.setPlatformRotation(currentPose);
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

    private float[] chainToRotations(FabrikChain3D chain, Vec3f referenceVec) {
        Vec3f firstRot = chain.getBone(0).getDirectionUV();
        Vec3f secondRot = chain.getBone(1).getDirectionUV();
        Vec3f thirdRot = chain.getBone(2).getDirectionUV();
        referenceVec = referenceVec.cross(Y_AXIS).negate().normalise();

        // Caliko cannot solve for roll -> Solution: the first joint passed into caliko combines the rotation platform and the first arm
        return new float[] {
                (float) (Math.atan2(firstRot.x, firstRot.z) + Math.toRadians(90)),
                (float) Math.atan2(Math.sqrt(firstRot.x * firstRot.x + firstRot.z * firstRot.z), firstRot.y),
                rot(firstRot, secondRot, referenceVec),
                rot(secondRot, thirdRot, referenceVec)
        };
    }

    private void moveToPose(float[] from, float[] to) {
        if(from.length != to.length || to.length != MachineArmRenderer.SPEEDS.length) throw new IllegalArgumentException("");
        for(int i = 0; i < from.length; i++) {
            Tuple<Float, Integer> min = MathUtil.argmin(
                    Math.abs(from[i] - to[i]),
                    (float) (Math.abs(to[i] - 1.5 * Math.PI) + from[i] + 0.5 * Math.PI),
                    (float) (Math.abs(from[i] - 1.5 * Math.PI) + to[i] + 0.5 * Math.PI));
            if(!ALLOW_FULL_ROTATION[i]) min.second = 0;
            float sign = switch(min.second) {
                case 0 -> to[i] > from[i] ? 1 : -1;
                case 1 -> -1;
                default -> 1;
            };
            from[i] = min.first < MachineArmRenderer.SPEEDS[i] * 2 ? to[i] : (float) MathUtil.circularRange(from[i] + sign * MachineArmRenderer.SPEEDS[i], -0.5 * Math.PI, 1.5 * Math.PI);
        }
    }

    public static float rot(Vec3f vec1, Vec3f vec2, Vec3f referenceVec) {
        float sign = Math.signum(Vec3f.scalarProduct(referenceVec, vec1.cross(vec2)));
        float signedAngle = (float) Math.acos(Vec3f.scalarProduct(vec1.normalised(), vec2.normalised()));
        return Float.isNaN(signedAngle) ? 0 : sign * signedAngle;
    }
}
