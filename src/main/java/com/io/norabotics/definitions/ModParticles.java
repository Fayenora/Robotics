package com.io.norabotics.definitions;

import com.io.norabotics.Robotics;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@SuppressWarnings("unused")
public class ModParticles {

    public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, Robotics.MODID);

    public static final RegistryObject<SimpleParticleType> SPARK = PARTICLES.register("spark",  () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> FLARE = PARTICLES.register("flare",  () -> new SimpleParticleType(false));
}
