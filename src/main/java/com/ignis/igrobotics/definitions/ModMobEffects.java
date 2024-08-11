package com.ignis.igrobotics.definitions;

import com.ignis.igrobotics.Robotics;
import com.ignis.igrobotics.common.content.GenericMobEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.DyeColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class ModMobEffects {

    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, Robotics.MODID);

    public static final RegistryObject<MobEffect> KNOCKBACK_RESISTANCE = register("knockback_resistance", () ->
            new GenericMobEffect(MobEffectCategory.BENEFICIAL, 0).addAttributeModifier(Attributes.KNOCKBACK_RESISTANCE, "7107DE5E-7CE8-4030-940E-514C1F160895", 1, AttributeModifier.Operation.ADDITION));
    public static final RegistryObject<MobEffect> IMPACTFUL = register("impactful", () ->
            new GenericMobEffect(MobEffectCategory.BENEFICIAL, 0));
    public static final RegistryObject<MobEffect> ARMOR_SHRED = register("armor_shred", () ->
            new GenericMobEffect(MobEffectCategory.HARMFUL, DyeColor.CYAN.getFireworkColor()).addAttributeModifier(Attributes.ARMOR, "4a2f8ccc-85fe-4309-a040-8ee7a1413db4", -1, AttributeModifier.Operation.ADDITION));

    private static RegistryObject<MobEffect> register(String name, Supplier<MobEffect> effect) {
        return EFFECTS.register(name, effect);
    }
}
