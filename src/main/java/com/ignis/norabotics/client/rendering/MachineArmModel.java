package com.ignis.norabotics.client.rendering;

import com.ignis.norabotics.Robotics;
import com.ignis.norabotics.common.helpers.util.MathUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.Entity;

public class MachineArmModel<T extends Entity> extends EntityModel<T> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Robotics.rl("machine_arm"), "main");
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
				.texOffs(14, 45).addBox(-3.0F, -4.0F, 2.0F, 6.0F, 6.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(14, 38).addBox(-3.0F, -4.0F, -3.0F, 6.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(8.0F, 16.0F, 8.0F));

		PartDefinition first_arm = platform.addOrReplaceChild("first_arm", CubeListBuilder.create().texOffs(36, 34).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 23.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition rotation_joint_1 = first_arm.addOrReplaceChild("rotation_joint_1", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, 21.0F, -2.0F, 4.0F, 7.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition second_arm = rotation_joint_1.addOrReplaceChild("second_arm", CubeListBuilder.create().texOffs(48, 0).addBox(-3.0F, -2.0F, -3.0F, 6.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(30, 32).addBox(-16.0F, 1.0F, 2.0F, 13.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(30, 30).addBox(-16.0F, -2.0F, 2.0F, 13.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(28, 38).addBox(-11.0F, -1.0F, 2.0F, 3.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(48, 10).addBox(-21.0F, -2.0F, 2.0F, 5.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(0, 11).addBox(-3.0F, -2.0F, 2.0F, 6.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(48, 5).addBox(-21.0F, -2.0F, -3.0F, 5.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(0, 31).addBox(-11.0F, -1.0F, -3.0F, 3.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(30, 28).addBox(-16.0F, -2.0F, -3.0F, 13.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(30, 26).addBox(-16.0F, 1.0F, -3.0F, 13.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 25.0F, 0.0F));

		PartDefinition joint_3 = second_arm.addOrReplaceChild("joint_3", CubeListBuilder.create().texOffs(0, 38).addBox(-1.0F, -4.0F, -2.0F, 3.0F, 7.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(0, 26).addBox(-1.0F, -7.0F, 0.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(12, 0).addBox(-1.0F, -9.0F, 1.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(0, 0).addBox(-1.0F, -6.0F, -2.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(0, 34).addBox(0.0F, -5.0F, -2.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-19.0F, 0.0F, 0.0F));

		PartDefinition bb_main = partdefinition.addOrReplaceChild("bb_main", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, 0.0F, -8.0F, 16.0F, 10.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(8.0F, 0.0F, 8.0F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	public void setPlatformRotation(float rot1, float rot2, float rot4, float rot5) {
		platform.yRot = rot1;
		first_arm.zRot = rot2 < Math.toRadians(180) ? MathUtil.clamp(0, rot2, (float) Math.toRadians(50)) : MathUtil.clamp((float) Math.toRadians(310), rot2, (float) Math.toRadians(360));
		second_arm.zRot = rot4 < Math.toRadians(180) ? MathUtil.clamp(0, rot4, (float) Math.toRadians(50)) : MathUtil.clamp((float) Math.toRadians(310), rot4, (float) Math.toRadians(360));
		joint_3.zRot = rot5;
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		platform.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		bb_main.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}