package com.ignis.igrobotics.common.perks;

import com.ignis.igrobotics.core.SimpleDataManager;
import com.ignis.igrobotics.core.capabilities.perks.Perk;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

public class PerkArmorShred extends Perk {
    public PerkArmorShred(String name) {
        super(name);
    }

    @Override
    public float onAttack(int level, Mob attacker, Entity toAttack, SimpleDataManager values) {
        if(toAttack instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, level - 1));
        }
        return super.onAttack(level, attacker, toAttack, values);
    }
}
