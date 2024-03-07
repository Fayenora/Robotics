package com.ignis.igrobotics.common.modules;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * This enum bundles all module actions and gives them an identifier
 */
public enum ModuleActions {
    NONE(new NoAction(), TextColor.fromRgb(0)),
    TELEPORT(new EnderModule(), TextColor.fromLegacyFormat(ChatFormatting.DARK_PURPLE)),
    REINFORCE(new MobEffectModule(  new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 0, 2),
                                    new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 0, 3)),
            TextColor.fromLegacyFormat(ChatFormatting.GRAY)),
    STEALTH(new MobEffectModule(new MobEffectInstance(MobEffects.INVISIBILITY)), TextColor.fromLegacyFormat(ChatFormatting.DARK_GRAY)),
    DASH(new DashModule(0, 0.5f, Entity::getLookAngle), TextColor.fromLegacyFormat(ChatFormatting.GREEN)),
    JUMP(new DashModule(1, 1, living -> new Vec3(0, 1, 0)), TextColor.fromLegacyFormat(ChatFormatting.GREEN));

    private final IModuleAction action;
    public final TextColor color;

    ModuleActions(IModuleAction action, TextColor color) {
        this.action = action;
        this.color = color;
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