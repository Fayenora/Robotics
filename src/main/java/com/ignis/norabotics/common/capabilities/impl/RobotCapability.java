package com.ignis.norabotics.common.capabilities.impl;

import com.ignis.norabotics.common.WorldData;
import com.ignis.norabotics.common.access.AccessConfig;
import com.ignis.norabotics.common.capabilities.ICommandable;
import com.ignis.norabotics.common.capabilities.IPartBuilt;
import com.ignis.norabotics.common.capabilities.IRobot;
import com.ignis.norabotics.common.capabilities.ModCapabilities;
import com.ignis.norabotics.common.content.entity.RobotEntity;
import com.ignis.norabotics.common.content.entity.ai.LookDownGoal;
import com.ignis.norabotics.common.content.entity.ai.PickupGoal;
import com.ignis.norabotics.common.misc.ModifiableExplosion;
import com.ignis.norabotics.integration.config.RoboticsConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.event.ForgeEventFactory;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.*;

public class RobotCapability implements IRobot {

    public static final int MAX_SWELL = 60;

    protected final LivingEntity entity;
    protected final SynchedEntityData dataManager;
    private AccessConfig access = new AccessConfig();



    private static final EntityDataAccessor<Boolean> ACTIVATED = RobotEntity.ACTIVATED;
    private static final EntityDataAccessor<Boolean> MUTED = RobotEntity.MUTED;
    private static final EntityDataAccessor<Integer> LOAD_CHUNK = RobotEntity.LOAD_CHUNK;
    private static final EntityDataAccessor<Integer> PICKUP_STATE = RobotEntity.PICKUP_STATE;
    private static final EntityDataAccessor<Integer> COMMAND_GROUP = RobotEntity.COMMAND_GROUP;
    private static final EntityDataAccessor<Integer> SWELLING = RobotEntity.SWELLING;

    public PickupGoal pickUpGoal;

    private float explosionDamage, explosionRadius;

    public RobotCapability(Mob entity) {
        this.entity = entity;
        this.dataManager = entity.getEntityData();

        dataManager.define(ACTIVATED, true);
        dataManager.define(MUTED, false);
        dataManager.define(LOAD_CHUNK, 0);
        dataManager.define(PICKUP_STATE, 0);
        dataManager.define(COMMAND_GROUP, 0);
        dataManager.define(SWELLING, 0);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putBoolean("active", isActive());
        nbt.putBoolean("muted", isMuffled());
        nbt.putInt("load_chunks", getChunkLoadingState());
        nbt.putInt("pickup_state", getPickUpState());
        nbt.putInt("command_group", getCommandGroup());
        nbt.putUUID("owner", getOwner());
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        setActivation(nbt.getBoolean("active"));
        setMuffled(nbt.getBoolean("muted"));
        setChunkLoading(nbt.getInt("load_chunks"));
        setPickUpState(nbt.getInt("pickup_state"));
        setCommandGroup(nbt.getInt("command_group"));
        setOwner(nbt.getUUID("owner"));
    }

    @Override
    public boolean isActive() {
        return dataManager.get(ACTIVATED);
    }

    @Override
    public void setActivation(boolean activation) {
        Optional<IEnergyStorage> storage = entity.getCapability(ForgeCapabilities.ENERGY).resolve();
        if(storage.isPresent() && storage.get().getEnergyStored() <= 0) {
            activation = false;
        }
        if(activation == isActive()) return;
        Optional<IPartBuilt> parts = entity.getCapability(ModCapabilities.PARTS).resolve();
        Optional<ICommandable> commands = entity.getCapability(ModCapabilities.COMMANDS).resolve();

        // NOTE Maybe make this fire as an event?
        dataManager.set(ACTIVATED, activation);

        // Alter pickup & chunkloading behavior
        if(!activation) {
            if(RoboticsConfig.general.pickUpShutdown.get() && entity instanceof Mob mob) {
                mob.setCanPickUpLoot(false);
            }
            if(RoboticsConfig.general.chunkLoadShutdown.get()) {
                setChunkLoading(0);
            }
        } else {
            setPickUpState(getPickUpState());//Reapply the rule the robot had when active (if there is such a rule saved)
        }

        // Stop/Resume commands
        if(commands.isPresent() && entity instanceof Mob mob) {
            if(activation) {
                mob.goalSelector.getRunningGoals().forEach(WrappedGoal::stop);
                mob.goalSelector.removeAllGoals(goal -> goal instanceof LookDownGoal);
                commands.get().reapplyAllTasks();
            } else {
                commands.get().removeAllTasks();
                mob.goalSelector.addGoal(0, new LookDownGoal(mob));
            }
        }

        // Reapply color
        if(parts.isPresent()) {
            if(activation) {
                parts.get().setTemporaryColor(parts.get().getColor());
            } else {
                parts.get().setTemporaryColor(DyeColor.GRAY);
            }
        }
    }

