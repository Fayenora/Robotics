package com.ignis.igrobotics.common.entity;

import com.ignis.igrobotics.core.capabilities.ModCapabilities;
import com.ignis.igrobotics.core.capabilities.commands.CommandCapability;
import com.ignis.igrobotics.core.util.ItemStackUtils;
import com.ignis.igrobotics.definitions.ModEntityTypes;
import com.ignis.igrobotics.definitions.ModItems;
import com.ignis.igrobotics.definitions.ModSounds;
import com.ignis.igrobotics.integration.config.RoboticsConfig;
import net.minecraft.MethodsReturnNonnullByDefault;
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
import net.minecraft.world.entity.ai.attributes.Attributes;
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
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
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

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public RobotEntity(Level level) {
        this(level, RoboticsConfig.general.startColor.get());
    }

    public RobotEntity(Level pLevel, DyeColor color) {
        super(ModEntityTypes.ROBOT.get(), pLevel);
        getCapability(ModCapabilities.PARTS).ifPresent(part -> part.setColor(color));
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(5, new MeleeAttackGoal(this, getAttributeValue(Attributes.MOVEMENT_SPEED), true));
        this.goalSelector.addGoal(CommandCapability.MAX_COMMANDS + CommandCapability.MAX_NON_COMMAND_GOALS + 2, new LookAtPlayerGoal(this, Player.class, 6));
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        boolean isPlate = false;
        for(RegistryObject<Item> plate : ModItems.PLATES) {
            if(plate.get().equals(stack.getItem())) {
                isPlate = true;
            }
        }
        if(!isPlate) return InteractionResult.PASS;
        if(!player.isCreative()) stack.setCount(stack.getCount() - 1);
        setHealth(getHealth() + 5);
        playSound(SoundEvents.ANVIL_USE);
        return InteractionResult.CONSUME;
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
    public ItemStack equipItemIfPossible(ItemStack stack) {
        //TODO Move this to a mixin/event
        ItemStack pickedUpStack = super.equipItemIfPossible(stack);
        if(!pickedUpStack.isEmpty()) return pickedUpStack;
        if(getCapability(ForgeCapabilities.ITEM_HANDLER).isPresent()) {
            Optional<IItemHandler> inventory = getCapability(ForgeCapabilities.ITEM_HANDLER).resolve();
            if(inventory.isPresent()) {
                stack.setCount(stack.getCount() - ItemStackUtils.insert(inventory.get(), stack, false).getCount());
                return stack;
            }
        }
        return ItemStack.EMPTY;
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
