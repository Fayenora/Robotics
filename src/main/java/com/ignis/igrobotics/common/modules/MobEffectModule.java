package com.ignis.igrobotics.common.modules;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

public class MobEffectModule implements IModuleAction {

    MobEffectInstance[] effects;

    public MobEffectModule(MobEffectInstance... effects) {
        this.effects = effects;
    }

    @Override
    public boolean execute(LivingEntity caster, int duration) {
        if(caster.level().isClientSide || !caster.isAlive()) {
            return false;
        }
        for(MobEffectInstance effect : effects) {
            caster.addEffect(new MobEffectInstance(effect.getEffect(), effect.getAmplifier(), duration, false, false, true));
        }
        return true;
    }
}
