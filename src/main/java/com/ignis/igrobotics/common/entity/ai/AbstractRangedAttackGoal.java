package com.ignis.igrobotics.common.entity.ai;

import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.function.Predicate;

public abstract class AbstractRangedAttackGoal extends Goal {

    protected final Mob attacker;
    @Nullable
    protected LivingEntity target;
    protected int attackDelay = -1;
    protected int seeTime;
    protected final int attackIntervalMin, attackIntervalMax;
    private final float inaccuracy;
    protected float attackRadius, attackRadiusSqr;

    private InteractionHand hand;

    public AbstractRangedAttackGoal(Mob attacker, int attackIntervalMin, int attackIntervalMax, float inaccuracy) {
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        this.attacker = attacker;
        this.attackIntervalMin = attackIntervalMin;
        this.attackIntervalMax = attackIntervalMax;
        this.inaccuracy = inaccuracy;
    }

    public abstract boolean validWeapon(Item item);

    public abstract Projectile getProjectile(ItemStack ammunition, float force);

    public abstract int getAttackRadius(ItemStack weapon);

    public abstract SoundEvent getSound(ItemStack weapon);

    protected abstract void attackTick(double distanceToTarget, boolean hasLineOfSight);

    public void performAttack(float force) {
        performAttack(attacker, target, hand, force, inaccuracy);
    }

    public void performAttack(Mob attacker, LivingEntity target, InteractionHand hand, float force, float inaccuracy) {
        ItemStack weapon = attacker.getItemInHand(hand);
        ItemStack ammunition = ForgeHooks.getProjectile(attacker, weapon, ItemStack.EMPTY); // Might be the weapon itself
        Projectile projectile = getProjectile(ammunition, force);
        Vec3 attackVec = attackVector(projectile, target);
        projectile.shoot(attackVec.x, attackVec.y, attackVec.z, projectile instanceof ThrownPotion ? 0.75f : 1.6f, inaccuracy);
        attacker.playSound(getSound(weapon), 1, 1 / (attacker.getRandom().nextFloat() * 0.4f + 0.8f));
        attacker.level.addFreshEntity(projectile);
        weapon.hurtAndBreak(1, attacker, living -> living.broadcastBreakEvent(getWeaponHand()));
        consumeAmmunition(attacker, weapon, ammunition);
    }

    public boolean isHoldingWeapon() {
        if(hand == null) {
            if(!attacker.isHolding(stack -> validWeapon(stack.getItem()))) return false;
            hand = ProjectileUtil.getWeaponHoldingHand(attacker, this::validWeapon);
            attackRadius = getAttackRadius(attacker.getItemInHand(hand));
            attackRadiusSqr = attackRadius * attackRadius;
            return true;
        }
        return validWeapon(attacker.getItemInHand(getWeaponHand()).getItem());
    }

    @Override
    public void tick() {
        double distanceToTarget = attacker.distanceToSqr(target);
        boolean hasLineOfSight = attacker.getSensing().hasLineOfSight(this.target);

        if (hasLineOfSight) {
            ++this.seeTime;
        } else {
            this.seeTime = 0;
        }

        moveToTarget(distanceToTarget);
        attacker.getLookControl().setLookAt(this.target, 30.0F, 30.0F);
        attackTick(distanceToTarget, hasLineOfSight);
    }

    protected void moveToTarget(double distanceToTarget) {
        if (distanceToTarget < (double) this.attackRadiusSqr && this.seeTime >= 5) {
            attacker.getNavigation().stop();
        } else {
            attacker.getNavigation().moveTo(this.target, 1);
        }
    }

    @Override
    public boolean canUse() {
        LivingEntity livingentity = attacker.getTarget();
        if(livingentity == null || !livingentity.isAlive()) return false;
        this.target = livingentity;
        return isHoldingWeapon() && hasAmmunition(attacker, attacker.getItemInHand(getWeaponHand()));
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse() || this.target.isAlive() && !attacker.getNavigation().isDone();
    }

    @Override
    public void stop() {
        this.target = null;
        this.seeTime = 0;
        this.attackDelay = -1;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    public InteractionHand getWeaponHand() {
        return hand;
    }

    public static void consumeAmmunition(LivingEntity living, ItemStack weapon, ItemStack ammunition) {
        if(ammunition.getItem() instanceof ArrowItem arrow && arrow.isInfinite(ammunition, weapon, null)) return;
        ammunition.shrink(1);
        if(ammunition.isEmpty()) {
            living.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(inventory -> {
                for(int i = 0; i < inventory.getSlots(); i++) {
                    if(ammunition == inventory.getStackInSlot(i)) {
                        inventory.extractItem(i, 1, false);
                    }
                }
            });
        }
    }

    public static ItemStack retrieveAmmunitionFromInventory(LivingEntity living, ItemStack weapon) {
        //Return the weapon itself. This might be a snowball, trident, etc.
        if(!(weapon.getItem() instanceof ProjectileWeaponItem weaponItem)) return weapon;
        ItemStack heldProjectile = ProjectileWeaponItem.getHeldProjectile(living, weaponItem.getSupportedHeldProjectiles());
        if (!heldProjectile.isEmpty()) return heldProjectile;

        Predicate<ItemStack> predicate = weaponItem.getAllSupportedProjectiles();
        if(living.getCapability(ForgeCapabilities.ITEM_HANDLER).isPresent()) {
            IItemHandler inventory = living.getCapability(ForgeCapabilities.ITEM_HANDLER).resolve().get();
            for(int i = 0; i < inventory.getSlots(); i++) {
                ItemStack stack = inventory.getStackInSlot(i);
                if (predicate.test(stack)) {
                    return stack;
                }
            }
        }

        return ItemStack.EMPTY;
    }

    public static boolean hasAmmunition(LivingEntity living, ItemStack weapon) {
        if(!(weapon.getItem() instanceof ProjectileWeaponItem weaponItem)) return true;
        if(!living.getCapability(ForgeCapabilities.ITEM_HANDLER).isPresent()) return false;
        IItemHandler inventory = living.getCapability(ForgeCapabilities.ITEM_HANDLER).resolve().get();
        Predicate<ItemStack> isAmmunition = weaponItem.getAllSupportedProjectiles();
        for(int i = 0; i < inventory.getSlots(); i++) {
            if(isAmmunition.test(inventory.getStackInSlot(i))) {
                return true;
            }
        }
        return false;
    }

    public static Vec3 attackVector(Projectile projectile, LivingEntity target) {
        Vec3 direction = target.position().relative(Direction.UP, target.getBbHeight() / 3).subtract(projectile.position());
        return direction.add(0, direction.horizontalDistance() * 0.2, 0);
    }
}
