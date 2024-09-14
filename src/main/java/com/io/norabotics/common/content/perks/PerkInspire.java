package com.io.norabotics.common.content.perks;

import com.google.common.base.Predicates;
import com.io.norabotics.common.capabilities.IRobot;
import com.io.norabotics.common.capabilities.ModCapabilities;
import com.io.norabotics.common.capabilities.impl.perk.Perk;
import com.io.norabotics.common.helpers.types.SimpleDataManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;

import java.util.Collection;
import java.util.Optional;

public class PerkInspire extends Perk {
    public PerkInspire(String name) {
        super(name);
    }

    @Override
    public float onDamage(int level, Mob robot, DamageSource dmgSource, float damage, SimpleDataManager values) {
        if(!(robot.level() instanceof ServerLevel serverLevel)) return super.onDamage(level, robot, dmgSource, damage, values);
        if(robot.getCapability(ModCapabilities.ROBOT).isPresent()) {
            IRobot iRobot = robot.getCapability(ModCapabilities.ROBOT).resolve().get();
            if(iRobot.hasOwner()) {
                Collection<Entity> allies = alliesInArea(robot, 8, iRobot.getOwner(), Predicates.alwaysTrue());
                Optional<Entity> ally = allies.stream().skip((long) (allies.size() * Math.random())).findFirst();
                if(ally.isPresent() && ally.get() instanceof Mob mob) {
                    mob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20 * 30, level - 1));
                    mob.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20 * 30, level - 1));
                }
                if(serverLevel.getEntity(iRobot.getOwner()) instanceof Mob mob) {
                    mob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20 * 30, level - 1));
                    mob.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20 * 30, level - 1));
                }
                robot.addEffect(new MobEffectInstance(MobEffects.GLOWING));
            }
        }
        return super.onDamage(level, robot, dmgSource, damage, values);
    }
}
