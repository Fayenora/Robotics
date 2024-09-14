package com.io.norabotics.common.content.perks;

import com.io.norabotics.common.capabilities.impl.perk.Perk;
import com.io.norabotics.common.helpers.types.SimpleDataManager;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Mob;

public class PerkThermalConductivity extends Perk {
    public PerkThermalConductivity(String name) {
        super(name, 1);
    }

    @Override
    public float onDamage(int level, Mob robot, DamageSource dmgSource, float damage, SimpleDataManager values) {
        if(dmgSource.is(DamageTypeTags.IS_FIRE)) {
            robot.invulnerableTime += 50;
        }
        return super.onDamage(level, robot, dmgSource, damage, values);
    }
}
