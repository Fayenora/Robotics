package com.ignis.igrobotics.common.content.perks;

import com.ignis.igrobotics.common.capabilities.impl.perk.Perk;
import com.ignis.igrobotics.common.helpers.types.SimpleDataManager;
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
