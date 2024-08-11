package com.ignis.igrobotics.common.content.entity.ai;

import com.ignis.igrobotics.common.helpers.DimensionNavigator;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class ReachAcrossDimensionGoal extends Goal {

    private final DimensionNavigator navigator;
    protected final Mob mob;
    private final float distance;

    protected int tryTicks;
    private LivingEntity target;

    public ReachAcrossDimensionGoal(Mob mob, float distance) {
        setFlags(EnumSet.of(Flag.MOVE));
        this.mob = mob;
        navigator = new DimensionNavigator(mob, 16, 16, 1);
        this.distance = distance;
    }

    @Override
    public void start() {
        navigator.navigateTo(target.level(), target.blockPosition());
        tryTicks = 0;
    }

    @Override
    public void tick() {
        ++this.tryTicks;
        if (this.shouldRecalculatePath()) {
            navigator.navigateTo(target.level(), target.blockPosition());
        }
    }

    protected boolean shouldRecalculatePath() {
        return tryTicks % 40 == 0;
    }

    @Override
    public boolean canUse() {
        target = mob.getTarget();
        return target != null && !hasReachedTarget();
    }

    public boolean hasReachedTarget() {
        return mob.level().dimension().equals(target.level().dimension()) && mob.distanceTo(target) < distance;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof ReachAcrossDimensionGoal reachAcrossDimensionGoal)) return false;
        return mob.equals(reachAcrossDimensionGoal.mob) && distance == reachAcrossDimensionGoal.distance;
    }
}
