package com.ignis.igrobotics.common.entity.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

import java.util.EnumSet;

public class SpecificTargetGoal extends TargetGoal {

	private final TargetingConditions targetingConditions = TargetingConditions.forCombat().ignoreLineOfSight();

	public SpecificTargetGoal(Mob creature, LivingEntity target) {
		super(creature, false, false);
		setFlags(EnumSet.of(Flag.TARGET));
		this.targetMob = target;
	}
	
	@Override
	public void start() {
		super.start();
		mob.setTarget(targetMob);
	}

	@Override
	public boolean canUse() {
		return canAttack(targetMob, targetingConditions);
	}

	@Override
	public boolean canContinueToUse() {
		mob.setTarget(targetMob);
		return canUse();
	}

	@Override
	public void stop() {
		mob.setTarget(null);
	}

	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof SpecificTargetGoal huntGoal)) return false;
		return mob.equals(huntGoal.mob) && (targetMob == huntGoal.targetMob || targetMob.equals(huntGoal.targetMob));
	}
}