    @Override
    public void setChunkLoading(int state) {
        entity.getCapability(ModCapabilities.CHUNK_LOADER).ifPresent(loader -> {
            loader.unloadChunks(new ChunkPos(entity.blockPosition()));
        });
        dataManager.set(LOAD_CHUNK, state);
        entity.getCapability(ModCapabilities.CHUNK_LOADER).ifPresent(loader -> {
            loader.loadChunks(new ChunkPos(entity.blockPosition()));
        });
    }

    @Override
    public int getChunkLoadingState() {
        return dataManager.get(LOAD_CHUNK);
    }

    @Override
    public void setCommandGroup(int group) {
        WorldData data = WorldData.get();
        if(group == 0) {
            data.releaseRobotFromCommandGroup(entity);
        } else {
            data.cacheRobotForCommandGroup(group, entity);
        }
        dataManager.set(COMMAND_GROUP, group);
    }

    @Override
    public int getCommandGroup() {
        return dataManager.get(COMMAND_GROUP);
    }

    @Override
    public void setPickUpState(int state) {
        dataManager.set(PICKUP_STATE, state);
        //We can't add any goals while the entity is still being loaded/initialised
        if(!(entity instanceof Mob mob)) return;
        if(pickUpGoal == null) {
            pickUpGoal = new PickupGoal(mob, 16);
        }
        mob.setCanPickUpLoot(state % 3 == 2);
        if(state % 3 == 1) {
            mob.goalSelector.addGoal(CommandCapability.MAX_NON_COMMAND_GOALS - 2, pickUpGoal);
        } else mob.goalSelector.removeGoal(pickUpGoal);
    }

    @Override
    public int getPickUpState() {
        return dataManager.get(PICKUP_STATE);
    }

    @Override
    public void setMuffled(boolean muffled) {
        dataManager.set(MUTED, muffled);
    }

    @Override
    public boolean isMuffled() {
        return dataManager.get(MUTED);
    }

    @Override
    public void setOwner(UUID newOwner) {
        access.setOwner(newOwner);
    }

    @Override
    public @NonNull UUID getOwner() {
        return access.getOwner();
    }

    @Override
    public void setAccess(AccessConfig access) {
        this.access = access;
    }

    @Override
    public AccessConfig getAccess() {
        return access; //TODO: Culminate across scopes
    }

    @Override
    public boolean isSwelling() {
        return dataManager.get(SWELLING) > 0;
    }

    @Override
    public int getSwell() {
        return dataManager.get(SWELLING);
    }

    @Override
    public void swell() {
        dataManager.set(SWELLING, (dataManager.get(SWELLING) + 1));
        if(dataManager.get(SWELLING) > MAX_SWELL) {
            explode();
        }
    }

    @Override
    public void igniteExplosion(float damage, float radius) {
        entity.playSound(SoundEvents.CREEPER_PRIMED);
        this.explosionDamage = damage;
        this.explosionRadius = radius;
        dataManager.set(SWELLING, 1);
    }

    protected void explode() {
        entity.setInvulnerable(true);
        Explosion.BlockInteraction interaction =  Explosion.BlockInteraction.DESTROY;
        if(entity.level().getGameRules().getBoolean(GameRules.RULE_MOB_EXPLOSION_DROP_DECAY)) interaction = Explosion.BlockInteraction.DESTROY_WITH_DECAY;
        if(!ForgeEventFactory.getMobGriefingEvent(entity.level(), entity)) interaction = Explosion.BlockInteraction.KEEP;
        Explosion explosion = new ModifiableExplosion(entity, explosionDamage, explosionRadius, true, interaction);
        if(ForgeEventFactory.onExplosionStart(entity.level(), explosion)) return;
        explosion.explode();
        explosion.finalizeExplosion(true);
    }
}
