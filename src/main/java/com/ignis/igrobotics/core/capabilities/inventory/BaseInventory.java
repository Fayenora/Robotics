package com.ignis.igrobotics.core.capabilities.inventory;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;
import java.util.stream.IntStream;

public class BaseInventory extends ItemStackHandler implements ModifiableInventory {

	private Level world;
	private Supplier<BlockPos> pos;
	private int[] lockedSlots = new int[0];
	
	public BaseInventory(Supplier<BlockPos> pos, int size) {
		super(size);
		this.pos = pos;
	}

	public void setLevel(Level level) {
		this.world = level;
	}
	
	public void lockSlots(int... slots) {
		lockedSlots = slots;
	}

	@Override
	public boolean isItemValid(int slot, @NotNull ItemStack stack) {
		return !IntStream.of(lockedSlots).anyMatch((x) -> x == slot);	//The very fancy way of "contains";
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

	public boolean isEmpty() {
		for(ItemStack stack : stacks) {
			if(!stack.isEmpty()) return false;
		}
		return true;
	}

	public void clear() {
		for(int i = 0; i < stacks.size(); i++) {
			stacks.set(i, ItemStack.EMPTY);
		}
	}

	public NonNullList<ItemStack> subList(int from, int to) {
		NonNullList<ItemStack> list = NonNullList.create();
		for(ItemStack stack : stacks.subList(from, to)) {
			list.add(stack);
		}
		return list;
	}

}
