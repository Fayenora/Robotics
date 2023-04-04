package com.ignis.igrobotics.common.entity.ai;

import com.ignis.igrobotics.common.blockentity.StorageBlockEntity;
import com.ignis.igrobotics.common.blocks.MachineBlock;
import com.ignis.igrobotics.common.blocks.StorageBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

import javax.annotation.Nullable;
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
    @Nullable
    private BlockPos storagePos;
    private boolean reachedTarget;

    public MoveToBlockGoal(Mob mob, BlockPos target) {
        this.mob = mob;
        this.blockPos = determineEnterPosition(mob.level.getBlockState(target), target);
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.JUMP));
    }

    private BlockPos determineEnterPosition(BlockState state, BlockPos pos) {
        if(!StorageBlock.class.isAssignableFrom(state.getBlock().getClass())) return pos;
        this.storagePos = pos;
        return pos.below(state.getValue(StorageBlock.HALF) == DoubleBlockHalf.UPPER ? 2 : 1).relative(state.getValue(MachineBlock.FACING));
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
        if(storagePos != null) {
            if(mob.distanceToSqr(storagePos.getCenter()) < 4) {
                BlockEntity tile = mob.level.getBlockEntity(storagePos);
                if(tile instanceof StorageBlockEntity storage) {
                    storage.enterRobot(mob);
                }
            }
        }
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

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof MoveToBlockGoal moveToBlockGoal)) return false;
        return mob.equals(moveToBlockGoal.mob) && blockPos.equals(moveToBlockGoal.blockPos);
    }
}