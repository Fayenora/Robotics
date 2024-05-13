package com.ignis.igrobotics.common.perks.modules;

import com.ignis.igrobotics.core.SimpleDataManager;
import com.ignis.igrobotics.core.capabilities.perks.Perk;
import com.ignis.igrobotics.definitions.ModMobEffects;
import com.ignis.igrobotics.integration.config.RoboticsConfig;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;

public class PerkFist extends Perk {

    public PerkFist(String name) {
        super(name, 1);
    }

    @Override
    public float attackEntityAsMob(int level, Mob attacker, Entity toAttack, SimpleDataManager values) {
        if(toAttack instanceof Mob mob) {
            mob.knockback(attacker.getAttributeValue(Attributes.ATTACK_KNOCKBACK), 1, 1);
            mob.addDeltaMovement(new Vec3(0, RoboticsConfig.general.fistKnockUp.get().floatValue(), 0));
            mob.addEffect(new MobEffectInstance(ModMobEffects.KNOCKBACK_RESISTANCE.get(), 1));
        }
        return super.attackEntityAsMob(level, attacker, toAttack, values);
    }
}
