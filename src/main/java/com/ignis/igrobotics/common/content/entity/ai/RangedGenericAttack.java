package com.ignis.igrobotics.common.content.entity.ai;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.*;
import net.minecraft.world.item.*;

public class RangedGenericAttack extends AbstractRangedAttackGoal {
    public RangedGenericAttack(Mob attacker, int attackIntervalMin, int attackIntervalMax, float inaccuracy) {
        super(attacker, attackIntervalMin, attackIntervalMax, inaccuracy);
    }

    @Override
    public boolean validWeapon(Item item) {
        return item instanceof SnowballItem || item instanceof ThrowablePotionItem ||
                (item instanceof ProjectileWeaponItem && !(item instanceof CrossbowItem || item instanceof BowItem));
    }

    @Override
    public Projectile getProjectile(ItemStack ammunition, float force) {
        if(ammunition.getItem() instanceof SnowballItem) return new Snowball(attacker.level(), attacker);
        if(ammunition.getItem() instanceof TridentItem) {
            ThrownTrident trident = new ThrownTrident(attacker.level(), attacker, ammunition);
            trident.pickup = AbstractArrow.Pickup.ALLOWED;
            return trident;
        }
        if(ammunition.getItem() instanceof ThrowablePotionItem) {
            ThrownPotion thrownpotion = new ThrownPotion(attacker.level(), attacker);
            thrownpotion.setItem(ammunition);
            thrownpotion.setXRot(thrownpotion.getXRot() - -20.0F);
            return thrownpotion;
        }
        return ProjectileUtil.getMobArrow(attacker, ammunition, force);
    }

    @Override
    public int getAttackRadius(ItemStack weapon) {
        if(weapon.getItem() instanceof ThrowablePotionItem) return 8;
        return 12;
    }

    @Override
    public SoundEvent getSound(ItemStack stack) {
        if(stack.getItem() instanceof SnowballItem) return SoundEvents.SNOWBALL_THROW;
        if(stack.getItem() instanceof TridentItem) return SoundEvents.TRIDENT_THROW;
        if(stack.getItem() instanceof ThrowablePotionItem) return SoundEvents.WITCH_THROW;
        return SoundEvents.ARROW_SHOOT;
    }

    /**
     * Code taken from {@link net.minecraft.world.entity.ai.goal.RangedAttackGoal}
     * @param distanceToTarget distance to attack target
     * @param hasLineOfSight whether the attacker has line of sight with the target
     */
    @Override
    protected void attackTick(double distanceToTarget, boolean hasLineOfSight) {
        if (--this.attackDelay == 0) {
            if (!hasLineOfSight) {
                return;
            }
            float f = (float) Math.sqrt(distanceToTarget) / this.attackRadius;
            float f1 = Mth.clamp(f, 0.1F, 1.0F);
            performAttack(f1);
            this.attackDelay = Mth.floor(f * (float)(this.attackIntervalMax - this.attackIntervalMin) + (float)this.attackIntervalMin);
        } else if (this.attackDelay < 0) {
            this.attackDelay = Mth.floor(Mth.lerp(Math.sqrt(distanceToTarget) / (double)this.attackRadius, (double)this.attackIntervalMin, (double)this.attackIntervalMax));
        }
    }
}
