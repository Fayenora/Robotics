package com.ignis.norabotics.common.content.actions;

import com.ignis.norabotics.definitions.robotics.ModActions;
import com.mojang.serialization.Codec;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

import java.util.Arrays;
import java.util.List;

public class MobEffectAction implements IAction {

    String name;
    List<MobEffectInstance> effects;
    TextColor color;

    public MobEffectAction(String name, TextColor color, MobEffectInstance... effects) {
        this(name, color, Arrays.stream(effects).toList());
    }

    public MobEffectAction(String name, TextColor color, List<MobEffectInstance> effects) {
        this.name = name;
        this.effects = effects;
        this.color = color;
    }

    @Override
    public boolean execute(LivingEntity caster, int duration) {
        if(caster.level().isClientSide || !caster.isAlive()) {
            return false;
        }
        for(MobEffectInstance effect : effects) {
            caster.addEffect(new MobEffectInstance(effect.getEffect(), effect.getAmplifier(), duration, false, false, true));
        }
        return true;
    }

    @Override
    public Codec<? extends IAction> codec() {
        return ModActions.MOB_EFFECT.get();
    }

    public List<MobEffectInstance> getEffects() {
        return effects;
    }

    @Override
    public TextColor getColor() {
        return color;
    }

    @Override
    public String toString() {
        return name;
    }
}
