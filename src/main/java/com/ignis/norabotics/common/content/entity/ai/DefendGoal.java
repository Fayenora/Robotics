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

public class DefendGoal extends TargetGoal {

    LivingEntity attacker;
    EntitySearch toDefend;
    private int timestamp, idleTicks = Reference.TICKS_UNTIL_SEARCHING_AGAIN;

    public DefendGoal(Mob defender, EntitySearch toDefend) {
        super(defender, false);
        this.toDefend = toDefend;
        this.setFlags(EnumSet.of(Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        if(idleTicks++ >= Reference.TICKS_UNTIL_SEARCHING_AGAIN || !isViableTarget(targetMob)) {
            Entity result = toDefend.commence((ServerLevel) mob.level(), mob.position());
            idleTicks = 0;
            if(isViableTarget(result) && result instanceof LivingEntity living) {
                targetMob = living;
                this.attacker = living.getLastHurtByMob();
                return true;
            }
            return isViableTarget(targetMob);
        }
        return true;
    }

    private boolean isViableTarget(Entity entity) {
        if(!(entity instanceof LivingEntity living) || living instanceof FakePlayer || !living.isAlive()) return false;
        return living.getLastHurtByMobTimestamp() != this.timestamp && canAttack(living.getLastHurtByMob(), TargetingConditions.DEFAULT);
    }

    @Override
    public void start() {
        this.idleTicks = Reference.TICKS_UNTIL_SEARCHING_AGAIN;
        this.mob.setTarget(attacker);
        if(targetMob != null) {
            this.timestamp = targetMob.getLastHurtByMobTimestamp();
        }
        super.start();
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof DefendGoal defendGoal)) return false;
        return Objects.equals(attacker, defendGoal.attacker) && Objects.equals(toDefend, defendGoal.toDefend);
    }
}
