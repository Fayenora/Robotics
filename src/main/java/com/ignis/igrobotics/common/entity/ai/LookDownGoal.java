package com.ignis.igrobotics.common.entity.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class LookDownGoal extends Goal {
	
	private LivingEntity entity;
	
	public LookDownGoal(LivingEntity entity) {
		this.entity = entity;
		setFlags(EnumSet.of(Flag.LOOK));
	}

	@Override
	public boolean canUse() {
		return canContinueToUse();
	}
	
	@Override
	public boolean canContinueToUse() {
		Vec3 idealLookVector = Vec3.directionFromRotation(-45, entity.getYHeadRot());
		Vec3 current = entity.getLookAngle();
		return current.distanceTo(idealLookVector) > 0.1;
	}
	
	@Override
	public void tick() {
		entity.lerpHeadTo(entity.getYHeadRot(), -45);
	}

	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof LookDownGoal lookDownGoal)) return false;
		return entity.equals(lookDownGoal.entity);
	}
}
