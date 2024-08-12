package com.ignis.norabotics.common.content.entity.ai;

import com.ignis.norabotics.Reference;
import com.ignis.norabotics.common.helpers.types.EntitySearch;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraftforge.common.util.FakePlayer;

import java.util.EnumSet;
import java.util.Objects;

public class SpecificTargetGoal extends TargetGoal {

	private static final TargetingConditions targetingConditions = TargetingConditions.forCombat().ignoreLineOfSight();
	protected final EntitySearch target;
	private int idleTicks = Reference.TICKS_UNTIL_SEARCHING_AGAIN;

	public SpecificTargetGoal(Mob creature, EntitySearch target) {
		super(creature, false, false);
		setFlags(EnumSet.of(Flag.TARGET));
		this.target = target;
	}
	
	@Override
	public void start() {
		idleTicks = Reference.TICKS_UNTIL_SEARCHING_AGAIN;
		mob.setTarget(targetMob);
		super.start();
	}

	@Override
	public boolean canUse() {
		if(idleTicks++ >= Reference.TICKS_UNTIL_SEARCHING_AGAIN || !isViableTarget(targetMob)) {
			Entity result = target.commence((ServerLevel) mob.level(), mob.position());
			idleTicks = 0;
			if(isViableTarget(result) && result instanceof LivingEntity living) {
				targetMob = living;
				return true;
			}
			return isViableTarget(targetMob);
		}
		return true;
	}

	private boolean isViableTarget(Entity entity) {
		if(!(entity instanceof LivingEntity living) || living instanceof FakePlayer || !living.isAlive()) return false;
		return canAttack(living, targetingConditions);
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
		return Objects.equals(mob, huntGoal.mob) && Objects.equals(target, huntGoal.target);
	}
}
