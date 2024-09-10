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
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4d;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import javax.annotation.ParametersAreNonnullByDefault;

import static com.ignis.norabotics.client.rendering.MachineArmModel.Y_AXIS;

@ParametersAreNonnullByDefault
@OnlyIn(Dist.CLIENT)
public class MachineArmRenderer implements BlockEntityRenderer<MachineArmBlockEntity> {

    public static final ResourceLocation MACHINE_ARM_TEXTURE = Robotics.rl("textures/machine_arm/texture.png");
    public static final double[] SPEEDS = new double[] {Math.toRadians(1), Math.toRadians(1), Math.toRadians(1), Math.toRadians(2)};
    public static final boolean[] ALLOW_FULL_ROTATION = new boolean[] {true, false, false, true};
    private static final int color = FastColor.ARGB32.color(255, 155, 0, 0);

    MachineArmModel<Entity> model;
    private float[] currentPose;
    private final ItemRenderer itemRenderer;

    public MachineArmRenderer(BlockEntityRendererProvider.Context context) {
        model = new MachineArmModel<>(context.bakeLayer(MachineArmModel.LAYER_LOCATION));
        itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(MachineArmBlockEntity arm, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        // Arm Movement
        if(arm.getPose() != null && arm.getPose().getNumBones() > 0) {
            float[] targetPose = chainToRotations(arm.getPose(), arm.getTarget(), arm.getState() == MachineArmBlockEntity.MachineArmState.PICKING_UP_ITEM);
            if (currentPose == null) currentPose = targetPose;
            moveToPose(currentPose, targetPose);
            model.setPlatformRotation(currentPose);
        }

        // Debug Lines
        if(Minecraft.getInstance().getEntityRenderDispatcher().shouldRenderHitBoxes() && arm.getPose() != null && arm.getPose().getNumBones() > 0) {
            VertexConsumer debug = pBuffer.getBuffer(RenderType.debugLineStrip(5));
            Vec3 cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
            Vec3 armBase = Vec3.atLowerCornerOf(arm.getBlockPos()).subtract(cameraPos).add(MachineArmModel.LOWER_LEFT_CORNER_OFFSET.scale(2));
            for(FabrikBone3D bone : arm.getPose().getChain()) {
                vertex(debug, pPoseStack.last().pose(), armBase, bone.getStartLocation().times(1 / 8f));
                vertex(debug, pPoseStack.last().pose(), armBase, bone.getEndLocation().times(1 / 8f));
            }
        }

        // Model
        VertexConsumer vertexconsumer = pBuffer.getBuffer(RenderType.entityCutout(MACHINE_ARM_TEXTURE));
        int light = LevelRenderer.getLightColor(arm.getLevel(), arm.getBlockPos().above());
        model.renderToBuffer(pPoseStack, vertexconsumer, light, pPackedOverlay, 1, 1, 1, 1);

        // Item
        /*
        FabrikBone3D lastBone = arm.getPose().getBone(arm.getPose().getNumBones() - 1);
        Vec3f endEffector = arm.getPose().getEffectorLocation().minus(lastBone.getEndLocation().minus(lastBone.getStartLocation()).times(2));
        Vec3 itemOffset = MathUtil.of(endEffector).scale(1 / 16d).add(MachineArmModel.LOWER_LEFT_CORNER_OFFSET);
        BlockPos roughItemPos = BlockPos.containing(Vec3.atLowerCornerOf(arm.getBlockPos()).add(itemOffset));
        int itemLight = LevelRenderer.getLightColor(arm.getLevel(), roughItemPos);

        Vector3f origin = new Vector3f(0, 0, 0);
        Vector3f currentTranslation = new Vector3f(0, 0, 0);
        pPoseStack.pushPose();
        //pPoseStack.last().pose().getColumn(3, origin);
        pPoseStack.translate(MachineArmModel.LOWER_LEFT_CORNER_OFFSET.x, MachineArmModel.LOWER_LEFT_CORNER_OFFSET.y, MachineArmModel.LOWER_LEFT_CORNER_OFFSET.z);
        pPoseStack.last().pose().getColumn(3, currentTranslation);
        //rotateCoordinateSystem(pPoseStack, (float) Math.toRadians(90), (float) Math.toRadians(180), 0, origin, origin);
        pPoseStack.mulPoseMatrix(forwardKinematics(currentPose, MachineArmModel.ARM_LENGTHS));
        pPoseStack.last().pose().getColumn(3, currentTranslation);
        rotateCoordinateSystem(pPoseStack, 0, (float) Math.toRadians(180), (float) Math.toRadians(90), origin, origin);
        Robotics.LOGGER.debug("Rotations: {}, {}, {}, {}", currentPose[0], currentPose[1], currentPose[2], currentPose[3]);
        Robotics.LOGGER.debug("Wanted: x: {}, y: {}, z: {}", itemOffset.x, itemOffset.y, itemOffset.z);
        Robotics.LOGGER.debug("Current: x: {}, y: {}, z: {}", pPoseStack.last().pose().m30(), pPoseStack.last().pose().m31(), pPoseStack.last().pose().m32());
        itemRenderer.renderStatic(arm.getGrabbedItem(), ItemDisplayContext.GROUND, itemLight, pPackedOverlay, pPoseStack, pBuffer, arm.getLevel(), 0);
        pPoseStack.popPose();
         */
    }

    private void rotateCoordinateSystem(PoseStack poseStack, float x, float y, float z, Vector3f origin, Vector3f current) {
        poseStack.rotateAround(new Quaternionf().rotateXYZ(x, y, z), origin.sub(current).x, origin.sub(current).y, origin.sub(current).z);
    }

    private Matrix4f forwardKinematics(float[] rotations, int[] lengths) {
        return denavitHartenberger((float) (currentPose[0]), (float) Math.toRadians(-90), 0, 0).mul(
                denavitHartenberger((float) (Math.toRadians(90) - currentPose[1]), 0, MachineArmModel.ARM_LENGTHS[0] / 16f, 0)).mul(
                denavitHartenberger(currentPose[2], 0, MachineArmModel.ARM_LENGTHS[1] / 16f, 0)).mul(
                denavitHartenberger(currentPose[3], 0, MachineArmModel.ARM_LENGTHS[2] / 16f, 0)
        );
    }

    /**
     *
     * @param theta angle about previous z from old x to new x
     * @param r length of the common normal. Assuming a revolute joint, this is the radius about previous z.
     * @param d offset along previous z to the common normal
     * @param alpha angle about common normal, from old z axis to new z axis
     * @see <a href="https://en.wikipedia.org/wiki/Denavit%E2%80%93Hartenberg_parameters">Denavit Hartenberg Paramters</a>
     * @return Denavit-Hartenberger matrix for this joint
     */
    private static Matrix4f denavitHartenberger(float theta, float alpha, float d, float r) {
        double cosTheta = Math.cos(theta);
        double sinTheta = Math.sin(theta);
        double cosAlpha = Math.cos(alpha);
        double sinAlpha = Math.sin(alpha);

        return new Matrix4f(new Matrix4d(
                cosTheta, -sinTheta * cosAlpha, sinTheta * sinAlpha, r * cosTheta,
                sinTheta, cosTheta * cosAlpha, -cosTheta * sinAlpha, r * sinTheta,
                0, sinAlpha, cosAlpha, d,
                0, 0, 0, 1)).transpose();
    }


    private void vertex(VertexConsumer vertexConsumer, Matrix4f pose, Vec3 base, Vec3f pos) {
        vertexConsumer.vertex(pose, (float) base.x + pos.x, (float) base.y + pos.y, (float) base.z + pos.z).color(1f, 0, 0, 0).endVertex();
        vertexConsumer.vertex(pose, (float) base.x + pos.x, (float) base.y + pos.y, (float) base.z + pos.z).color(color).endVertex();
    }

    private float[] chainToRotations(FabrikChain3D chain, Vec3f referenceVec, boolean invertEffector) {
        Vec3f firstRot = chain.getBone(0).getDirectionUV();
        Vec3f secondRot = chain.getBone(1).getDirectionUV();
        Vec3f thirdRot = chain.getBone(2).getDirectionUV();
        referenceVec = referenceVec.cross(Y_AXIS).negate().normalise();

        // Caliko cannot solve for roll -> Solution: the first joint passed into caliko combines the rotation platform and the first arm
        return new float[] {
                (float) (Math.atan2(firstRot.x, firstRot.z) + Math.toRadians(90)),
                (float) Math.atan2(Math.sqrt(firstRot.x * firstRot.x + firstRot.z * firstRot.z), firstRot.y),
                rot(firstRot, secondRot, referenceVec),
                invertEffector ? (float) (Math.toRadians(180) + rot(secondRot, thirdRot, referenceVec)) : rot(secondRot, thirdRot, referenceVec)
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
