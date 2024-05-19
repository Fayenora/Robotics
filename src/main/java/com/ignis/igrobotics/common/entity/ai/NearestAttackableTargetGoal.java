package com.ignis.igrobotics.common.entity.ai;

import com.ignis.igrobotics.common.CommonSetup;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

public class NearestAttackableTargetGoal<T extends LivingEntity> extends net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal<T> {

    public NearestAttackableTargetGoal(Mob mob, EntityType<T> type) {
        this(mob, (Class<T>) CommonSetup.allLivingEntities.get(type).getClass());
    }

    public NearestAttackableTargetGoal(Mob pMob, Class<T> targetType) {
        this(pMob, targetType, true, false);
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
