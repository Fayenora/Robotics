package com.io.norabotics.common.helpers.types;

import com.io.norabotics.common.handlers.RobotBehavior;
import com.io.norabotics.common.helpers.EntityFinder;
import com.mojang.authlib.GameProfile;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.ForgeEventFactory;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.Map;
import java.util.function.Predicate;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class RobotFakePlayer extends FakePlayer {

    /** The attached mob() */
    protected LivingEntity mob;

    public RobotFakePlayer(Mob mob, GameProfile name) {
        super((ServerLevel) mob.level(), name);
        this.mob = mob;
    }

    @Override
    public float getDigSpeed(BlockState state, @Nullable BlockPos pos) {
        return RobotBehavior.destroySpeed(mob(), mob().getMainHandItem(), state);
    }

    @Override
    public boolean hasCorrectToolForDrops(BlockState state) {
        return ForgeEventFactory.doPlayerHarvestCheck(this, state, !state.requiresCorrectToolForDrops() || getMainHandItem().isCorrectToolForDrops(state));
    }

    @Override
    public boolean canCollideWith(Entity p_20303_) {
        return false;
    }

    // Mob Effects
    public boolean removeAllEffects() { return mob().removeAllEffects(); }
    public Collection<MobEffectInstance> getActiveEffects() { return mob().getActiveEffects(); }
    public Map<MobEffect, MobEffectInstance> getActiveEffectsMap() { return mob().getActiveEffectsMap(); }
    public boolean hasEffect(MobEffect p_21024_) { return mob().hasEffect(p_21024_); }
    public MobEffectInstance getEffect(MobEffect p_21125_) { return mob().getEffect(p_21125_); }
    public boolean addEffect(MobEffectInstance p_147208_, @Nullable Entity p_147209_) { return mob().addEffect(p_147208_, p_147209_); }
    public boolean canBeAffected(MobEffectInstance p_21197_) { return mob().canBeAffected(p_21197_); }
    public void forceAddEffect(MobEffectInstance p_147216_, @Nullable Entity p_147217_) { mob().forceAddEffect(p_147216_, p_147217_); }
    public MobEffectInstance removeEffectNoUpdate(@Nullable MobEffect p_21164_) { return mob().removeEffectNoUpdate(p_21164_); }
    public boolean removeEffect(MobEffect p_21196_) { return mob().removeEffect(p_21196_); }
    public boolean curePotionEffects(ItemStack curativeItem) { return mob().curePotionEffects(curativeItem); }

    // Health & Attributes
    public float getHealth() { return mob == null ? super.getHealth() : mob().getHealth(); }
    public void setHealth(float p_21154_) { if(mob == null) super.setHealth(p_21154_); else mob().setHealth(p_21154_); }
    public boolean hurt(DamageSource p_9037_, float p_9038_) { return mob().hurt(p_9037_, p_9038_); }
    public void heal(float p_21116_) { mob().heal(p_21116_); }
    public boolean isDeadOrDying() { return mob == null ? super.isDeadOrDying() : mob().isDeadOrDying(); }
    public void knockback(double p_147241_, double p_147242_, double p_147243_) { mob().knockback(p_147241_, p_147242_, p_147243_); }
    public boolean isAlive() { return mob == null ? super.isAlive() : mob().isAlive(); }
    public int getArmorValue() { return mob().getArmorValue(); }
    public boolean isInvulnerableTo(DamageSource source) { return false; }
    public void die(DamageSource source) { mob().die(source); }
    public void handleDamageEvent(DamageSource source) { mob().handleDamageEvent(source); }
    public AttributeMap getAttributes() { return mob == null ? super.getAttributes() : mob().getAttributes(); }

    // Inventory & Hands
    public ItemStack getMainHandItem() { return mob == null ? super.getMainHandItem() : mob().getMainHandItem(); }
    public ItemStack getOffhandItem() { return mob == null ? super.getOffhandItem() : mob().getOffhandItem(); }
    public boolean isHolding(Predicate<ItemStack> p_21094_) { return mob().isHolding(p_21094_); }
    public ItemStack getItemInHand(InteractionHand p_21121_) { return mob().getItemInHand(p_21121_); }
    public void setItemInHand(InteractionHand p_21009_, ItemStack p_21010_) { mob().setItemInHand(p_21009_, p_21010_); }
    public boolean hasItemInSlot(EquipmentSlot p_21034_) { return mob().hasItemInSlot(p_21034_); }
    public ItemStack getUseItem() { return mob().getUseItem(); }
    public Iterable<ItemStack> getArmorSlots() { return mob().getArmorSlots(); }
    public ItemStack getItemBySlot(EquipmentSlot p_21127_) { return mob == null ? super.getItemBySlot(p_21127_) : mob().getItemBySlot(p_21127_); }
    public void setItemSlot(EquipmentSlot p_21036_, ItemStack p_21037_) { mob().setItemSlot(p_21036_, p_21037_); }
    public void take(Entity p_21030_, int p_21031_) { mob().take(p_21030_, p_21031_); }

    public float getSpeed() { return mob().getSpeed(); }
    public void setSpeed(float p_21320_) { mob().setSpeed(p_21320_); }

    public ItemStack eat(Level p_21067_, ItemStack p_21068_) { return mob().eat(p_21067_, p_21068_); }

    public <T> @NotNull LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing) {
        return mob == null ? super.getCapability(capability, facing) : mob().getCapability(capability, facing);
    }

    public void invalidateCaps() {
        if(mob != null) {
            mob().invalidateCaps();
        } else super.invalidateCaps();
    }

    public void reviveCaps() {
        if(mob != null) {
            mob().reviveCaps();
        } else super.reviveCaps();
    }

    //TODO Implement experience?
    public void setExperiencePoints(int p_8986_) {}
    public void setExperienceLevels(int p_9175_) {}
    public void giveExperienceLevels(int p_9200_) {}
    public void giveExperiencePoints(int p_9208_) {}

    /* Barely relevant
    public Entity changeDimension(ServerLevel p_9180_, ITeleporter teleporter);
    protected void onEffectAdded(MobEffectInstance p_143393_, @Nullable Entity p_143394_);
    protected void onEffectUpdated(MobEffectInstance p_143396_, boolean p_143397_, @Nullable Entity p_143398_);
    protected void onEffectRemoved(MobEffectInstance p_9184_);
    public void teleportTo(double p_8969_, double p_8970_, double p_8971_);
    public void teleportRelative(double p_251611_, double p_248861_, double p_252266_);
    public boolean teleportTo(ServerLevel p_265564_, double p_265424_, double p_265680_, double p_265312_, Set<RelativeMovement> p_265192_, float p_265059_, float p_265266_);
    public void teleportTo(ServerLevel p_9000_, double p_9001_, double p_9002_, double p_9003_, float p_9004_, float p_9005_);
    public ItemEntity drop(ItemStack p_9085_, boolean p_9086_, boolean p_9087_);
    public boolean drop(boolean p_182295_);
    public ItemStack getItemBySlot(EquipmentSlot p_36257_)
    public void setItemSlot(EquipmentSlot p_36161_, ItemStack p_36162_);
    public boolean addItem(ItemStack p_36357_);
     */

    private LivingEntity mob() {
        if(mob == null) return this;
        if(!this.mob.isRemoved()) return mob;
        Entity entity = EntityFinder.getEntity(mob.level(), mob.getUUID());
        if(entity instanceof LivingEntity newMob) {
            this.mob = newMob;
        }
        return mob;
    }
    
}
