package com.ignis.norabotics.common.content.entity.ai;

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
		idealLookVector = Vec3.directionFromRotation(45, entity.getYHeadRot());
	}

	@Override
	public boolean canUse() {
		return true;
	}
	
	@Override
	public boolean canContinueToUse() {
		return true;
	}
	
	@Override
	public void tick() {
		entity.getLookControl().setLookAt(entity.getEyePosition().add(idealLookVector));
	}

	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof LookDownGoal lookDownGoal)) return false;
		return entity.equals(lookDownGoal.entity);
	}
}
