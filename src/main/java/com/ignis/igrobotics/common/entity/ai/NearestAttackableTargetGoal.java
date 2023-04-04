package com.ignis.igrobotics.common.entity.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

public class NearestAttackableTargetGoal<T extends LivingEntity> extends net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal<T> {

    public NearestAttackableTargetGoal(Mob pMob, Class<T> pTargetType) {
        this(pMob, pTargetType, true, false);
    }

    public NearestAttackableTargetGoal(Mob pMob, Class<T> pTargetType, boolean pMustSee, boolean pMustReach) {
        super(pMob, pTargetType, pMustSee, pMustReach);
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof NearestAttackableTargetGoal<?> attackGoal)) return false;
        return targetType.equals(attackGoal.targetType);
    }
}
