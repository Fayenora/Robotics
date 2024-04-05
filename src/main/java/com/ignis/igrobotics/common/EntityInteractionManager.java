package com.ignis.igrobotics.common;

import com.ignis.igrobotics.Reference;
import com.ignis.igrobotics.common.handlers.RobotBehavior;
import com.ignis.igrobotics.core.RobotFakePlayerFactory;
import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GameMasterBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import java.lang.ref.WeakReference;

/**
 * A class for enabling a mob to perform player actions like digging and right-clicking
 * The code is taken from {@link net.minecraft.server.level.ServerPlayerGameMode}
 * NOTE: This should be kept up to date with vanilla code
 */
@FieldsAreNonnullByDefault
@SuppressWarnings("Argument might be null")
public class EntityInteractionManager {

    protected ServerLevel level;
    private final Mob mob;
    protected final WeakReference<Player> fakePlayer;
    private boolean isDestroyingBlock;
    private int destroyProgressStart;
    private BlockPos destroyPos = BlockPos.ZERO;
    private int gameTicks;
    private boolean hasDelayedDestroy;

    private static final GameType gameType = GameType.SURVIVAL;

    public EntityInteractionManager(Mob mob) {
        this.mob = mob;
        this.level = (ServerLevel) mob.level;
        fakePlayer = new WeakReference<>(RobotFakePlayerFactory.get(mob, Reference.PROFILE));
    }

    private int lastSentState = -1;

    public boolean dig(BlockPos pos, Direction side) {
        ++this.gameTicks;
        mob.swing(mob.getUsedItemHand());

        if(!destroyPos.equals(pos) || !isDestroyingBlock) {
            startDestroyingBlock(pos, side, 0);
        }

        BlockState state = level.getBlockState(pos);

        if(incrementDestroyProgress(state, pos, destroyProgressStart) >= 1) {
            destroyBlock(destroyPos);
            destroyPos = BlockPos.ZERO;
            this.isDestroyingBlock = false;
            this.lastSentState = -1;
            return true;
        }

        return false;
    }

    private float incrementDestroyProgress(BlockState state, BlockPos pos, int startTime) {
        int i = this.gameTicks - startTime;
        float f = state.getDestroyProgress(this.fakePlayer.get(), level, pos) * (float)(i + 1);
        int j = (int)(f * 10.0F);
        if (j != this.lastSentState) {
            this.level.destroyBlockProgress(mob.getId(), pos, j);
            this.lastSentState = j;
        }

        return f;
    }

    private void debugLogging(BlockPos pos, boolean p_215127_, int p_215128_, String message) {
    }

    public void startDestroyingBlock(BlockPos pos, Direction side, int p_215124_) {
        PlayerInteractEvent.LeftClickBlock event = ForgeHooks.onLeftClickBlock(fakePlayer.get(), pos, side);
        if (event.isCanceled() || event.getResult() == net.minecraftforge.eventbus.api.Event.Result.DENY) {
            return;
        }
        if (!RobotBehavior.canReach(mob, pos)) {
            this.debugLogging(pos, false, p_215124_, "too far");
            return;
        }
        if (!this.level.mayInteract(this.fakePlayer.get(), pos)) {
            //this.fakePlayer.connection.send(new ClientboundBlockUpdatePacket(p_215120_, this.level.getBlockState(p_215120_)));
            this.debugLogging(pos, false, p_215124_, "may not interact");
            return;
        }

        this.destroyProgressStart = this.gameTicks;
        float f = 1.0F;
        BlockState blockstate = this.level.getBlockState(pos);
        if (!blockstate.isAir()) {
            if (event.getUseBlock() != net.minecraftforge.eventbus.api.Event.Result.DENY)
                blockstate.attack(this.level, pos, fakePlayer.get());
            f = blockstate.getDestroyProgress(fakePlayer.get(), level, pos);
        }

        if (!blockstate.isAir() && f >= 1.0F) {
            this.destroyAndAck(pos, p_215124_, "insta mine");
        } else {
            if (this.isDestroyingBlock) {
                //this.fakePlayer.connection.send(new ClientboundBlockUpdatePacket(this.destroyPos, this.level.getBlockState(this.destroyPos)));
                this.debugLogging(pos, false, p_215124_, "abort destroying since another started (client insta mine, server disagreed)");
            }

            this.isDestroyingBlock = true;
            this.destroyPos = pos.immutable();
            int i = (int)(f * 10.0F);
            this.level.destroyBlockProgress(mob.getId(), pos, i);
            this.debugLogging(pos, true, p_215124_, "actual start of destroying");
            this.lastSentState = i;
        }
    }

