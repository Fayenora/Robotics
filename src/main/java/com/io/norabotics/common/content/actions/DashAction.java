package com.io.norabotics.common.content.actions;

import com.io.norabotics.definitions.ModMobEffects;
import com.io.norabotics.definitions.robotics.ModActions;
import com.mojang.serialization.Codec;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.function.Function;

@MethodsReturnNonnullByDefault
public class DashAction implements IAction {

    public enum DashDirection implements StringRepresentable, Function<LivingEntity, Vec3> {
        LOOK(Entity::getLookAngle),
        UP(living -> new Vec3(0, 1, 0));

        private final Function<LivingEntity, Vec3> dir;

        DashDirection(Function<LivingEntity, Vec3> dir) {
            this.dir = dir;
        }

        @Override
        public String getSerializedName() {
            return toString().toLowerCase();
        }

        @Override
        public Vec3 apply(LivingEntity entity) {
            return dir.apply(entity);
        }
    }

    private final String name;
    private final TextColor color;
    private final int impactStrength;
    private final float force;
    private final DashDirection direction;

    public DashAction(String name, TextColor color, int impactStrength, float force, DashDirection direction) {
        this.name = name;
        this.color = color;
        this.impactStrength = impactStrength;
        this.force = force;
        this.direction = direction;
    }

    @Override
    public boolean execute(LivingEntity caster, int duration) {
        if(caster.level().isClientSide || !caster.isAlive()) {
            return false;
        }
        caster.addDeltaMovement(direction.apply(caster).scale(force));
        caster.addEffect(new MobEffectInstance(ModMobEffects.IMPACTFUL.get(), duration, impactStrength, false, false, false));
        return true;
    }

    @Override
    public Codec<? extends IAction> codec() {
        return ModActions.DASH.get();
    }

    public int getImpactStrength() {
        return impactStrength;
    }

    public float getForce() {
        return force;
    }

    public DashDirection getDirection() {
        return direction;
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
