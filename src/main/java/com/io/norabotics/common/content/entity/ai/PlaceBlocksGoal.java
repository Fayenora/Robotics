package com.io.norabotics.common.content.entity.ai;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
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
        return level.getBlockState(pos).canBeReplaced(new RobotPlaceContext(entity, pos));
    }

    @Override
    protected boolean operateOnBlock(BlockPos pos) {
        if(!(entity.getMainHandItem().getItem() instanceof BlockItem blockItem)) return false;
        InteractionResult result =  blockItem.place(new RobotPlaceContext(entity, pos));
        entity.swing(InteractionHand.MAIN_HAND);
        return result.consumesAction();
    }

    @Override
    public boolean canUse() {
        return entity.getMainHandItem().getItem() instanceof BlockItem  && super.canUse();
    }

    @MethodsReturnNonnullByDefault
    private static class RobotPlaceContext extends BlockPlaceContext {

        private final LivingEntity entity;

        public RobotPlaceContext(LivingEntity entity, BlockPos pos) {
            super(entity.level(), null, InteractionHand.MAIN_HAND, entity.getMainHandItem(), new BlockHitResult(Vec3.ZERO, Direction.UP, pos, true));
            this.entity = entity;
        }

        @Override
        public Direction getNearestLookingDirection() {
            return Direction.orderedByNearest(entity)[0];
        }

        @Override
        public Direction getNearestLookingVerticalDirection() {
            return Direction.getFacingAxis(entity, Direction.Axis.Y);
        }

        @Override
        public Direction[] getNearestLookingDirections() {
            Direction[] adirection = Direction.orderedByNearest(entity);
            if (!this.replaceClicked) {
                Direction direction = this.getClickedFace();

                int i;
                for (i = 0; i < adirection.length && adirection[i] != direction.getOpposite(); ++i) {
                }

                if (i > 0) {
                    System.arraycopy(adirection, 0, adirection, 1, i);
                    adirection[0] = direction.getOpposite();
                }

            }
            return adirection;
        }
    }
}