    public void stopDestroyingBlock(int p_215124_) {
        int j = this.gameTicks - this.destroyProgressStart;
        BlockState blockstate1 = this.level.getBlockState(destroyPos);
        if (!blockstate1.isAir()) {
            float f1 = blockstate1.getDestroyProgress(fakePlayer.get(), level, destroyPos) * (float)(j + 1);
            if (f1 >= 0.7F) {
                this.isDestroyingBlock = false;
                this.level.destroyBlockProgress(mob.getId(), destroyPos, -1);
                this.destroyAndAck(destroyPos, p_215124_, "destroyed");
                return;
            }

            if (!this.hasDelayedDestroy) {
                this.isDestroyingBlock = false;
                this.hasDelayedDestroy = true;
            }
        }
        this.debugLogging(destroyPos, true, p_215124_, "stopped destroying");
    }

    public void cancelDestroyingBlock() {
        this.isDestroyingBlock = false;
        this.level.destroyBlockProgress(mob.getId(), destroyPos, -1);
        this.debugLogging(destroyPos, true, destroyProgressStart, "aborted destroying");
    }

    public void destroyAndAck(BlockPos pos, int p_215118_, String p_215119_) {
        if (this.destroyBlock(pos)) {
            this.debugLogging(pos, true, p_215118_, p_215119_);
        } else {
            //this.fakePlayer.connection.send(new ClientboundBlockUpdatePacket(p_215117_, this.level.getBlockState(p_215117_)));
            this.debugLogging(pos, false, p_215118_, p_215119_);
        }

    }

    public boolean destroyBlock(BlockPos pos) {
        if(!ForgeHooks.canEntityDestroy(level, pos, mob)) return false;
        BlockState blockstate = this.level.getBlockState(pos);
        int exp = ForgeHooks.onBlockBreakEvent(level, gameType, (ServerPlayer) fakePlayer.get(), pos);
        if (exp == -1) {
            return false;
        }
        BlockEntity blockentity = this.level.getBlockEntity(pos);
        Block block = blockstate.getBlock();
        if (block instanceof GameMasterBlock) {
            this.level.sendBlockUpdated(pos, blockstate, blockstate, 3);
            return false;
        }
        if (mob.getMainHandItem().onBlockStartBreak(pos, fakePlayer.get())) {
            return false;
        }

        ItemStack itemstack = mob.getMainHandItem();
        ItemStack itemstack1 = itemstack.copy();
        boolean flag1 = blockstate.canHarvestBlock(this.level, pos, fakePlayer.get()); // previously player.hasCorrectToolForDrops(blockstate)
        itemstack.mineBlock(this.level, blockstate, pos, fakePlayer.get());
        if (itemstack.isEmpty() && !itemstack1.isEmpty())
            net.minecraftforge.event.ForgeEventFactory.onPlayerDestroyItem(fakePlayer.get(), itemstack1, InteractionHand.MAIN_HAND);
        boolean flag = removeBlock(pos, flag1);

        if (flag && flag1) {
            block.playerDestroy(this.level, fakePlayer.get(), pos, blockstate, blockentity, itemstack1);
        }

        if (flag && exp > 0)
            blockstate.getBlock().popExperience(level, pos, exp);

        return true;
    }

    private boolean removeBlock(BlockPos p_180235_1_, boolean canHarvest) {
        BlockState state = this.level.getBlockState(p_180235_1_);
        boolean removed = state.onDestroyedByPlayer(level, p_180235_1_, fakePlayer.get(), canHarvest, this.level.getFluidState(p_180235_1_));
        if (removed)
            state.getBlock().destroy(this.level, p_180235_1_, state);
        return removed;
    }

