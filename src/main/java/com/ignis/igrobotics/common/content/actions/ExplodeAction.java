package com.ignis.igrobotics.common.content.actions;

import com.ignis.igrobotics.common.capabilities.ModCapabilities;
import com.ignis.igrobotics.definitions.ModActions;
import com.mojang.serialization.Codec;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class ExplodeAction implements IAction {

    private final String name;
    private final TextColor color;
    private final float radius, damage;

    public ExplodeAction(String name, TextColor color, float radius, float damage) {
        this.name = name;
        this.color = color;
        this.radius = radius;
        this.damage = damage;
    }

    @Override
    public boolean execute(LivingEntity caster, int duration) {
        Level level = caster.level();
        if(level.isClientSide || !caster.isAlive()) {
            return false;
        }
        caster.getCapability(ModCapabilities.ROBOT).ifPresent(robot -> {
            robot.igniteExplosion(damage, radius);
        });
        return true;
    }

    @Override
    public Codec<? extends IAction> codec() {
        return ModActions.SELF_DESTRUCT.get();
    }

    public float getRadius() {
        return radius;
    }

    public float getDamage() {
        return damage;
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
