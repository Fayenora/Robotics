package com.ignis.igrobotics.common.perks;

import com.ignis.igrobotics.core.SimpleDataManager;
import com.ignis.igrobotics.core.capabilities.perks.Perk;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Mob;

public class PerkConnexiom extends Perk {
    public PerkConnexiom(String name) {
        super(name);
    }

    @Override
    public float damageEntity(int level, Mob robot, DamageSource dmgSource, float damage, SimpleDataManager values) {
        return super.damageEntity(level, robot, dmgSource, damage, values);
    }
}