    public InteractionResult useItem(ServerPlayer p_9262_, Level p_9263_, ItemStack p_9264_, InteractionHand p_9265_) {
        if (p_9262_.getCooldowns().isOnCooldown(p_9264_.getItem())) {
            return InteractionResult.PASS;
        } else {
            InteractionResult cancelResult = net.minecraftforge.common.ForgeHooks.onItemRightClick(p_9262_, p_9265_);
            if (cancelResult != null) return cancelResult;
            int i = p_9264_.getCount();
            int j = p_9264_.getDamageValue();
            InteractionResultHolder<ItemStack> interactionresultholder = p_9264_.use(p_9263_, p_9262_, p_9265_);
            ItemStack itemstack = interactionresultholder.getObject();
            if (itemstack == p_9264_ && itemstack.getCount() == i && itemstack.getUseDuration() <= 0 && itemstack.getDamageValue() == j) {
                return interactionresultholder.getResult();
            } else if (interactionresultholder.getResult() == InteractionResult.FAIL && itemstack.getUseDuration() > 0 && !p_9262_.isUsingItem()) {
                return interactionresultholder.getResult();
            } else {
                if (p_9264_ != itemstack) {
                    p_9262_.setItemInHand(p_9265_, itemstack);
                }

                if (itemstack.isEmpty()) {
                    p_9262_.setItemInHand(p_9265_, ItemStack.EMPTY);
                }

                if (!p_9262_.isUsingItem()) {
                    p_9262_.inventoryMenu.sendAllDataToRemote();
                }

                return interactionresultholder.getResult();
            }
        }
    }

    public InteractionResult useItemOn(ServerPlayer p_9266_, Level p_9267_, ItemStack p_9268_, InteractionHand p_9269_, BlockHitResult p_9270_) {
        BlockPos blockpos = p_9270_.getBlockPos();
        BlockState blockstate = p_9267_.getBlockState(blockpos);
        if (!blockstate.getBlock().isEnabled(p_9267_.enabledFeatures())) {
            return InteractionResult.FAIL;
        }
        net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock event = net.minecraftforge.common.ForgeHooks.onRightClickBlock(p_9266_, p_9269_, blockpos, p_9270_);
        if (event.isCanceled()) return event.getCancellationResult();
        UseOnContext useoncontext = new UseOnContext(p_9266_, p_9269_, p_9270_);
        if (event.getUseItem() != net.minecraftforge.eventbus.api.Event.Result.DENY) {
            InteractionResult result = p_9268_.onItemUseFirst(useoncontext);
            if (result != InteractionResult.PASS) return result;
        }
        boolean flag = !p_9266_.getMainHandItem().isEmpty() || !p_9266_.getOffhandItem().isEmpty();
        boolean flag1 = (p_9266_.isSecondaryUseActive() && flag) && !(p_9266_.getMainHandItem().doesSneakBypassUse(p_9267_, blockpos, p_9266_) && p_9266_.getOffhandItem().doesSneakBypassUse(p_9267_, blockpos, p_9266_));
        ItemStack itemstack = p_9268_.copy();
        if (event.getUseBlock() == net.minecraftforge.eventbus.api.Event.Result.ALLOW || (event.getUseBlock() != net.minecraftforge.eventbus.api.Event.Result.DENY && !flag1)) {
            InteractionResult interactionresult = blockstate.use(p_9267_, p_9266_, p_9269_, p_9270_);
            if (interactionresult.consumesAction()) {
                CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(p_9266_, blockpos, itemstack);
                return interactionresult;
            }
        }

        if (event.getUseItem() == net.minecraftforge.eventbus.api.Event.Result.ALLOW || (!p_9268_.isEmpty() && !p_9266_.getCooldowns().isOnCooldown(p_9268_.getItem()))) {
            if (event.getUseItem() == net.minecraftforge.eventbus.api.Event.Result.DENY) return InteractionResult.PASS;
            InteractionResult interactionresult1;
            interactionresult1 = p_9268_.useOn(useoncontext);

            if (interactionresult1.consumesAction()) {
                CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(p_9266_, blockpos, itemstack);
            }

            return interactionresult1;
        } else {
            return InteractionResult.PASS;
        }
    }

    public void setLevel(ServerLevel p_9261_) {
        this.level = p_9261_;
    }
}
