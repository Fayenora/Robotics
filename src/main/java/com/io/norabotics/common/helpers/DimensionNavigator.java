package com.io.norabotics.common.helpers;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

public class DimensionNavigator {

    public static final Map<ResourceKey<DimensionType>, Block> PORTAL_BLOCKS = new HashMap<>();
    public static final Map<ResourceKey<DimensionType>, Block> PRIMARY_BLOCKS = new HashMap<>();
    static {
        PORTAL_BLOCKS.put(BuiltinDimensionTypes.NETHER, Blocks.NETHER_PORTAL);
        PORTAL_BLOCKS.put(BuiltinDimensionTypes.END, Blocks.END_PORTAL);
        PRIMARY_BLOCKS.put(BuiltinDimensionTypes.OVERWORLD, Blocks.DIRT);
        PRIMARY_BLOCKS.put(BuiltinDimensionTypes.NETHER, Blocks.NETHERRACK);
        PRIMARY_BLOCKS.put(BuiltinDimensionTypes.END, Blocks.END_STONE);
    }

    private final Mob mob;
    private final double speedMod;
    private final int searchRange, verticalSearchRange;

    public DimensionNavigator(Mob mob, int searchRange, int verticalSearchRange, double speedModifier) {
        this.mob = mob;
        this.speedMod = speedModifier;
        this.searchRange = searchRange;
        this.verticalSearchRange = verticalSearchRange;
    }

    public boolean navigateTo(GlobalPos pos) {
        Level level = mob.getServer().getLevel(pos.dimension());
        if(level == null) return false;
        return navigateTo(level, pos.pos());
    }

    public boolean navigateTo(Level level, BlockPos pos) {
        if(mob.level().equals(level)) {
            return mob.getNavigation().moveTo(pos.getX(), pos.getY(), pos.getZ(), speedMod);
        }
        Block portal = getPortalBlock(level.dimensionTypeId());
        BlockPos portalPos = findNearestBlock(mob.level(), mob.blockPosition(), portal, verticalSearchRange, searchRange);
        if(portalPos == null) return false;
        PathNavigation navigator = mob.getNavigation();
        Vec3 portalCenter = Vec3.atCenterOf(portalPos);
        mob.setPortalCooldown(0);
        return navigator.moveTo(navigator.createPath(portalCenter.x, portalCenter.y, portalCenter.z, 0), speedMod);
    }

    private Block getPortalBlock(ResourceKey<DimensionType> targetDimension) {
        ResourceKey<DimensionType> dimension = mob.level().dimensionTypeId();
        if(!dimension.equals(BuiltinDimensionTypes.OVERWORLD)) {
            return PORTAL_BLOCKS.get(dimension);
        } else {
            return PORTAL_BLOCKS.get(targetDimension);
        }
    }

    public static BlockPos findNearestBlock(Level level, BlockPos source, Block block, int verticalSearchRange, int searchRange) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for(int k = 0; k <= verticalSearchRange; k = k > 0 ? -k : 1 - k) {
            for(int l = 0; l < searchRange; ++l) {
                for(int i1 = 0; i1 <= l; i1 = i1 > 0 ? -i1 : 1 - i1) {
                    for(int j1 = i1 < l && i1 > -l ? l : 0; j1 <= l; j1 = j1 > 0 ? -j1 : 1 - j1) {
                        pos.setWithOffset(source, i1, k - 1, j1);
                        if (level.getBlockState(pos).getBlock().equals(block)) {
                            return pos;
                        }
                    }
                }
            }
        }

        return null;
    }
}
