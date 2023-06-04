package com.ignis.igrobotics.client.rendering.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer;

import javax.annotation.Nullable;

@OnlyIn(Dist.CLIENT)
public class HeldItemRenderer<T extends LivingEntity & GeoAnimatable> extends BlockAndItemGeoLayer<T> {

    private static final String LEFT_HAND = "leftArm";
    private static final String RIGHT_HAND = "rightArm";

    public HeldItemRenderer(GeoRenderer<T> renderer) {
        super(renderer);
    }

    @Nullable
    @Override
    protected ItemStack getStackForBone(GeoBone bone, T animatable) {
        return switch (bone.getName()) {
            case LEFT_HAND -> animatable.getMainArm() == HumanoidArm.LEFT ? animatable.getMainHandItem() : animatable.getOffhandItem();
            case RIGHT_HAND -> animatable.getMainArm() == HumanoidArm.RIGHT ? animatable.getMainHandItem() : animatable.getOffhandItem();
            default -> null;
        };
    }

    @Override
    protected ItemDisplayContext getTransformTypeForStack(GeoBone bone, ItemStack stack, T animatable) {
        // Apply the camera transform for the given hand
        return switch (bone.getName()) {
            case LEFT_HAND, RIGHT_HAND -> ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
            default -> ItemDisplayContext.NONE;
        };
    }

    // Do some quick render modifications depending on what the item is
    @Override
    protected void renderStackForBone(PoseStack poseStack, GeoBone bone, ItemStack stack, T animatable, MultiBufferSource bufferSource, float partialTick, int packedLight, int packedOverlay) {
        if (stack == animatable.getMainHandItem()) {
            poseStack.mulPose(Axis.XP.rotationDegrees(-90f));
            poseStack.translate(0.1, 0.125, -0.6);

            if (stack.getItem() instanceof ShieldItem)
                poseStack.translate(0, 0.125, -0.25);
        }
        else if (stack == animatable.getOffhandItem()) {
            poseStack.mulPose(Axis.XP.rotationDegrees(-90f));
            poseStack.translate(-0.1, 0.125, -0.6);

            if (stack.getItem() instanceof ShieldItem) {
                poseStack.translate(0, 0.125, 0.25);
                poseStack.mulPose(Axis.YP.rotationDegrees(180));
            }
        }

        super.renderStackForBone(poseStack, bone, stack, animatable, bufferSource, partialTick, packedLight, packedOverlay);
    }
}
