package com.ignis.igrobotics.common.entity.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

/**
 * Adaptation of {@link net.minecraft.world.entity.ai.goal.MoveToBlockGoal}
 * @author Nathan
 *
 */
public class MoveToBlockGoal extends Goal {

    protected final Mob mob;
    /** Controls task execution delay */
    protected int nextStartTick;
    protected int tryTicks;
    protected int maxStayTicks;
    /** Block to move to */
    protected BlockPos blockPos;
    private boolean reachedTarget;

    public MoveToBlockGoal(Mob pMob, BlockPos target) {
        this.mob = pMob;
        this.blockPos = target;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.JUMP));
    }

    public boolean canUse() {
        if (this.nextStartTick <= 0) {
            this.nextStartTick = 200;

            return true;
        }
        --this.nextStartTick;
        return false;
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    public boolean canContinueToUse() {
        return this.tryTicks >= -this.maxStayTicks && this.tryTicks <= 1200;
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void start() {
        double speed = mob.getAttributeValue(Attributes.MOVEMENT_SPEED);
        this.mob.getNavigation().moveTo(this.blockPos.getX() + 0.5D, this.blockPos.getY() + 1, this.blockPos.getZ() + 0.5D, speed);
        this.tryTicks = 0;
        this.maxStayTicks = this.mob.getRandom().nextInt(this.mob.getRandom().nextInt(1200) + 1200) + 1200;
    }

    public double acceptedDistance() {
        return 1.0D;
    }

    protected BlockPos getMoveToTarget() {
        return this.blockPos.above();
    }

    public boolean requiresUpdateEveryTick() {
        return true;
    }

    /**
     * Keep ticking a continuous task that has already been started
     */
    public void tick() {
        BlockPos blockpos = this.getMoveToTarget();
        if (!blockpos.closerToCenterThan(this.mob.position(), this.acceptedDistance())) {
            this.reachedTarget = false;
            ++this.tryTicks;
            if (this.shouldRecalculatePath()) {
                double speed = mob.getAttributeValue(Attributes.MOVEMENT_SPEED);
                this.mob.getNavigation().moveTo(blockpos.getX() + 0.5D, blockpos.getY(), blockpos.getZ() + 0.5D, speed);
            }
        } else {
            this.reachedTarget = true;
            --this.tryTicks;
        }

    }

    public boolean shouldRecalculatePath() {
        return this.tryTicks % 40 == 0;
    }

    public boolean hasReachedTarget() {
        return reachedTarget;
    }

}