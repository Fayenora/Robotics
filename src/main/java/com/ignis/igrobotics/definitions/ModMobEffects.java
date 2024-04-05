package com.ignis.igrobotics.definitions;

import com.ignis.igrobotics.Robotics;
import com.ignis.igrobotics.common.GenericMobEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class ModMobEffects {

    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, Robotics.MODID);

    private static final Supplier<MobEffect> knockbackresistance = () -> new GenericMobEffect(MobEffectCategory.BENEFICIAL, 0).addAttributeModifier(Attributes.KNOCKBACK_RESISTANCE, "7107DE5E-7CE8-4030-940E-514C1F160895", 1, AttributeModifier.Operation.ADDITION);

    public static final RegistryObject<MobEffect> KNOCKBACK_RESISTANCE = register("knockback_resistance", knockbackresistance);
    public static final RegistryObject<MobEffect> IMPACTFUL = register("impactful", () -> new GenericMobEffect(MobEffectCategory.BENEFICIAL, 0));

    private static RegistryObject<MobEffect> register(String name, Supplier<MobEffect> effect) {
        return EFFECTS.register(name, effect);
    }
}
