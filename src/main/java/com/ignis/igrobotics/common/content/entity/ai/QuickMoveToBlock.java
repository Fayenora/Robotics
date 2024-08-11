package com.ignis.igrobotics.common.content.entity.ai;

import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.WrappedGoal;

public class QuickMoveToBlock extends MoveToBlockGoal {

    protected int maxStayTicks;

    public QuickMoveToBlock(Mob mob, GlobalPos target) {
        super(mob, target);
        clearExistingInstances(); //There can only be one task of this type be applied at once
    }

    private void clearExistingInstances() {
        mob.goalSelector.getRunningGoals().filter(goal -> goal.getGoal() instanceof QuickMoveToBlock).forEach(WrappedGoal::stop);
        mob.targetSelector.getRunningGoals().filter(goal -> goal.getGoal() instanceof QuickMoveToBlock).forEach(WrappedGoal::stop);
        mob.goalSelector.removeAllGoals(goal -> goal instanceof QuickMoveToBlock);
        mob.targetSelector.removeAllGoals(goal -> goal instanceof QuickMoveToBlock);
    }

    @Override
    public boolean canContinueToUse() {
        return super.canContinueToUse() && this.tryTicks >= -this.maxStayTicks;
    }

    @Override
    public void start() {
        super.start();
        maxStayTicks = 200;
    }
}
