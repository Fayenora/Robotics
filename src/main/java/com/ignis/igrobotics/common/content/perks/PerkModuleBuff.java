package com.ignis.igrobotics.common.content.perks;

import com.ignis.igrobotics.common.capabilities.impl.perk.Perk;
import com.ignis.igrobotics.common.helpers.types.SimpleDataManager;
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
