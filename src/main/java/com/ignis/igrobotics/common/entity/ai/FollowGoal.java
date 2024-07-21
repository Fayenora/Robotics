package com.ignis.igrobotics.common.entity.ai;

import com.ignis.igrobotics.Reference;
import com.ignis.igrobotics.core.EntitySearch;
import com.ignis.igrobotics.core.capabilities.commands.CommandApplyException;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraftforge.common.util.FakePlayer;

import java.util.EnumSet;
import java.util.Objects;

/**
 * A slightly altered version of {@link net.minecraft.world.entity.ai.goal.FollowMobGoal}
 * @author Ignis
 */
public class FollowGoal extends Goal {

	protected final Mob entity;
    protected Entity followingEntityCache;
    protected EntitySearch followingEntity;
    private final PathNavigation navigation;
    private int timeToRecalculatePath;
    private final float stopDistance;
    private float oldWaterCost;
    protected float areaSize;
    private int idleTicks = Reference.TICKS_UNTIL_SEARCHING_AGAIN;

    public FollowGoal(Mob follower, EntitySearch toFollow, int distance) {
        this.entity = follower;
        this.navigation = follower.getNavigation();
        this.followingEntity = toFollow;
        this.stopDistance = distance;
        this.areaSize = toFollow.getRange() > 0 ? toFollow.getRange() : (float) follower.getAttributeValue(Attributes.FOLLOW_RANGE);
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));

        if (!(follower.getNavigation() instanceof GroundPathNavigation) && !(follower.getNavigation() instanceof FlyingPathNavigation)) {
            throw new CommandApplyException("command.follow.unsupported");
        }
    }

    @Override
	public boolean canUse() {
        if(idleTicks++ >= Reference.TICKS_UNTIL_SEARCHING_AGAIN || !isViableTarget(followingEntityCache)) {
            Entity result = followingEntity.commence((ServerLevel) entity.level(), entity.position());
            idleTicks = 0;
            if(isViableTarget(result) && result instanceof LivingEntity living) {
                followingEntityCache = living;
                return true;
            }
            return isViableTarget(followingEntityCache);
        }
        return true;
    }

    private boolean isViableTarget(Entity entity) {
        if(!(entity instanceof LivingEntity living) || living instanceof FakePlayer || !living.isAlive()) return false;
        return this.entity.distanceTo(living) < areaSize;
    }

    @Override
	public boolean canContinueToUse() {
        return this.followingEntityCache != null && !this.navigation.isDone() && this.entity.distanceToSqr(this.followingEntityCache) > this.stopDistance * this.stopDistance;
    }

    @Override
	public void start() {
        this.idleTicks = Reference.TICKS_UNTIL_SEARCHING_AGAIN;
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
        this.entity.getLookControl().setLookAt(this.followingEntityCache, 10.0F, (float)this.entity.getMaxHeadXRot());

        if (--this.timeToRecalculatePath > 0) return;
        
        this.timeToRecalculatePath = adjustedTickDelay(10);
        double d3 = entity.distanceToSqr(followingEntityCache);

        if (d3 > this.stopDistance * this.stopDistance) {
            this.navigation.moveTo(this.followingEntityCache, 1);
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
                this.navigation.moveTo(this.entity.getX() - d4, this.entity.getY(), this.entity.getZ() - d5, 1);
            }
        }
    }

    public Entity following() {
        return followingEntityCache;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof FollowGoal followGoal)) return false;
        return Objects.equals(entity, followGoal.entity) && Objects.equals(followingEntity, followGoal.followingEntity) && stopDistance == followGoal.stopDistance;
    }
}
