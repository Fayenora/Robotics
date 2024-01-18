package com.ignis.igrobotics.common.modules;

import net.minecraft.world.entity.LivingEntity;

/**
 * This enum bundles all module actions and gives them an identifier
 */
public enum ModuleActions {
    NONE(new NoAction()),
    TELEPORT(new EnderModule());

    private final IModuleAction action;

    ModuleActions(IModuleAction action) {
        this.action = action;
    }

    public boolean execute(LivingEntity caster, int energyCost, int duration) {
        return action.execute(caster, energyCost, duration);
    }

    static class NoAction implements IModuleAction {
        @Override
        public boolean execute(LivingEntity caster, int energyCost, int duration) {
            return true;
        }
    }
}