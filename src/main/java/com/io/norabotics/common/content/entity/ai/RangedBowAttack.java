package com.io.norabotics.common.content.entity.ai;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;

public class RangedBowAttack extends AbstractRangedAttackGoal {
    public RangedBowAttack(Mob attacker, int attackIntervalMin, int attackIntervalMax, float inaccuracy) {
        super(attacker, attackIntervalMin, attackIntervalMax, inaccuracy);
    }

    @Override
    public boolean validWeapon(Item item) {
        if(!(item instanceof BowItem bow)) return false;
        attackRadius = bow.getDefaultProjectileRange();
        attackRadiusSqr = attackRadius * attackRadius;
        return true;
    }

    @Override
    public Projectile getProjectile(ItemStack ammunition, float force) {
        AbstractArrow abstractarrow = ProjectileUtil.getMobArrow(attacker, ammunition, force);
        if (attacker.getItemInHand(getWeaponHand()).getItem() instanceof BowItem bow)
            abstractarrow = bow.customArrow(abstractarrow); // Modify the arrow through bow properties
        return abstractarrow;
    }

    @Override
    public int getAttackRadius(ItemStack weapon) {
        if(!(weapon.getItem() instanceof ProjectileWeaponItem bow)) return 0;
        return bow.getDefaultProjectileRange();
    }

    @Override
    public SoundEvent getSound(ItemStack stack) {
        return SoundEvents.SKELETON_SHOOT;
    }

    /**
     * Code taken from {@link net.minecraft.world.entity.ai.goal.RangedBowAttackGoal}
     * @param distanceToTarget distance to attack target
     * @param hasLineOfSight whether the attacker has line of sight with the target
     */
    @Override
    protected void attackTick(double distanceToTarget, boolean hasLineOfSight) {
        if (attacker.isUsingItem()) {
            if (!hasLineOfSight && this.seeTime < -60) {
                attacker.stopUsingItem();
            } else if (hasLineOfSight) {
                int i = attacker.getTicksUsingItem();
                if (i >= 20) {
                    attacker.stopUsingItem();
                    performAttack(BowItem.getPowerForTime(i));
                    this.attackDelay = this.attackIntervalMin;
                }
            }
        } else if (--this.attackDelay <= 0 && this.seeTime >= -60) {
            attacker.startUsingItem(getWeaponHand());
        }
    }
}
