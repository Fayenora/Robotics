package com.ignis.igrobotics.common.modules;

import net.minecraft.world.entity.LivingEntity;

public interface IModuleAction {
    boolean execute(LivingEntity caster, int energyCost, int duration);
}
