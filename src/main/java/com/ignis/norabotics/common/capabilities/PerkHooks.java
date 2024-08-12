package com.ignis.norabotics.common.capabilities;

import com.ignis.norabotics.common.helpers.types.SimpleDataManager;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;

public interface PerkHooks {

    default void onEntityUpdate(int level, Mob entity, SimpleDataManager values) {}

    default void onEntityJump(int level, Mob entity, SimpleDataManager values) {}

    default void onModuleActivated(int level, Mob entity, SimpleDataManager values) {}

    /**
     * Executed when a robot with this perk damages another entity
     * @param level of the perk
     * @param toAttack entity that is attacked
     * @return knockback to add
     */
    default float onAttack(int level, Mob attacker, Entity toAttack, SimpleDataManager values) {
        return 0;
    }

    /**
     * Executed when a robot with this perk gets damaged
     * @param level of the perk
     * @param dmgSource damage source
     * @param damage amount of damage
     * @return adjusted damage
     */
    default float onDamage(int level, Mob robot, DamageSource dmgSource, float damage, SimpleDataManager values) {
        return damage;
    }
}
