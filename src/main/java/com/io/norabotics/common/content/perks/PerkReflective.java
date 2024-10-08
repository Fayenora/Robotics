package com.io.norabotics.common.content.perks;

import com.io.norabotics.common.capabilities.impl.perk.Perk;
import com.io.norabotics.common.helpers.types.SimpleDataManager;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Mob;

import java.util.Objects;

public class PerkReflective extends Perk {

    public static final float CHANCE_PER_LEVEL = 0.02f;

    public PerkReflective(String name) {
        super(name);
    }

    @Override
    public float onDamage(int level, Mob robot, DamageSource dmgSource, float damage, SimpleDataManager values) {
        if(dmgSource.getEntity() != null && Objects.equals(dmgSource.getDirectEntity(), dmgSource.getEntity()) && Math.random() > CHANCE_PER_LEVEL * level) {
            dmgSource.getEntity().hurt(dmgSource, damage);
            return 0;
        }
        return super.onDamage(level, robot, dmgSource, damage, values);
    }
}
