package com.io.norabotics.common.capabilities.impl;

import com.io.norabotics.common.capabilities.IPerkMap;
import com.io.norabotics.common.capabilities.IShielded;
import com.io.norabotics.common.capabilities.ModCapabilities;
import com.io.norabotics.common.capabilities.ModifiableEnergyStorage;
import com.io.norabotics.common.content.entity.RobotEntity;
import com.io.norabotics.common.handlers.ShieldBehavior;
import com.io.norabotics.definitions.ModAttributes;
import com.io.norabotics.definitions.ModSounds;
import com.io.norabotics.definitions.robotics.ModPerks;
import com.io.norabotics.integration.config.RoboticsConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

import java.util.UUID;

/**
 * A shield around an entity. Requires energy to activate only if the entity has the capability to provide it.
 * Requires energy per tick only if the entity has {@link com.io.norabotics.definitions.ModAttributes#ENERGY_CONSUMPTION} as attribute.
 * Behavior is defined in {@link ShieldBehavior}
 */
public class ShieldCapability implements IShielded {

    public static final EntityDataAccessor<Float> SHIELD_HEALTH = RobotEntity.SHIELD_HEALTH;
    private static final UUID MODIFIER_UUID = UUID.fromString("44b77863-d868-4ec8-b3e6-41da330b2bd8");

    public final int BASE_HEALTH, ACTIVATION_ENERGY_COST, ENERGY_PER_HEALTH_COST;
    public final float RECHARGE_RATE;
    public final AttributeModifier ENERGY_UPKEEP;

    protected final LivingEntity entity;
    protected SynchedEntityData dataManager;
    private ModifiableEnergyStorage energy;
    private IPerkMap perks;

    public ShieldCapability(LivingEntity entity) {
        this.entity = entity;
        dataManager = entity.getEntityData();
        dataManager.define(SHIELD_HEALTH, 0f);

        BASE_HEALTH = RoboticsConfig.general.shieldBaseHealth.get();
        ACTIVATION_ENERGY_COST = RoboticsConfig.general.shieldActivationCost.get();
        ENERGY_PER_HEALTH_COST = RoboticsConfig.general.shieldPerHealthCost.get();
        RECHARGE_RATE = RoboticsConfig.general.shieldRechargeRate.get().floatValue();
        ENERGY_UPKEEP = new AttributeModifier(MODIFIER_UUID, "shield_upkeep", 10, AttributeModifier.Operation.ADDITION);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag compound = new CompoundTag();
        compound.putFloat("currentHealth", getHealth());
        return compound;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        setHealth(nbt.getFloat("currentHealth"));
    }

    @Override
    public void damage(float damage) {
        entity.invulnerableTime = 10;
        if(entity.level().isClientSide) return;
        setHealth(Math.max(getHealth() - damage, 0));
        if(getHealth() <= 0) {
            setActive(false);
            entity.playSound(ModSounds.SHIELD_IMPACT_FATAL.get());
        } else {
            entity.playSound(ModSounds.SHIELD_IMPACT.get());
        }
    }

    @Override
    public void tick() {
        if(isShielded() && energy().getEnergyStored() <= 0) {
            setActive(false);
            return;
        }
        recharge(RECHARGE_RATE);
    }

    @Override
    public void recharge(float health) {
        if(!isShielded()) return;
        float healthToAdd = Math.min(health, getMaxHealth() - getHealth());
        if(healthToAdd == 0) return;
        if(energy() != null) {
            int energyToExtract = (int) (ENERGY_PER_HEALTH_COST * healthToAdd);
            if(energy().extractEnergy(energyToExtract, true) < energyToExtract) {
                return;
            }
            energy().extractEnergy(energyToExtract, false);
        }
        setHealth(getHealth() + healthToAdd);
    }

    @Override
    public boolean isShielded() {
        return getHealth() > 0;
    }

    @Override
    public float getHealth() {
        return dataManager.get(SHIELD_HEALTH);
    }

    private void setHealth(float value) {
        dataManager.set(SHIELD_HEALTH, value);
    }

    @Override
    public float getMaxHealth() {
        return BASE_HEALTH + perks().getLevel(ModPerks.PERK_SHIELD.get()) * 2;
    }

    @Override
    public boolean setActive(boolean activation) {
        if(energy() != null && activation) {
            if(energy().extractEnergy(ACTIVATION_ENERGY_COST, true) < ACTIVATION_ENERGY_COST) return false;
            energy().extractEnergy(ACTIVATION_ENERGY_COST, false);
        }
        AttributeInstance instance = entity.getAttribute(ModAttributes.ENERGY_CONSUMPTION);
        if(instance != null) {
            if(activation) {
                instance.addPermanentModifier(ENERGY_UPKEEP);
            } else {
                instance.removeModifier(ENERGY_UPKEEP);
            }
        }
        if(activation) {
            setHealth(BASE_HEALTH);
            entity.playSound(ModSounds.SHIELD_UP.get());
        } else setHealth(0);
        return true;
    }

    private ModifiableEnergyStorage energy() {
        if(entity.getCapability(ForgeCapabilities.ENERGY).isPresent() && entity.getCapability(ForgeCapabilities.ENERGY).resolve().get() instanceof ModifiableEnergyStorage modifiableEnergyStorage) {
            energy = modifiableEnergyStorage;
        }
        return energy;
    }

    private IPerkMap perks() {
        if(perks == null) {
            entity.getCapability(ModCapabilities.PERKS).ifPresent(perks -> this.perks = perks);
        }
        return perks;
    }
}
