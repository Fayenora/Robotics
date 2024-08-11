package com.ignis.igrobotics.common.content.entity.ai;

import com.ignis.igrobotics.common.helpers.EntityInteractionManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class BreakBlocksGoal extends AbstractMultiBlockGoal {

    private final EntityInteractionManager interactionManager;

    public BreakBlocksGoal(Mob entity, GlobalPos pos1, GlobalPos pos2) {
        super(entity, pos1, pos2);
        interactionManager = new EntityInteractionManager(entity);
    }

    @Override
    protected boolean isValidBlock(Level level, BlockPos pos, ItemStack stack) {
        BlockState state = level.getBlockState(pos);
        if(state.isAir() || state.getDestroySpeed(level, pos) < 0) return false;
        if(!state.requiresCorrectToolForDrops()) return true;
        return stack.isCorrectToolForDrops(state);
    }

    @Override
    protected boolean operateOnBlock(BlockPos pos) {
        return interactionManager.dig(pos, Direction.UP);
    }

    @Override
    public void stop() {
        super.stop();
        interactionManager.cancelDestroyingBlock();
    }
}
