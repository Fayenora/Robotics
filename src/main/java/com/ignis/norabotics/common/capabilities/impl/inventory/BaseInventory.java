package com.ignis.norabotics.common.capabilities.impl.inventory;

import com.ignis.norabotics.common.capabilities.ModifiableInventory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.function.Supplier;

public class BaseInventory extends ItemStackHandler implements ModifiableInventory {

	private Level world;
	private final Supplier<BlockPos> pos;
	
	public BaseInventory(Supplier<BlockPos> pos, int size) {
		super(size);
		this.pos = pos;
	}

	public void setLevel(Level level) {
		this.world = level;
	}

	@Override
	public void setSize(int newSize) {
		if(getSlots() == newSize) return;
		
		//Drop items that don't fit anymore
		if(world != null && !world.isClientSide() && newSize < getSlots()) {
			Containers.dropContents(world, pos.get(), subList(newSize, getSlots()));
			for(int i = newSize; i < getSlots(); i++) {
				stacks.set(i, ItemStack.EMPTY);
			}
		}

		//Change the list size
		NonNullList<ItemStack> newStacks = NonNullList.withSize(newSize, ItemStack.EMPTY);
		for(int i = 0; i < Math.min(newSize, getSlots()); i++) {
			newStacks.set(i, stacks.get(i));
		}
		stacks = newStacks;
	}

	@Override
	public @NotNull ItemStack getStackInSlot(int slot) {
		if(slot < 0 || slot >= stacks.size()) return ItemStack.EMPTY;
		return super.getStackInSlot(slot);
	}

	public boolean isEmpty() {
		for(ItemStack stack : stacks) {
			if(!stack.isEmpty()) return false;
		}
		return true;
	}

	public void clear() {
		Collections.fill(stacks, ItemStack.EMPTY);
	}

	public NonNullList<ItemStack> subList(int from, int to) {
		NonNullList<ItemStack> list = NonNullList.create();
		list.addAll(stacks.subList(from, to));
		return list;
	}

}
