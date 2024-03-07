package com.ignis.igrobotics.common.modules;

import com.ignis.igrobotics.definitions.ModMobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.function.Function;

public class DashModule implements IModuleAction {

    private final int impactStrength;
    private final float force;
    private final Function<LivingEntity, Vec3> direction;

    public DashModule(int impactStrength, float force, Function<LivingEntity, Vec3> direction) {
        this.impactStrength = impactStrength;
        this.force = force;
        this.direction = direction;
    }

    @Override
    public boolean execute(LivingEntity caster, int duration) {
        if(caster.level.isClientSide || !caster.isAlive()) {
            return false;
        }
        caster.addDeltaMovement(direction.apply(caster).scale(force));
        caster.addEffect(new MobEffectInstance(ModMobEffects.IMPACTFUL.get(), duration, impactStrength, false, false, false));
        return true;
    }
}
