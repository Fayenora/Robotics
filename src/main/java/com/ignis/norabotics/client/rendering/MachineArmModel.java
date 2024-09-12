package com.ignis.norabotics.client.rendering;

import au.edu.federation.caliko.FabrikBone3D;
import au.edu.federation.caliko.FabrikChain3D;
import au.edu.federation.caliko.FabrikJoint3D;
import au.edu.federation.utils.Vec3f;
import com.ignis.norabotics.Robotics;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.entity.ZombieRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class MachineArmModel<T extends Entity> extends EntityModel<T> {

	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Robotics.rl("machine_arm"), "main");
	public static final int JOINT_COUNT = 3;
	public static final Vec3f X_AXIS = new Vec3f(1, 0, 0);
	public static final Vec3f Y_AXIS = new Vec3f(0, 1, 0);
	public static final Vec3f Z_AXIS = new Vec3f(0, 0, 1);
	public static final int[] ARM_LENGTHS = new int[] {26, 19, 9};
	private static final Vec3f[] ROTATIONS = new Vec3f[] {
			new Vec3f(0, 1, 1),
			new Vec3f(0, 0, -1),
			new Vec3f(0, -1, -1)};
	public static final Vec3 LOWER_LEFT_CORNER_OFFSET = new Vec3(0.5, 1, 0.5);

	private final ModelPart platform;
	private final ModelPart first_arm;
	private final ModelPart rotation_joint_1;
	private final ModelPart second_arm;
	private final ModelPart joint_3;
	private final ModelPart bb_main;

	public MachineArmModel(ModelPart root) {
		this.platform = root.getChild("platform");
		this.first_arm = platform.getChild("first_arm");
		this.rotation_joint_1 = first_arm.getChild("rotation_joint_1");
		this.second_arm = rotation_joint_1.getChild("second_arm");
		this.joint_3 = second_arm.getChild("joint_3");
		this.bb_main = root.getChild("bb_main");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition platform = partdefinition.addOrReplaceChild("platform", CubeListBuilder.create().texOffs(0, 26).addBox(-5.0F, -6.0F, -5.0F, 10.0F, 2.0F, 10.0F, new CubeDeformation(0.0F))
				.texOffs(12, 38).addBox(-3.0F, -4.0F, 2.0F, 6.0F, 6.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(30, 26).addBox(-3.0F, -4.0F, -3.0F, 6.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(8.0F, 16.0F, 8.0F));

		PartDefinition first_arm = platform.addOrReplaceChild("first_arm", CubeListBuilder.create().texOffs(36, 34).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 23.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition rotation_joint_1 = first_arm.addOrReplaceChild("rotation_joint_1", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, 21.0F, -2.0F, 4.0F, 7.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition second_arm = rotation_joint_1.addOrReplaceChild("second_arm", CubeListBuilder.create().texOffs(44, 26).addBox(-2.0F, -3.0F, -3.0F, 4.0F, 6.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(16, 45).addBox(1.0F, 3.0F, 2.0F, 1.0F, 13.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 45).addBox(-2.0F, 3.0F, 2.0F, 1.0F, 13.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(20, 45).addBox(-1.0F, 8.0F, 2.0F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(48, 6).addBox(-2.0F, 16.0F, 2.0F, 4.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(0, 26).addBox(-2.0F, -3.0F, 2.0F, 4.0F, 6.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(48, 0).addBox(-2.0F, 16.0F, -3.0F, 4.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(8, 11).addBox(-1.0F, 8.0F, -3.0F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(30, 38).addBox(-2.0F, 3.0F, -3.0F, 1.0F, 13.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(26, 38).addBox(1.0F, 3.0F, -3.0F, 1.0F, 13.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 25.0F, 0.0F));

		PartDefinition joint_3 = second_arm.addOrReplaceChild("joint_3", CubeListBuilder.create().texOffs(0, 38).addBox(-1.0F, -2.0F, -2.0F, 2.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(0, 11).addBox(-1.0F, 4.0F, 0.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(12, 0).addBox(0.0F, 7.0F, 1.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(0, 0).addBox(0.0F, 4.0F, -2.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(0, 33).addBox(-1.0F, 4.0F, -2.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(20, 52).addBox(-2.0F, -4.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 19.0F, 0.0F));

		PartDefinition claw_1 = joint_3.addOrReplaceChild("claw_1", CubeListBuilder.create().texOffs(0, 49).addBox(-3.0F, -6.0F, -1.0F, 1.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition claw_2 = joint_3.addOrReplaceChild("claw_2", CubeListBuilder.create().texOffs(6, 49).addBox(2.0F, -6.0F, -1.0F, 1.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition bb_main = partdefinition.addOrReplaceChild("bb_main", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, 0.0F, -8.0F, 16.0F, 10.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(8.0F, 0.0F, 8.0F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	public static FabrikChain3D constructDefaultChain() {
		return constructChain(ROTATIONS);
	}

	public static FabrikChain3D constructChain(Vec3f[] rotations) {
		FabrikChain3D chain = new FabrikChain3D("Machine Arm");
		chain.addBone(new FabrikBone3D(new Vec3f(), rotations[0], ARM_LENGTHS[0]));
		for(int i = 1; i < JOINT_COUNT; i++) {
			chain.addConsecutiveBone(rotations[i], ARM_LENGTHS[i]);
		}
		FabrikJoint3D joint_2 = new FabrikJoint3D();
		FabrikJoint3D joint_3 = new FabrikJoint3D();
		joint_2.setAsLocalHinge(X_AXIS, 160, 160, Y_AXIS);
		joint_3.setAsLocalHinge(X_AXIS, 160, 160, Y_AXIS);
		chain.getBone(1).setJoint(joint_2);
		chain.getBone(2).setJoint(joint_3);
		return chain;
	}

	public void setPlatformRotation(float[] rotations) {
		setupAnim(null, rotations[0], rotations[1], rotations[2], rotations[3], 0);
	}

	@Override
	public void setupAnim(Entity entity, float rot1, float rot2, float rot3, float rot4, float headPitch) {
		platform.yRot = rot1;
		first_arm.zRot = rot2;
		second_arm.zRot = rot3;
		joint_3.zRot = rot4;
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		poseStack.pushPose();
		platform.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		bb_main.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		poseStack.popPose();
	}
}