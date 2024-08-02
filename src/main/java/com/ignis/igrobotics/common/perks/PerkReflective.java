package com.ignis.igrobotics.common.perks;

import com.ignis.igrobotics.core.SimpleDataManager;
import com.ignis.igrobotics.core.capabilities.perks.Perk;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Mob;

public class PerkReflective extends Perk {

    public static final float CHANCE_PER_LEVEL = 0.02f;

    public PerkReflective(String name) {
        super(name);
    }

    @Override
    public float damageEntity(int level, Mob robot, DamageSource dmgSource, float damage, SimpleDataManager values) {
        if(dmgSource.getEntity() != null && Math.random() > CHANCE_PER_LEVEL * level) {
            dmgSource.getEntity().hurt(dmgSource, damage);
            return 0;
        }
        return super.damageEntity(level, robot, dmgSource, damage, values);
    }
}
