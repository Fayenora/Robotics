package com.io.norabotics.common.content.entity.ai;

import com.io.norabotics.common.handlers.RobotBehavior;
import com.io.norabotics.common.helpers.DimensionNavigator;
import com.io.norabotics.definitions.ModAttributes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.items.IItemHandler;

import java.lang.ref.WeakReference;
import java.util.EnumSet;
import java.util.Optional;

public class RetrieveGoal extends Goal {

	protected final Mob entity;
	protected final DimensionNavigator navigator;
	private final WeakReference<Player> fakePlayer;
	GlobalPos target, adjacent;
	ItemStack toTake;
	boolean taskFinished, invalid;
	private int tickCounter, takeItemsCounter, awayCounter;
	private final int takeItemTime;
	private final int maxStay, minAway;
	IItemHandler openedInventory;
	Container openedContainer;

	/**
	 * AI Task for an entity to walk to a storage container and take out the specified ItemStacks
	 * @param mob the entity that should interact with the storage
	 * @param from the position of the storage
	 * @param toTake the ItemStacks to take, matched along Item & Damage
	 * @param time the base time it should take to take out 1 stack. Is modified by attributes
	 * @param maxStay maximum ticks to stay at a location when no operation is possible
	 * @param minAway Minimum ticks to stay away from the chest and do other tasks after this task ran out
	 */
	public RetrieveGoal(Mob mob, ItemStack toTake, GlobalPos from, int time, int maxStay, int minAway) {
		this.entity = mob;
		navigator = new DimensionNavigator(mob, 16, 16, 1);
		fakePlayer = new WeakReference<>(FakePlayerFactory.getMinecraft((ServerLevel) entity.level()));
		this.target = from;
		this.toTake = toTake;
		this.maxStay = maxStay;
		this.minAway = minAway;
		awayCounter = minAway; //On instantiation this task should immediately be usable
		this.takeItemTime = time;
		setFlags(EnumSet.of(Flag.MOVE, Flag.TARGET, Flag.LOOK));
	}

	public RetrieveGoal(Mob mob, ItemStack toTake, GlobalPos from) {
		this(mob, toTake, from, 20, 400, 200);
	}

	@Override
	public boolean canUse() {
		taskFinished = false;
		return awayCounter++ > minAway && !invalid;
	}

	@Override
	public boolean canContinueToUse() {
		return !taskFinished;
	}

	@Override
	public void start() {
		adjacent = findBestPosNextTo(target);
		navigator.navigateTo(adjacent);
	}

	@Override
	public void tick() {
		if(!RobotBehavior.canReach(entity, target)) {
            ++this.tickCounter;
            takeItemsCounter = 0;

            if (this.tickCounter % 40 == 0) {
				adjacent = findBestPosNextTo(target);
				navigator.navigateTo(adjacent);
            }
            return;
        }
		openedInventory = openContainer(target);
		if(openedInventory == null) { //The target either is not a valid container (anymore or never was)
			taskFinished = true;
			invalid = true;
			return;
		}

		takeItemsCounter++;
        interactWithContainer(openedInventory, takeItemsCounter);

		if(takeItemsCounter > maxStay) {
			taskFinished = true;
		}
	}

	@Override
	public void stop() {
		//NOTE: {@link RobotBehavior} handles the case the entity dies while having the container opened
		if(taskFinished || entity.isDeadOrDying()) {
			closeContainer();
		}
		takeItemsCounter = 0;
		awayCounter = 0;
		adjacent = null;
	}

	@Override
	public boolean isInterruptable() {
		return false;
	}

	protected int getTakeItemTime() {
		return (int) (takeItemTime * entity.getAttributeValue(ModAttributes.LOGISTICS_TIME));
	}

	protected void interactWithContainer(IItemHandler blockInventory, int ticks) {
		if(ticks % getTakeItemTime() != 0) return;
		entity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(entityInventory -> {
			for(int i = 0; i < blockInventory.getSlots(); i++) {
				ItemStack inSlot = blockInventory.extractItem(i, Integer.MAX_VALUE, true);
				if(inSlot.isEmpty()) continue;
				if(toTake == null || !inSlot.getItem().equals(toTake.getItem())) continue;

				//Extract the items and put them into the entity inventory
				ItemStack extracted = blockInventory.extractItem(i, inSlot.getCount(), false);
				for(int j = 0; j < entityInventory.getSlots(); j++) {
					extracted = entityInventory.insertItem(j, extracted, false);
				}
				//If not all the taken items fit, put the rest back
				if(extracted.getCount() > 0) {
					blockInventory.insertItem(i, extracted, false);
				}
				return;
			}

			//No items of the desired type are left
			taskFinished = true;
		});
	}

	private GlobalPos findBestPosNextTo(GlobalPos pos) {
		if(adjacent != null) return adjacent;
		Level level = entity.getServer().getLevel(pos.dimension());
		if(level == null) return pos;
		double distance = Double.MAX_VALUE;
		BlockPos result = pos.pos();
		BlockPos.MutableBlockPos considered_pos = result.mutable();
		for(int i = -1; i <= 1; i += 2) {
			for(int j = -1; j <= 1; j += 2) {
				considered_pos.setWithOffset(pos.pos(), i, 0, j);
				boolean isFullBlock = level.getBlockState(considered_pos.above()).isCollisionShapeFullBlock(level, considered_pos.above());
				if(entity.distanceToSqr(considered_pos.getCenter()) < distance && !isFullBlock) {
					distance = entity.distanceToSqr(considered_pos.getCenter());
					result = considered_pos;
				}
			}
		}
		return GlobalPos.of(pos.dimension(), result);
	}

	public IItemHandler openContainer(GlobalPos pos) {
		if(openedInventory != null) return openedInventory;
		Level level = entity.getServer().getLevel(pos.dimension());
		if(level == null) return null;
		BlockEntity tile = level.getBlockEntity(pos.pos());
		if(!canInteractWith(tile)) return null;
		Optional<IItemHandler> handler = tile.getCapability(ForgeCapabilities.ITEM_HANDLER).resolve();
		if(handler.isEmpty()) return null;
		if(tile instanceof Container container && openedContainer == null) {
			openedContainer = container;
			container.startOpen(getFakePlayer());
		}
		return handler.get();
	}

	public void closeContainer() {
		if(openedContainer == null) return;
		openedContainer.stopOpen(getFakePlayer());
		openedContainer = null;
		openedInventory = null;
	}

	private boolean canInteractWith(BlockEntity tile) {
		if(tile == null) return false;
		if(!tile.getCapability(ForgeCapabilities.ITEM_HANDLER).isPresent()) return false;
		if(tile instanceof BaseContainerBlockEntity containerBlockEntity) return containerBlockEntity.canOpen(getFakePlayer());
		return true;
	}

	private Player getFakePlayer() {
		Player player = fakePlayer.get();
		player.setPos(entity.getX(), entity.getY(), entity.getZ());
		player.setItemSlot(EquipmentSlot.MAINHAND, entity.getMainHandItem());
		return player;
	}

	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof RetrieveGoal retrieveGoal)) return false;
		return entity.equals(retrieveGoal.entity) && target.equals(retrieveGoal.target) && toTake.equals(retrieveGoal.toTake);
	}
}
