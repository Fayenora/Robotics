package com.ignis.norabotics.common.content.entity.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

//TODO Place blocks regardless in which hand
public class PlaceBlocksGoal extends AbstractMultiBlockGoal {

    public PlaceBlocksGoal(Mob entity, GlobalPos pos1, GlobalPos pos2) {
        super(entity, pos1, pos2);
    }

    @Override
    protected boolean isValidBlock(Level level, BlockPos pos, ItemStack stack) {
        return level.getBlockState(pos).canBeReplaced(getDefaultPlaceContext(pos));
    }

    @Override
    protected boolean operateOnBlock(BlockPos pos) {
        if(!(entity.getMainHandItem().getItem() instanceof BlockItem blockItem)) return false;
        InteractionResult result =  blockItem.place(getDefaultPlaceContext(pos));
        entity.swing(InteractionHand.MAIN_HAND);
        return result.consumesAction();
    }

    @Override
    public boolean canUse() {
        return entity.getMainHandItem().getItem() instanceof BlockItem  && super.canUse();
    }

    protected BlockPlaceContext getDefaultPlaceContext(BlockPos pos) {
        BlockHitResult blockHit = new BlockHitResult(Vec3.ZERO, Direction.UP, pos, true);
        return new BlockPlaceContext(entity.level(), null, InteractionHand.MAIN_HAND, entity.getMainHandItem(), blockHit);
    }
}
