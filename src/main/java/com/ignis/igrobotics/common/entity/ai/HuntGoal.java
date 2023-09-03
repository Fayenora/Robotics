package com.ignis.igrobotics.common.entity.ai;

import net.minecraft.core.GlobalPos;
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
		if(targetMob != null && !mob.level.dimension().equals(targetMob.level.dimension())) {
			DimensionNavigator nav = new DimensionNavigator(mob, 16, 16, 1);
			GlobalPos pos = GlobalPos.of(targetMob.level.dimension(), targetMob.blockPosition());
			nav.navigateTo(pos);
		}
		mob.setTarget(targetMob);
	}

	@Override
	public boolean canUse() {
		return canAttack(targetMob, TargetingConditions.DEFAULT);
	}

	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof HuntGoal huntGoal)) return false;
		return mob.equals(huntGoal.mob) && (targetMob == huntGoal.targetMob || targetMob.equals(huntGoal.targetMob));
	}
}
