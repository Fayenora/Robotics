package com.ignis.igrobotics.core.capabilities.inventory;

import java.util.function.Supplier;
import java.util.stream.IntStream;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

public class BaseInventory extends ItemStackHandler {

	private Level world;
	private Supplier<BlockPos> pos;
	private int inventorySize;
	private int[] lockedSlots = new int[0];
	
	public BaseInventory(Supplier<BlockPos> pos, int size) {
		super(size);
		inventorySize = size;
		this.pos = pos;
	}

	public void setLevel(Level level) {
		this.world = level;
	}
	
	public void lockSlots(int... slots) {
		lockedSlots = slots;
	}

	@Override
	public int getSlots() {
		return inventorySize;
	}

	@Override
	public boolean isItemValid(int slot, @NotNull ItemStack stack) {
		return !IntStream.of(lockedSlots).anyMatch((x) -> x == slot);	//The very fancy way of "contains";
	}
	
	public int getMaxInventorySize() {
		return stacks.size();
	}

	@Override
	public void setSize(int newSize) {
		newSize = Math.min(newSize, getMaxInventorySize());
		if(getSlots() == newSize) return;
		
		//Drop items that don't fit anymore
		if(world != null && !world.isClientSide()) {
			if(newSize < getSlots()) {
				Containers.dropContents(world, pos.get(), subList(newSize, inventorySize));
				for(int i = newSize; i < inventorySize; i++) {
					stacks.set(i, ItemStack.EMPTY);
				}
			}
		}

		this.inventorySize = newSize;
	}

	@Override
	public CompoundTag serializeNBT() {
		CompoundTag nbt = super.serializeNBT();
		nbt.putInt("inventorySize", inventorySize);
		return super.serializeNBT();
	}

	@Override
	public void deserializeNBT(CompoundTag nbt) {
		super.deserializeNBT(nbt);
		this.inventorySize = nbt.getInt("inventorySize");
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
