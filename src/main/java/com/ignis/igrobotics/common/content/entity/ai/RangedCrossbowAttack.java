package com.ignis.igrobotics.common.content.entity.ai;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;

public class RangedCrossbowAttack extends AbstractRangedAttackGoal {

    public static final UniformInt PATHFINDING_DELAY_RANGE = TimeUtil.rangeOfSeconds(1, 2);

    protected CrossbowState crossbowState = CrossbowState.UNCHARGED;
    private int updatePathDelay;

    public RangedCrossbowAttack(Mob attacker, int attackIntervalMin, int attackIntervalMax, float inaccuracy) {
        super(attacker, attackIntervalMin, attackIntervalMax, inaccuracy);
    }

    @Override
    public void performAttack(Mob attacker, LivingEntity target, InteractionHand hand, float force, float inaccuracy) {
        CrossbowItem.performShooting(attacker.level(), attacker, hand, attacker.getItemInHand(hand), force, inaccuracy);
        attacker.getItemInHand(hand).hurtAndBreak(1, attacker, living -> living.broadcastBreakEvent(getWeaponHand()));
    }

    @Override
    public boolean validWeapon(Item item) {
        return item instanceof CrossbowItem;
    }

    @Override
    public Projectile getProjectile(ItemStack ammunition, float force) {
        return ProjectileUtil.getMobArrow(attacker, ammunition, force);
    }

    @Override
    public int getAttackRadius(ItemStack weapon) {
        if(!(weapon.getItem() instanceof ProjectileWeaponItem crossbow)) return 0;
        return crossbow.getDefaultProjectileRange();
    }

    @Override
    public SoundEvent getSound(ItemStack weapon) {
        return SoundEvents.CROSSBOW_SHOOT;
    }

    @Override
    protected void moveToTarget(double distanceToTarget) {
        if (!canRecharge(distanceToTarget)) {
            --this.updatePathDelay;
            if (this.updatePathDelay <= 0) {
                attacker.getNavigation().moveTo(target, this.canRun() ? 1 : 0.5D);
                this.updatePathDelay = PATHFINDING_DELAY_RANGE.sample(attacker.getRandom());
            }
        } else {
            this.updatePathDelay = 0;
            attacker.getNavigation().stop();
        }
    }

    private boolean canRecharge(double distanceToTarget) {
        return (distanceToTarget <= this.attackRadiusSqr && this.seeTime >= 5) || this.attackDelay != 0;
    }

    /**
     * Code taken from {@link net.minecraft.world.entity.ai.goal.RangedCrossbowAttackGoal}
     * @param distanceToTarget distance to attack target
     * @param hasLineOfSight whether the attacker has line of sight with the target
     */
    @Override
    protected void attackTick(double distanceToTarget, boolean hasLineOfSight) {
        switch(crossbowState) {
            case UNCHARGED -> {
                if (canRecharge(distanceToTarget)) {
                    attacker.startUsingItem(getWeaponHand());
                    this.crossbowState = CrossbowState.CHARGING;
                    //attacker.setChargingCrossbow(true); (Maybe in the future for animations)
                }
            }
            case CHARGING -> {
                if (!attacker.isUsingItem()) {
                    this.crossbowState = CrossbowState.UNCHARGED;
                }

                int i = attacker.getTicksUsingItem();
                ItemStack itemstack = attacker.getUseItem();
                if (i >= CrossbowItem.getChargeDuration(itemstack)) {
                    attacker.releaseUsingItem();
                    this.crossbowState = CrossbowState.CHARGED;
                    this.attackDelay = 20 + attacker.getRandom().nextInt(20);
                    ItemStack crossbow = attacker.getItemInHand(getWeaponHand());
                    CrossbowItem.setCharged(crossbow, true);
                    //attacker.setChargingCrossbow(false); (Maybe in the future for animations)
                }
            }
            case CHARGED -> {
                if (--this.attackDelay <= 0) this.crossbowState = CrossbowState.READY_TO_ATTACK;
            }
            case READY_TO_ATTACK -> {
                performAttack(1);
                ItemStack crossbow = attacker.getItemInHand(getWeaponHand());
                CrossbowItem.setCharged(crossbow, false);
                this.crossbowState = CrossbowState.UNCHARGED;
            }
        }
    }

    @Override
    public boolean canUse() {
        return super.canUse() || (crossbowState == CrossbowState.CHARGED && isHoldingWeapon());
    }

    private boolean canRun() {
        return this.crossbowState == CrossbowState.UNCHARGED;
    }

    enum CrossbowState {
        UNCHARGED,
        CHARGING,
        CHARGED,
        READY_TO_ATTACK;
    }
}
