package com.ignis.igrobotics.common.perks;

import com.ignis.igrobotics.core.SimpleDataManager;
import com.ignis.igrobotics.core.capabilities.perks.Perk;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;

public class PerkModuleBuff extends Perk {
    public PerkModuleBuff(String name) {
        super(name);
    }

    @Override
    public void onModuleActivated(int level, Mob entity, SimpleDataManager values) {
        entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, level - 1));
        super.onModuleActivated(level, entity, values);
    }
}
