package com.ignis.igrobotics.common.entity;

import com.ignis.igrobotics.core.capabilities.ModCapabilities;
import com.ignis.igrobotics.definitions.ModEntityTypes;
import com.ignis.igrobotics.definitions.ModSounds;
import com.ignis.igrobotics.integration.config.RoboticsConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Arrays;

public class RobotEntity extends PathfinderMob implements GeoEntity {

    public static final EntityDataAccessor<Integer> COLOR = SynchedEntityData.defineId(RobotEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer>[] BODY_PARTS = new EntityDataAccessor[] {
            SynchedEntityData.defineId(RobotEntity.class, EntityDataSerializers.INT),
            SynchedEntityData.defineId(RobotEntity.class, EntityDataSerializers.INT),
            SynchedEntityData.defineId(RobotEntity.class, EntityDataSerializers.INT),
            SynchedEntityData.defineId(RobotEntity.class, EntityDataSerializers.INT),
            SynchedEntityData.defineId(RobotEntity.class, EntityDataSerializers.INT),
            SynchedEntityData.defineId(RobotEntity.class, EntityDataSerializers.INT)
    };
    public static final EntityDataAccessor<Integer> RENDER_OVERLAYS = SynchedEntityData.defineId(RobotEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Boolean> ACTIVATED = SynchedEntityData.defineId(RobotEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> MUTED = SynchedEntityData.defineId(RobotEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Integer> LOAD_CHUNK = SynchedEntityData.defineId(RobotEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> PICKUPSTATE = SynchedEntityData.defineId(RobotEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> COMMAND_GROUP = SynchedEntityData.defineId(RobotEntity.class, EntityDataSerializers.INT);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public RobotEntity(Level level) {
        this(level, RoboticsConfig.general.startColor.get());
    }

    public RobotEntity(Level pLevel, DyeColor color) {
        super(ModEntityTypes.ROBOT.get(), pLevel);
        getCapability(ModCapabilities.PARTS).ifPresent(part -> part.setColor(color));
        Arrays.fill(this.armorDropChances, 0); //These would be randomly damaged
        Arrays.fill(this.handDropChances, 0); //These would be randomly damaged
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, getAttributeValue(Attributes.MOVEMENT_SPEED), true));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6));
    }

    @Override
    public boolean canBeAffected(MobEffectInstance instance) {
        if(instance.getEffect().equals(MobEffects.POISON) && RoboticsConfig.general.poisonImmunity.get()) return false;
        return super.canBeAffected(instance);
    }

    @Override
    public void heal(float p_21116_) {
        //Prevent any health regeneration
    }

    /////////////////
    // Animations
    /////////////////

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "idle", 5, state -> state.setAndContinue(DefaultAnimations.IDLE)));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    protected @NotNull BodyRotationControl createBodyControl() {
        return new RobotBodyControl(this);
    }

    ////////////////
    // Sounds
    ////////////////

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.ROBOT_DEATH.get();
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return ModSounds.ROBOT_HURT.get();
    }
}
