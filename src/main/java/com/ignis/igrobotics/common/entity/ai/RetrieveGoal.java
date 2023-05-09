package com.ignis.igrobotics.common.entity.ai;

import com.ignis.igrobotics.common.RobotBehavior;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.items.IItemHandler;

import java.lang.ref.WeakReference;
import java.util.EnumSet;

public class RetrieveGoal extends Goal {
	
	final Mob entity;
	private final WeakReference<Player> fakePlayer;
	BlockPos target, adjacent;
	ItemStack toTake;
	boolean taskFinished;
	private int tickCounter, takeItemsCounter, awayCounter;
	protected int takeItemTime;
	private final int maxStay, minAway;
	IItemHandler openedInventory;
	Container openedContainer;
	
	/**
	 * AI Task for an entity to walk to a storage container and take out the specified ItemStacks
	 * @param mob the entity that should interact with the storage
	 * @param from the position of the storage
	 * @param toTake the ItemStacks to take, matched along Item & Damage
	 * @param time the time it should take to take out 1 stack
	 * @param maxStay maximum ticks to stay at a location when no operation is possible
	 * @param minAway Minimum ticks to stay away from the chest and do other tasks after this task ran out
	 */
	public RetrieveGoal(Mob mob, BlockPos from, ItemStack toTake, int time, int maxStay, int minAway) {
		this.entity = mob;
		fakePlayer = new WeakReference<>(FakePlayerFactory.getMinecraft((ServerLevel) entity.level));
		this.target = from;
		this.toTake = toTake;
		this.maxStay = maxStay;
		this.minAway = minAway;
		awayCounter = minAway; //On instantiation this task should immediately be usable
		this.takeItemTime = time;
		setFlags(EnumSet.of(Flag.MOVE, Flag.TARGET, Flag.LOOK));
	}

	@Override
	public boolean canUse() {
		return awayCounter++ > minAway;
	}
	
	@Override
	public boolean canContinueToUse() {
		return !taskFinished;
	}
	
	@Override
	public void start() {
		adjacent = findBestPosNextTo(target);
		double speed = entity.getAttributeValue(Attributes.MOVEMENT_SPEED);
		this.entity.getNavigation().moveTo(adjacent.getX() + 0.5, adjacent.getY() + 0.5, adjacent.getZ() + 0.5, speed);
	}
	
	@Override
	public void tick() {
		if(!RobotBehavior.canReach(entity, target)) {
            ++this.tickCounter;
            takeItemsCounter = 0;

            if (this.tickCounter % 40 == 0) {
            	adjacent = findBestPosNextTo(target);
				double speed = entity.getAttributeValue(Attributes.MOVEMENT_SPEED);
                this.entity.getNavigation().moveTo((adjacent.getX()) + 0.5D, adjacent.getY() + 0.5, adjacent.getZ() + 0.5D, speed);
            }
            return;
        }
		openedInventory = openContainer(target);
		if(openedInventory == null) { //The target either is not a valid container (anymore or never was)
			taskFinished = true;
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
		//FIXME: closeContainer really needs to be called. Does this happen when the entity dies?
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

	protected void interactWithContainer(IItemHandler blockInventory, int ticks) {
		if(ticks % takeItemTime != 0) return;
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
	
	private BlockPos findBestPosNextTo(BlockPos pos) {
		if(adjacent != null) return adjacent;
		double distance = Double.MAX_VALUE;
		BlockPos result = pos;
		for(int i = -1; i <= 1; i += 2) {
			for(int j = -1; j <= 1; j += 2) {
				BlockPos considered_pos = pos.offset(i, 0, j);
				boolean isFullBlock = entity.level.getBlockState(considered_pos.above()).isCollisionShapeFullBlock(entity.level, considered_pos.above());
				if(entity.distanceToSqr(considered_pos.getCenter()) < distance && !isFullBlock) {
					distance = entity.distanceToSqr(considered_pos.getCenter());
					result = considered_pos;
				}
			}
		}
		return result;
	}
	
	public IItemHandler openContainer(BlockPos pos) {
		if(openedInventory != null) return openedInventory;
		BlockEntity tile = entity.level.getBlockEntity(pos);
		if(!canInteractWith(tile)) return null;
		IItemHandler handler = tile.getCapability(ForgeCapabilities.ITEM_HANDLER).orElse(null);
		if(tile instanceof Container container && openedContainer == null) {
			openedContainer = container;
			container.startOpen(getFakePlayer());
		}
		return handler;
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
