package com.io.norabotics.common.content.entity;

import com.io.norabotics.common.capabilities.IPartBuilt;
import com.io.norabotics.common.capabilities.ModCapabilities;
import com.io.norabotics.common.capabilities.impl.CommandCapability;
import com.io.norabotics.common.content.entity.ai.RangedBowAttack;
import com.io.norabotics.common.content.entity.ai.RangedCrossbowAttack;
import com.io.norabotics.common.content.entity.ai.RangedGenericAttack;
import com.io.norabotics.common.content.entity.ai.ReachAcrossDimensionGoal;
import com.io.norabotics.common.handlers.RobotBehavior;
import com.io.norabotics.common.helpers.util.InventoryUtil;
import com.io.norabotics.common.robot.*;
import com.io.norabotics.definitions.ModEntityTypes;
import com.io.norabotics.definitions.ModItems;
import com.io.norabotics.definitions.ModSounds;
import com.io.norabotics.definitions.robotics.ModModules;
import com.io.norabotics.integration.config.RoboticsConfig;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.NonNullList;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
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
    public static final EntityDataAccessor<Integer> PICKUP_STATE = SynchedEntityData.defineId(RobotEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> COMMAND_GROUP = SynchedEntityData.defineId(RobotEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> SWELLING = SynchedEntityData.defineId(RobotEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Float> SHIELD_HEALTH = SynchedEntityData.defineId(RobotEntity.class, EntityDataSerializers.FLOAT);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private int swingTime = 0;

    public RobotEntity(Level level) {
        this(level, RoboticsConfig.general.startColor.get());
    }

    public RobotEntity(Level pLevel, DyeColor color) {
        super(ModEntityTypes.ROBOT.get(), pLevel);
        getCapability(ModCapabilities.PARTS).ifPresent(part -> part.setColor(color));
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(4, new RangedGenericAttack(this, 2, 8, 2));
        this.goalSelector.addGoal(4, new RangedBowAttack(this, 2, 8, 4));
        this.goalSelector.addGoal(4, new RangedCrossbowAttack(this, 2, 8, 1));
        this.goalSelector.addGoal(5, new MeleeAttackGoal(this, 1, true));
        this.goalSelector.addGoal(8, new ReachAcrossDimensionGoal(this, 64));
        this.goalSelector.addGoal(CommandCapability.MAX_COMMANDS + CommandCapability.MAX_NON_COMMAND_GOALS + 2, new LookAtPlayerGoal(this, Player.class, 6));
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if(!getCapability(ModCapabilities.PARTS).isPresent() || level().isClientSide) return InteractionResult.PASS;
        IPartBuilt parts = getCapability(ModCapabilities.PARTS).resolve().get();
        EnumRobotMaterial repairMaterial = parts.materialForSlot(EnumModuleSlot.BODY);
        RegistryObject<Item> repairItem = ModItems.PLATES.get(repairMaterial);
        ItemStack stack = player.getItemInHand(hand);
        if(repairItem != null && stack.getItem().equals(repairItem.get()) && getHealth() < getMaxHealth()) {
            if(!player.isCreative()) stack.setCount(stack.getCount() - 1);
            setHealth(getHealth() + 5);
            playSound(SoundEvents.ANVIL_USE);
            return InteractionResult.CONSUME;
        }
        RobotPart part = RobotPart.getFromItem(stack.getItem());
        if(part != null && (!parts.hasBodyPart(part.getPart().toModuleSlot()) || player.isCreative())) {
            if(!player.isCreative()) stack.setCount(stack.getCount() - 1);
            parts.setBodyPart(part);
            playSound(SoundEvents.ANVIL_USE);
            return InteractionResult.CONSUME;
        }
        if(ModModules.isModule(stack) && player.isCreative()) {
            RobotModule module = ModModules.get(stack);
            for(EnumModuleSlot slot : module.getViableSlots()) {
                NonNullList<ItemStack> items = parts.getBodyParts(slot);
                NonNullList<ItemStack> copy = NonNullList.withSize(items.size(), ItemStack.EMPTY);
                int i = 0;
                if(items.isEmpty()) return InteractionResult.PASS;
                while(i < items.size() && !items.get(i).isEmpty()) {
                    copy.set(i++, items.get(i));
                }
                copy.set(i, stack);
                parts.setBodyParts(slot, copy);
                playSound(SoundEvents.ANVIL_USE);
                break;
            }
            return InteractionResult.CONSUME;
        }

        return InteractionResult.PASS;
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

    @Override
    public int getAirSupply() {
        return super.getMaxAirSupply();
    }

    @Override
    public ItemStack equipItemIfPossible(ItemStack stack) {
        //TODO Move this to a mixin/event
        ItemStack pickedUpStack = super.equipItemIfPossible(stack);
        if(!pickedUpStack.isEmpty()) return pickedUpStack;
        if(getCapability(ForgeCapabilities.ITEM_HANDLER).isPresent()) {
            Optional<IItemHandler> inventory = getCapability(ForgeCapabilities.ITEM_HANDLER).resolve();
            if(inventory.isPresent()) {
                stack.setCount(stack.getCount() - InventoryUtil.insert(inventory.get(), stack, false).getCount());
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    /////////////////
    // Animations
    /////////////////


    @Override
    public void swing(InteractionHand p_21012_, boolean p_21013_) {
        swingTime = RobotBehavior.swingSpeed(this) * 3;
        super.swing(p_21012_, p_21013_);
    }

    private <T extends GeoAnimatable> PlayState getAnimation(AnimationState<T> animationState) {
        // TODO Use native walkanimation attributes in living entity
        if(animationState.isMoving()) {
            animationState.getController().setAnimation(RawAnimation.begin().then("animation.robot.walk", Animation.LoopType.LOOP));
            return PlayState.CONTINUE;
        }
        if(swingTime-- > 0) {
            Animation.LoopType animationType = swingTime > RobotBehavior.swingSpeed(this) * 3 / 2 ? Animation.LoopType.LOOP : Animation.LoopType.PLAY_ONCE;
            switch(swingingArm) {
                case MAIN_HAND -> animationState.getController().setAnimation(RawAnimation.begin().then("animation.robot.interactRight", animationType));
                case OFF_HAND -> animationState.getController().setAnimation(RawAnimation.begin().then("animation.robot.interactLeft", animationType));
            }
            return PlayState.CONTINUE;
        }
        animationState.getController().setAnimation(RawAnimation.begin().then("animation.robot.idle", Animation.LoopType.LOOP));
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, this::getAnimation));
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
