package com.ignis.igrobotics.common.modules;

import net.minecraft.world.entity.LivingEntity;

public interface IModuleAction {

    IModuleAction NO_ACTION = (caster, duration) -> true;
    boolean execute(LivingEntity caster, int duration);
}
