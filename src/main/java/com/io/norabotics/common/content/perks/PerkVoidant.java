package com.io.norabotics.common.content.perks;

import com.io.norabotics.common.capabilities.impl.perk.Perk;
import com.io.norabotics.common.helpers.types.SimpleDataManager;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Mob;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;

public class PerkVoidant extends Perk {

    public static final Predicate<MobEffectInstance> REMOVABLE = e -> !e.isInfiniteDuration() && !e.isAmbient() && e.isVisible() && e.getEffect().isBeneficial();

    public PerkVoidant(String name) {
        super(name, 1);
    }

    @Override
    public float onDamage(int level, Mob robot, DamageSource dmgSource, float damage, SimpleDataManager values) {
        if(dmgSource.getEntity() instanceof Mob mob) {
            Collection<MobEffectInstance> effectsToReduce = mob.getActiveEffects().stream().filter(REMOVABLE).toList();
            Optional<MobEffectInstance> effectToRemove = effectsToReduce.stream().skip((long) (effectsToReduce.size() * Math.random())).findFirst();
            if(effectToRemove.isEmpty()) return super.onDamage(level, robot, dmgSource, damage, values);
            MobEffectInstance e = effectToRemove.get();
            MobEffectInstance copy = new MobEffectInstance(e.getEffect(), e.mapDuration(i -> i - 30 * 20), e.getAmplifier(), e.isAmbient(), e.isVisible(), e.showIcon(), null, e.getFactorData());
            if(copy.getDuration() < 0) {
                mob.removeEffect(copy.getEffect());
            } else mob.forceAddEffect(copy, null);
        }
        return super.onDamage(level, robot, dmgSource, damage, values);
    }
}
