package com.ignis.igrobotics.common.entity.ai;

import com.ignis.igrobotics.common.blockentity.StorageBlockEntity;
import com.ignis.igrobotics.common.blocks.MachineBlock;
import com.ignis.igrobotics.common.blocks.StorageBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
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
    private final DimensionNavigator navigator;
    /** Controls task execution delay */
    protected int nextStartTick;
    protected int tryTicks;
    protected int maxStayTicks;
    /** Block to move to */
    protected GlobalPos blockPos;
    @Nullable
    private GlobalPos storagePos;

    private MoveToBlockGoal(Mob mob) {
        this.mob = mob;
        navigator = new DimensionNavigator(mob, 16, 16, 1);
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.JUMP));
    }

    public MoveToBlockGoal(Mob mob, BlockPos target) {
        this(mob);
        this.blockPos = determineEnterPosition(mob.level.getBlockState(target), GlobalPos.of(mob.level.dimension(), target));
    }

    public MoveToBlockGoal(Mob mob, GlobalPos target) {
        this(mob);
        Level targetDimension = mob.getServer().getLevel(target.dimension());
        if(targetDimension != null) {
            this.blockPos = determineEnterPosition(targetDimension.getBlockState(target.pos()), target);
        } else blockPos = target;
    }

    private GlobalPos determineEnterPosition(BlockState state, GlobalPos pos) {
        if(!StorageBlock.class.isAssignableFrom(state.getBlock().getClass())) return pos;
        this.storagePos = GlobalPos.of(pos.dimension(), pos.pos().below(state.getValue(StorageBlock.HALF) == DoubleBlockHalf.UPPER ? 1 : 0));
        return GlobalPos.of(pos.dimension(), pos.pos().below(state.getValue(StorageBlock.HALF) == DoubleBlockHalf.UPPER ? 2 : 1).relative(state.getValue(MachineBlock.FACING)));
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
        navigator.navigateTo(blockPos);
        this.tryTicks = 0;
        this.maxStayTicks = this.mob.getRandom().nextInt(this.mob.getRandom().nextInt(1200) + 1200) + 1200;
    }

    public double acceptedDistance() {
        return 1.0D;
    }

    public boolean requiresUpdateEveryTick() {
        return true;
    }

    /**
     * Keep ticking a continuous task that has already been started
     */
    public void tick() {
        if(storagePos != null && mob.level.dimension().equals(storagePos.dimension()) && mob.distanceToSqr(storagePos.pos().getCenter()) < 12) {
            BlockEntity tile = mob.level.getBlockEntity(storagePos.pos());
            if(tile instanceof StorageBlockEntity storage) {
                storage.enterStorage(mob);
            }
        }
        if (!hasReachedTarget()) {
            ++this.tryTicks;
            if (this.shouldRecalculatePath()) {
                double speed = mob.getAttributeValue(Attributes.MOVEMENT_SPEED);
                navigator.navigateTo(blockPos);
            }
        } else {
            --this.tryTicks;
        }
    }

    public boolean shouldRecalculatePath() {
        return this.tryTicks % 40 == 0;
    }

    public boolean hasReachedTarget() {
        return mob.level.dimension().equals(blockPos.dimension()) && blockPos.pos().closerToCenterThan(this.mob.position(), this.acceptedDistance());
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof MoveToBlockGoal moveToBlockGoal)) return false;
        return mob.equals(moveToBlockGoal.mob) && blockPos.equals(moveToBlockGoal.blockPos);
    }
}