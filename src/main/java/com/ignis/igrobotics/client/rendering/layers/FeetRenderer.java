package com.ignis.igrobotics.client.rendering.layers;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.renderer.GeoRenderer;

/**
 * Necessary as the robot model has no seperate bones for the feet and must render the boots along the legs
 * @param <T>
 */
public class FeetRenderer<T extends LivingEntity & GeoAnimatable> extends ArmorRenderer<T> {
    public FeetRenderer(GeoRenderer geoRenderer) {
        super(geoRenderer);
    }

    @Nullable
    @Override
    protected ItemStack getArmorItemForBone(GeoBone bone, T animatable) {
        return switch(bone.getName()) {
            case LEFT_ARMOR_LEG -> hasLeftLeg ? bootsStack : null;
            case RIGHT_ARMOR_LEG -> hasRightLeg ? bootsStack : null;
            default -> super.getArmorItemForBone(bone, animatable);
        };
    }

    @NotNull
    @Override
    protected EquipmentSlot getEquipmentSlotForBone(GeoBone bone, ItemStack stack, T animatable) {
        return EquipmentSlot.FEET;
    }
}
