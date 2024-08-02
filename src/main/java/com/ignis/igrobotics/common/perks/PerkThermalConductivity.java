package com.ignis.igrobotics.common.perks;

import com.ignis.igrobotics.core.SimpleDataManager;
import com.ignis.igrobotics.core.capabilities.perks.Perk;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Mob;

public class PerkThermalConductivity extends Perk {
    public PerkThermalConductivity(String name) {
        super(name, 1);
    }

    @Override
    public float damageEntity(int level, Mob robot, DamageSource dmgSource, float damage, SimpleDataManager values) {
        if(dmgSource.is(DamageTypeTags.IS_FIRE)) {
            robot.invulnerableTime += 50;
        }
        return super.damageEntity(level, robot, dmgSource, damage, values);
    }
}
