package com.io.norabotics.common.content.perks;

import com.io.norabotics.common.capabilities.impl.perk.Perk;
import com.io.norabotics.common.helpers.types.SimpleDataManager;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Mob;

public class PerkConnexiom extends Perk {
    public PerkConnexiom(String name) {
        super(name);
    }

    @Override
    public float onDamage(int level, Mob robot, DamageSource dmgSource, float damage, SimpleDataManager values) {
        return super.onDamage(level, robot, dmgSource, damage, values);
    }
}
