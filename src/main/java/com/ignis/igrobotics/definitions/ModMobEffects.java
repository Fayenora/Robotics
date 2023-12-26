package com.ignis.igrobotics.definitions;

import com.ignis.igrobotics.Robotics;
import com.ignis.igrobotics.common.MobEffectKnockbackResistance;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class ModMobEffects {

    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, Robotics.MODID);

    public static final RegistryObject<MobEffect> KNOCKBACK_RESISTANCE = register("knockback_resistance", MobEffectKnockbackResistance::new);

    private static RegistryObject<MobEffect> register(String name, Supplier<MobEffect> effect) {
        return EFFECTS.register(name, effect);
    }
}
