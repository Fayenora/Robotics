package com.ignis.igrobotics.common.content.entity.ai;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class LookDownGoal extends Goal {
	
	private final Mob entity;
	private final Vec3 idealLookVector;
	
	public LookDownGoal(Mob entity) {
		setFlags(EnumSet.of(Flag.LOOK, Flag.TARGET, Flag.MOVE, Flag.JUMP));
		this.entity = entity;
		idealLookVector = Vec3.directionFromRotation(-45, entity.getYHeadRot());
	}

	@Override
	public boolean canUse() {
		return canContinueToUse();
	}
	
	@Override
	public boolean canContinueToUse() {
		Vec3 current = entity.getLookAngle();
		return current.distanceTo(idealLookVector) > 0.1;
	}
	
	@Override
	public void tick() {
		entity.getLookControl().setLookAt(idealLookVector);
	}

	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof LookDownGoal lookDownGoal)) return false;
		return entity.equals(lookDownGoal.entity);
	}
}
