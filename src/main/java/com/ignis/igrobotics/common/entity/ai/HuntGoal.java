package com.ignis.igrobotics.common.entity.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

import java.util.EnumSet;

public class HuntGoal extends TargetGoal {

	public HuntGoal(Mob creature, LivingEntity target) {
		super(creature, false, false);
		setFlags(EnumSet.of(Flag.MOVE, Flag.TARGET, Flag.LOOK));
		this.targetMob = target;
	}
	
	@Override
	public void start() {
		super.start();
		mob.setTarget(targetMob);
	}

	@Override
	public boolean canUse() {
		return canAttack(targetMob, TargetingConditions.DEFAULT);
	}

}
