package com.ignis.igrobotics.client.rendering.layers;

import com.ignis.igrobotics.common.capabilities.ModCapabilities;
import com.ignis.igrobotics.common.robot.EnumRobotPart;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.ItemArmorGeoLayer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@OnlyIn(Dist.CLIENT)
public class ArmorRenderer<T extends LivingEntity & GeoAnimatable> extends ItemArmorGeoLayer<T> {

    protected static final String LEFT_BOOT = "leftFoot";
    protected static final String RIGHT_BOOT = "rightFoot";
    protected static final String LEFT_ARMOR_LEG = "leftLeg";
    protected static final String RIGHT_ARMOR_LEG = "rightLeg";
    protected static final String CHESTPLATE = "body";
    protected static final String RIGHT_SLEEVE = "rightArm";
    protected static final String LEFT_SLEEVE = "leftArm";
    protected static final String HELMET = "head";

    boolean hasLeftLeg, hasRightLeg, hasLeftArm, hasRightArm;

    public ArmorRenderer(GeoRenderer<T> geoRenderer) {
        super(geoRenderer);
    }

    @Override
    public void preRender(PoseStack poseStack, T animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        animatable.getCapability(ModCapabilities.PARTS).ifPresent(parts -> {
            hasLeftArm = parts.hasBodyPart(EnumRobotPart.LEFT_ARM);
            hasRightArm = parts.hasBodyPart(EnumRobotPart.RIGHT_ARM);
            hasLeftLeg = parts.hasBodyPart(EnumRobotPart.LEFT_LEG);
            hasRightLeg = parts.hasBodyPart(EnumRobotPart.RIGHT_LEG);
        });
        super.preRender(poseStack, animatable, bakedModel, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);
    }

    @Nullable
    @Override
    protected ItemStack getArmorItemForBone(GeoBone bone, T animatable) {
        // Return the items relevant to the bones being rendered for additional rendering
        return switch (bone.getName()) {
            case LEFT_BOOT -> hasLeftLeg ? bootsStack : null;
            case RIGHT_BOOT -> hasRightLeg ? bootsStack : null;
            case LEFT_ARMOR_LEG -> hasLeftLeg ? leggingsStack : null;
            case RIGHT_ARMOR_LEG -> hasRightLeg ? leggingsStack : null;
            case CHESTPLATE -> chestplateStack;
            case RIGHT_SLEEVE -> hasRightArm ? chestplateStack : null;
            case LEFT_SLEEVE -> hasLeftArm ? chestplateStack : null;
            case HELMET -> helmetStack;
            default -> null;
        };
    }

    // Return the equipment slot relevant to the bone we're using
    @Nonnull
    @Override
    protected EquipmentSlot getEquipmentSlotForBone(GeoBone bone, ItemStack stack, T animatable) {
        return switch (bone.getName()) {
            case LEFT_BOOT, RIGHT_BOOT -> EquipmentSlot.FEET;
            case LEFT_ARMOR_LEG, RIGHT_ARMOR_LEG -> EquipmentSlot.LEGS;
            case RIGHT_SLEEVE -> animatable.getMainArm() == HumanoidArm.RIGHT ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
            case LEFT_SLEEVE -> animatable.getMainArm() == HumanoidArm.LEFT ? EquipmentSlot.OFFHAND : EquipmentSlot.MAINHAND;
            case CHESTPLATE -> EquipmentSlot.CHEST;
            case HELMET -> EquipmentSlot.HEAD;
            default -> super.getEquipmentSlotForBone(bone, stack, animatable);
        };
    }

    // Return the ModelPart responsible for the armor pieces we want to render
    @Nonnull
    @Override
    protected ModelPart getModelPartForBone(GeoBone bone, EquipmentSlot slot, ItemStack stack, T animatable, HumanoidModel<?> baseModel) {
        return switch (bone.getName()) {
            case LEFT_BOOT, LEFT_ARMOR_LEG -> baseModel.leftLeg;
            case RIGHT_BOOT, RIGHT_ARMOR_LEG -> baseModel.rightLeg;
            case RIGHT_SLEEVE -> baseModel.rightArm;
            case LEFT_SLEEVE -> baseModel.leftArm;
            case CHESTPLATE -> baseModel.body;
            case HELMET -> baseModel.head;
            default -> super.getModelPartForBone(bone, slot, stack, animatable, baseModel);
        };
    }

    @Override
    protected void prepModelPartForRender(PoseStack poseStack, GeoBone bone, ModelPart sourcePart) {
        super.prepModelPartForRender(poseStack, bone, sourcePart);
        sourcePart.xRot = 0;
        sourcePart.yRot = 0;
        sourcePart.zRot = 0;
    }
}
