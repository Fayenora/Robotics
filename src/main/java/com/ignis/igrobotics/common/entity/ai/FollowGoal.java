package com.ignis.igrobotics.common.entity.ai;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.BlockPathTypes;

import java.util.EnumSet;

/**
 * A slightly altered version of {@link net.minecraft.world.entity.ai.goal.FollowMobGoal}
 * @author Ignis
 */
public class FollowGoal extends Goal {

	protected final Mob entity;
    protected Entity followingEntityCache, followingEntity;
    private final double speedModifier;
    private final PathNavigation navigation;
    private int timeToRecalculatePath;
    private final float stopDistance;
    private float oldWaterCost;
    protected final float areaSize;

    public FollowGoal(Mob follower, Entity toFollow, float distance, float area) {
        this.entity = follower;
        this.speedModifier = follower.getAttributeValue(Attributes.MOVEMENT_SPEED);
        this.navigation = follower.getNavigation();
        this.followingEntityCache = toFollow;
        this.followingEntity = toFollow;
        this.stopDistance = distance;
        this.areaSize = area;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));

        if (!(follower.getNavigation() instanceof GroundPathNavigation) && !(follower.getNavigation() instanceof FlyingPathNavigation)) {
            throw new IllegalArgumentException("Unsupported mob type for FollowMobGoal");
        }
    }

    @Override
	public boolean canUse() {
    	if(followingEntityCache == null) {
    		followingEntityCache = followingEntity;
    	}
        return entity.distanceTo(followingEntityCache) < areaSize;
    }

    @Override
	public boolean canContinueToUse() {
        return this.followingEntityCache != null && !this.navigation.isDone() && this.entity.distanceToSqr(this.followingEntityCache) > this.stopDistance * this.stopDistance;
    }

    @Override
	public void start() {
        this.timeToRecalculatePath = 0;
        this.oldWaterCost = this.entity.getPathfindingMalus(BlockPathTypes.WATER);
        this.entity.setPathfindingMalus(BlockPathTypes.WATER, 0.0F);
    }

    @Override
	public void stop() {
    	followingEntityCache = null;
        this.navigation.stop();
        this.entity.setPathfindingMalus(BlockPathTypes.WATER, this.oldWaterCost);
    }

    @Override
	public void tick() {
        if (this.followingEntityCache == null || this.entity.isLeashed()) return;
        //Don't make this conflict with other look tasks (should be handled by mutex bits, but can lead to weird head flickering)
        this.entity.getLookControl().setLookAt(this.followingEntity, 10.0F, (float)this.entity.getMaxHeadXRot());

        if (--this.timeToRecalculatePath > 0) return;
        
        this.timeToRecalculatePath = adjustedTickDelay(10);
        double d3 = entity.distanceToSqr(followingEntityCache);

        if (d3 > this.stopDistance * this.stopDistance) {
            this.navigation.moveTo(this.followingEntityCache, this.speedModifier);
        } else {
            this.navigation.stop();

            double lookX = 0, lookY = 0, lookZ = 0;
            if(followingEntityCache instanceof Mob mob) {
            	lookX = mob.getLookControl().getWantedX();
                lookY = mob.getLookControl().getWantedY();
                lookZ = mob.getLookControl().getWantedZ();
            }

            if (d3 <= this.stopDistance || (followingEntityCache instanceof Mob && lookX == this.entity.getX() && lookY == this.entity.getY() && lookZ == this.entity.getZ())) {
                double d4 = this.followingEntityCache.getX() - this.entity.getX();
                double d5 = this.followingEntityCache.getZ() - this.entity.getZ();
                this.navigation.moveTo(this.entity.getX() - d4, this.entity.getY(), this.entity.getZ() - d5, this.speedModifier);
            }
        }
    }

}
