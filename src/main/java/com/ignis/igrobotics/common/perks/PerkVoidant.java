package com.ignis.igrobotics.common.perks;

import com.ignis.igrobotics.core.SimpleDataManager;
import com.ignis.igrobotics.core.capabilities.perks.Perk;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Mob;

import java.util.Collection;
import java.util.Optional;

public class PerkVoidant extends Perk {

    public PerkVoidant(String name) {
        super(name, 1);
    }

    @Override
    public float damageEntity(int level, Mob robot, DamageSource dmgSource, float damage, SimpleDataManager values) {
        if(dmgSource.getEntity() instanceof Mob mob) {
            Collection<MobEffectInstance> effectsToReduce = mob.getActiveEffects().stream().filter(e -> !e.isInfiniteDuration() && !e.isAmbient() && e.isVisible()).toList();
            Optional<MobEffectInstance> effectToRemove = effectsToReduce.stream().skip((long) (effectsToReduce.size() * Math.random())).findFirst();
            if(effectToRemove.isEmpty()) return super.damageEntity(level, robot, dmgSource, damage, values);
            MobEffectInstance e = effectToRemove.get();
            MobEffectInstance copy = new MobEffectInstance(e.getEffect(), e.mapDuration(i -> i - 30 * 20), e.getAmplifier(), e.isAmbient(), e.isVisible(), e.showIcon(), null, e.getFactorData());
            mob.forceAddEffect(copy, null);
        }
        return super.damageEntity(level, robot, dmgSource, damage, values);
    }
}
