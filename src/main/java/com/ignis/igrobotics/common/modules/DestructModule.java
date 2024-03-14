package com.ignis.igrobotics.common.modules;

import com.ignis.igrobotics.core.capabilities.ModCapabilities;
import net.minecraft.world.entity.LivingEntity;

public class DestructModule implements IModuleAction {

    private final float radius, damage;

    public DestructModule(float radius, float damage) {
        this.radius = radius;
        this.damage = damage;
    }

    @Override
    public boolean execute(LivingEntity caster, int duration) {
        if(!caster.isAlive()) return false;
        caster.getCapability(ModCapabilities.ROBOT).ifPresent(robot -> {
            robot.igniteExplosion(10, 8);
        });
        return true;
    }
}
