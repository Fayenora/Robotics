package com.io.norabotics.common.content.perks;

import com.io.norabotics.common.capabilities.impl.perk.Perk;
import com.io.norabotics.common.helpers.types.SimpleDataManager;
import com.io.norabotics.definitions.ModMobEffects;
import net.minecraft.world.effect.MobEffectInstance;
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
            living.addEffect(new MobEffectInstance(ModMobEffects.ARMOR_SHRED.get(), 200, level - 1));
        }
        return super.onAttack(level, attacker, toAttack, values);
    }
}
