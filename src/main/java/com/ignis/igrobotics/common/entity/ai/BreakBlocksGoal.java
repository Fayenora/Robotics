package com.ignis.igrobotics.common.entity.ai;

import com.ignis.igrobotics.common.EntityInteractionManager;
import com.ignis.igrobotics.common.RobotBehavior;
import com.ignis.igrobotics.core.capabilities.commands.CommandApplyException;
import com.ignis.igrobotics.core.util.MathUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class BreakBlocksGoal extends Goal {

    public static final int MAX_ALLOWED_BLOCKS = 256;

    protected Mob entity;
    protected BlockPos pos1, pos2;
    private final int minX, maxX, minY, maxY, minZ, maxZ;

    private final EntityInteractionManager interactionManager;

    /** Hash of all blocks in the area to mine */
    private int hash;
    private boolean containsBlocks = true;
    private BlockPos nextPos;

    public BreakBlocksGoal(Mob entity, BlockPos pos1, BlockPos pos2) {
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.TARGET));
        this.entity = entity;
        this.pos1 = pos1;
        this.pos2 = pos2;
        interactionManager = new EntityInteractionManager(entity);

        minX = Math.min(pos1.getX(), pos2.getX());
        maxX = Math.max(pos1.getX(), pos2.getX());
        minY = Math.min(pos1.getY(), pos2.getY());
        maxY = Math.max(pos1.getY(), pos2.getY());
        minZ = Math.min(pos1.getZ(), pos2.getZ());
        maxZ = Math.max(pos1.getZ(), pos2.getZ());
        if(Math.abs(maxX - minX) + Math.abs(maxY - minY) + Math.abs(maxZ - minZ) > MAX_ALLOWED_BLOCKS) {
            throw new CommandApplyException("command.break.too_many_blocks", MAX_ALLOWED_BLOCKS);
        }
    }

    @Override
    public void stop() {
        interactionManager.cancelDestroyingBlock();
        nextPos = null;
        hash = 0;
    }

    @Override
    public void tick() {
        if(nextPos == null) {
            nextPos = selectNextBlock();
        }
        if(nextPos != null && moveToBlockAndMine(nextPos)) {
            nextPos = null;
        }
    }

    @Override
    public boolean canUse() {
        int recomputedHash = 0;
        for(int x = minX; x <= maxX; x++) {
            for(int y = minY; y <= maxY; y++) {
                for(int z = minZ; z <= maxZ; z++) {
                    recomputedHash += entity.level.getBlockState(new BlockPos(x, y, z)).hashCode();
                }
            }
        }

        //Re-check the area if it changed
        if(recomputedHash != hash) {
            containsBlocks = doesAreaContainMineableBlocks();
            hash = recomputedHash;
        }

        return containsBlocks;
    }

    public boolean moveToBlockAndMine(BlockPos pos) {
        if(!RobotBehavior.canReach(entity, pos)) {
            double entitySpeed = entity.getAttributes().getValue(Attributes.MOVEMENT_SPEED);
            entity.getNavigation().moveTo(pos.getX(), pos.getY(), pos.getZ(), entitySpeed);
            return false;
        }
        entity.getLookControl().setLookAt(Vec3.atCenterOf(pos));
        return interactionManager.dig(pos, Direction.UP);
    }

    private boolean doesAreaContainMineableBlocks() {
        for(int x = minX; x <= maxX; x++) {
            for(int y = minY; y <= maxY; y++) {
                for(int z = minZ; z <= maxZ; z++) {
                    if(canToolHarvestBlock(new BlockPos(x, y, z), entity.getMainHandItem())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean canToolHarvestBlock(BlockPos pos, ItemStack stack) {
        BlockState state = entity.level.getBlockState(pos);
        if(state.isAir() || state.getDestroySpeed(entity.level, pos) < 0) return false;
        if(!state.requiresCorrectToolForDrops()) return true;
        return stack.isCorrectToolForDrops(state);
    }

    public BlockPos selectNextBlock() {
        int posX = MathUtil.restrict(minX, entity.getX(), maxX);
        int posY = MathUtil.restrict(minY, entity.getY(), maxY);
        int posZ = MathUtil.restrict(minZ, entity.getZ(), maxZ);

        return nextHarvestable(new BlockPos(posX, posY, posZ));
    }

    //BFS for the next non air block in the area
    public BlockPos nextHarvestable(BlockPos start) {
        List<BlockPos> queue = new ArrayList<>();
        Set<BlockPos> explored = new HashSet<>();
        explored.add(start);
        queue.add(start);
        while(!queue.isEmpty()) {
            BlockPos pos = queue.get(0);
            queue.remove(0);
            //Check condition
            if(canToolHarvestBlock(pos, entity.getMainHandItem())) return pos;
            // Check neighbors
            for(Direction dir : Direction.values()) {
                BlockPos neighbor = pos.relative(dir);
                if(!explored.contains(neighbor) && isInBounds(neighbor)) {
                    explored.add(neighbor);
                    queue.add(neighbor);
                }
            }
        }

        return null;
    }

    public boolean isInBounds(BlockPos pos) {
        return pos.getX() >= minX && pos.getX() <= maxX
                && pos.getY() >= minY && pos.getY() <= maxY
                && pos.getZ() >= minZ && pos.getZ() <= maxZ;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof BreakBlocksGoal other)) return false;
        return entity.equals(other.entity) && minX == other.minX && minY == other.minY && minZ == other.minZ && maxX == other.maxX && maxY == other.maxY && maxZ == other.maxZ;
    }
}
