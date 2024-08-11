package com.ignis.igrobotics.common.content.entity.ai;

import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;

import java.util.concurrent.atomic.AtomicBoolean;

public class StoreGoal extends RetrieveGoal {

	public StoreGoal(Mob mob, ItemStack toStore, GlobalPos from) {
		super(mob, toStore, from);
	}
	
	@Override
	public boolean canUse() {
		AtomicBoolean containsItem = new AtomicBoolean(false);
		entity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(inventory -> {
			for(int i = 0; i < inventory.getSlots(); i++) {
				if(inventory.getStackInSlot(i).getItem().equals(toTake.getItem())) {
					containsItem.set(true);
					return;
				}
			}
		});
		return containsItem.get() && super.canUse();
	}
	
	@Override
	public boolean canContinueToUse() {
		AtomicBoolean containsItem = new AtomicBoolean(false);
		entity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(inventory -> {
			for(int i = 0; i < inventory.getSlots(); i++) {
				if(inventory.getStackInSlot(i).getItem().equals(toTake.getItem())) {
					containsItem.set(true);
					return;
				}
			}
		});
		return containsItem.get() && super.canContinueToUse();
	}
	
	@Override
	protected void interactWithContainer(IItemHandler inventory, int ticks) {
		if(ticks % getTakeItemTime() != 0) return;
		entity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(entityInventory -> {
			//Start with index 6 to avoid dumping any currently equipped items
			for(int i = 6; i < entityInventory.getSlots(); i++) {
				ItemStack stack = entityInventory.extractItem(i, Integer.MAX_VALUE, true);
				if(toTake == null || !stack.getItem().equals(toTake.getItem())) continue;

				stack = entityInventory.extractItem(i, Integer.MAX_VALUE, false);
				for(int j = 0; j < inventory.getSlots(); j++) {
					stack = inventory.insertItem(j, stack, false);
				}
				//Take the stack back, if it doesn't fit into the container
				if(stack.getCount() > 0) {
					entityInventory.insertItem(i, stack, false);
				}
			}

			//No items of the desired type are left
			taskFinished = true;
		});
	}

}
