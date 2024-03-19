package com.ignis.igrobotics.common.entity.ai;

import com.ignis.igrobotics.common.RobotBehavior;
import com.ignis.igrobotics.core.capabilities.commands.CommandApplyException;
import com.ignis.igrobotics.core.util.MathUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public abstract class AbstractMultiBlockGoal extends Goal {

    public static final int MAX_ALLOWED_BLOCKS = 256;

    protected Mob entity;
    protected DimensionNavigator navigator;
    private final ResourceKey<Level> dim;
    private final int minX, maxX, minY, maxY, minZ, maxZ;

    private int hash;
    private BlockPos nextPos;

    public AbstractMultiBlockGoal(Mob entity, GlobalPos pos1, GlobalPos pos2) {
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.TARGET));
        this.entity = entity;
        navigator = new DimensionNavigator(entity, 16, 16, 1);
        dim = pos1.dimension();
        minX = Math.min(pos1.pos().getX(), pos2.pos().getX());
        maxX = Math.max(pos1.pos().getX(), pos2.pos().getX());
        minY = Math.min(pos1.pos().getY(), pos2.pos().getY());
        maxY = Math.max(pos1.pos().getY(), pos2.pos().getY());
        minZ = Math.min(pos1.pos().getZ(), pos2.pos().getZ());
        maxZ = Math.max(pos1.pos().getZ(), pos2.pos().getZ());
        if(!pos1.dimension().equals(pos2.dimension())) {
            throw new CommandApplyException("command.break.different_dimensions");
        }
        if(Math.abs(maxX - minX) + Math.abs(maxY - minY) + Math.abs(maxZ - minZ) > MAX_ALLOWED_BLOCKS) {
            throw new CommandApplyException("command.break.too_many_blocks", MAX_ALLOWED_BLOCKS);
        }
    }

    protected abstract boolean isValidBlock(Level level, BlockPos pos, ItemStack stack);

    protected abstract boolean operateOnBlock(BlockPos pos);

    public boolean moveAndOperateOnBlock(BlockPos pos) {
        if(!RobotBehavior.canReach(entity, pos)) {
            entity.getNavigation().moveTo(pos.getX(), pos.getY(), pos.getZ(), 1);
            return false;
        }
        if(entity.getLookAngle().distanceTo(Vec3.atCenterOf(pos).subtract(entity.getEyePosition()).normalize()) > 0.1) {
            entity.getLookControl().setLookAt(Vec3.atCenterOf(pos));
            return false;
        }
        return operateOnBlock(pos);
    }

    @Override
    public void stop() {
        nextPos = null;
        hash = 0;
    }

    @Override
    public void tick() {
        if(!entity.level.dimension().equals(dim)) {
            navigator.navigateTo(GlobalPos.of(dim, new BlockPos(minX, minY, minZ)));
        }
        Level level = entity.getServer().getLevel(dim);
        if(level == null) return;
        if(nextPos == null) {
            nextPos = selectNextBlock(level);
        }
        if(nextPos != null && moveAndOperateOnBlock(nextPos)) {
            nextPos = null;
        }
    }

    @Override
    public boolean canUse() {
        int recomputedHash = 0;
        Level level = entity.getServer().getLevel(dim);
        if(level == null) return true; //Dimension likely not loaded. Move there!

        //FIXME: Hash Computation not working
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(0, 0, 0); //Use a mutable pos for efficiency reasons
        for(int x = minX; x <= maxX; x++) {
            for(int y = minY; y <= maxY; y++) {
                for(int z = minZ; z <= maxZ; z++) {
                    pos.set(x, y, z);
                    recomputedHash += level.getBlockState(pos).hashCode();
                }
            }
        }

        boolean valid = false;
        //Re-check the area if it changed
        if(recomputedHash != hash) {
            valid = doesAreaContainValidBlocks(level);
            hash = recomputedHash;
        }

        return valid;
    }

    private boolean doesAreaContainValidBlocks(Level level) {
        for(int x = minX; x <= maxX; x++) {
            for(int y = minY; y <= maxY; y++) {
                for(int z = minZ; z <= maxZ; z++) {
                    if(isValidBlock(level, new BlockPos(x, y, z), entity.getMainHandItem())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public BlockPos selectNextBlock(Level level) {
        int posX = MathUtil.restrict(minX, entity.getX(), maxX);
        int posY = MathUtil.restrict(minY, entity.getY(), maxY);
        int posZ = MathUtil.restrict(minZ, entity.getZ(), maxZ);

        return nextValidBlock(level, new BlockPos(posX, posY, posZ));
    }

    //BFS for the next non-air block in the area
    public BlockPos nextValidBlock(Level level, BlockPos start) {
        List<BlockPos> queue = new ArrayList<>();
        Set<BlockPos> explored = new HashSet<>();
        explored.add(start);
        queue.add(start);
        while(!queue.isEmpty()) {
            BlockPos pos = queue.get(0);
            queue.remove(0);
            //Check condition
            if(isValidBlock(level, pos, entity.getMainHandItem())) return pos;
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
        if(!(obj instanceof AbstractMultiBlockGoal other)) return false;
        return entity.equals(other.entity) && minX == other.minX && minY == other.minY && minZ == other.minZ && maxX == other.maxX && maxY == other.maxY && maxZ == other.maxZ && dim.equals(other.dim);
    }
}
