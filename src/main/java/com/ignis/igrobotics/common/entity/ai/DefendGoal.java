package com.ignis.igrobotics.common.entity.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

import java.util.EnumSet;

public class DefendGoal extends TargetGoal {

    LivingEntity attacker;
    LivingEntity toDefend;
    private int timestamp;

    public DefendGoal(Mob defender, LivingEntity toDefend, boolean checkSight) {
        super(defender, true);
        this.toDefend = toDefend;
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        this.attacker = toDefend.getLastHurtByMob();
        int i = toDefend.getLastHurtByMobTimestamp();
        return i != this.timestamp && this.canAttack(this.attacker, TargetingConditions.DEFAULT);
    }

    @Override
    public void start() {
        this.mob.setTarget(attacker);
        if (toDefend != null)
        {
            this.timestamp = toDefend.getLastHurtByMobTimestamp();
        }
        super.start();
    }
}
