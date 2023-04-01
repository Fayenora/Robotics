package com.ignis.igrobotics.common.entity.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;

import java.util.Iterator;

public class QuickMoveToBlock extends MoveToBlockGoal {

    public QuickMoveToBlock(PathfinderMob mob, BlockPos target) {
        super(mob, target);

        //There can only be one task of this type be applied at once
        Iterator<WrappedGoal> entries = mob.targetSelector.getAvailableGoals().iterator();
        while(entries.hasNext()) {
            Goal goal = entries.next().getGoal();
            if(goal instanceof QuickMoveToBlock) {
                entries.remove();
            }
        }
    }

    @Override
    public void start() {
        super.start();
        maxStayTicks = 200;
    }
}
