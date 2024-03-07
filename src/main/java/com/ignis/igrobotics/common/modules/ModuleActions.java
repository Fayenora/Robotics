package com.ignis.igrobotics.common.modules;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * This enum bundles all module actions and gives them an identifier
 */
public enum ModuleActions {
    NONE(new NoAction()),
    TELEPORT(new EnderModule()),
    REINFORCE(new MobEffectModule(  new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 0, 2),
                                    new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 0, 3))),
    STEALTH(new MobEffectModule(new MobEffectInstance(MobEffects.INVISIBILITY))),
    DASH(new DashModule(0, 0.5f, Entity::getLookAngle)),
    JUMP(new DashModule(1, 1, living -> new Vec3(0, 1, 0)));

    private final IModuleAction action;

    ModuleActions(IModuleAction action) {
        this.action = action;
    }

    public boolean execute(LivingEntity caster, int duration) {
        return action.execute(caster, duration);
    }

    static class NoAction implements IModuleAction {
        @Override
        public boolean execute(LivingEntity caster, int duration) {
            return true;
        }
    }
}