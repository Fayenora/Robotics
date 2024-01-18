package com.ignis.igrobotics.core.util;

import com.google.gson.JsonElement;
import com.ignis.igrobotics.Robotics;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.function.Predicate;

public class ItemStackUtils {

	public static void dropItem(Entity entity, ItemStack stack) {
		dropItem(entity.level, entity.xOld, entity.yOld, entity.zOld, stack);
	}

	public static void dropItem(Level level, double x, double y, double z, ItemStack stack) {
		ItemEntity item = new ItemEntity(level, x, y, z, stack);
		item.setDefaultPickUpDelay();

		float f = Robotics.RANDOM.nextFloat() * 0.3F;
		float f1 = Robotics.RANDOM.nextFloat() * ((float)Math.PI * 2F);
		item.setDeltaMovement(-Math.sin(f1) * f, 0.25, Math.cos(f1) * f);

		level.addFreshEntity(item);
	}
	
	/**
	 * Try to insert the given itemstack in the inventory
	 * @param inventory the inventory to insert the stack into
	 * @param stack the itemstack
	 * @param simulate whether to actually perform the operation, or just calculate the return value
	 * @return the remainder. If this is empty, the entire stack fits in the inventory
	 */
	public static ItemStack insert(IItemHandler inventory, ItemStack stack, boolean simulate) {
		return insert(inventory, 0, stack, simulate);
	}
	
	/**
	 * Try to insert the given itemstack in the inventory
	 * @param inventory the inventory to insert the stack into
	 * @param startSlot only attempt to insert the stack from this slot onwards
	 * @param stack the itemstack
	 * @param simulate whether to actually perform the operation, or just calculate the return value
	 * @return the remainder. If this is empty, the entire stack fits in the inventory
	 */
	public static ItemStack insert(IItemHandler inventory, int startSlot, ItemStack stack, boolean simulate) {
		return insert(inventory, startSlot, inventory.getSlots(), stack, simulate);
	}
	
	/**
	 * Try to insert the given itemstack in the inventory
	 * @param inventory the inventory to insert the stack into
	 * @param startSlot only attempt to insert the stack from this slot onwards
	 * @param endSlot only attempt to insert the stack up to this slot
	 * @param stack the itemstack
	 * @param simulate whether to actually perform the operation, or just calculate the return value
	 * @return the remainder. If this is empty, the entire stack fits in the inventory
	 */
	public static ItemStack insert(IItemHandler inventory, int startSlot, int endSlot, ItemStack stack, boolean simulate) {
		ItemStack remainder = stack;
		for(int i = startSlot; i < endSlot; i++) {
    		if(!inventory.isItemValid(i, stack)) continue;
    		remainder = inventory.insertItem(i, remainder, simulate);
    	}
		return remainder;
	}
	
	public static boolean areEmpty(ItemStack[] stacks) {
		for(ItemStack stack : stacks) {
			if(!stack.isEmpty()) {
				return false;
			}
		}
		return true;
	}
	
	public static boolean areItemsEqual(ItemStack[] stacks, ItemStack[] otherStacks) {
		if(stacks.length != otherStacks.length) {
			return false;
		}
		for(int i = 0; i < stacks.length; i++) {
			if(!stacks[i].getItem().equals(otherStacks[i].getItem())) {
				return false;
			}
		}
		return true;
	}
	
	public static boolean areBeneathMaxStackSize(ItemStack[] stacks) {
		for(ItemStack stack : stacks) {
			if(stack.getCount() >= stack.getMaxStackSize()) {
				return false;
			}
		}
		return true;
	}

	public static void saveAllItems(CompoundTag tag, NonNullList<ItemStack> list, String key) {
		ListTag nbtList = new ListTag();

		for (int i = 0; i < list.size(); ++i) {
			ItemStack itemstack = list.get(i);

			if (!itemstack.isEmpty()) {
				CompoundTag compound = itemstack.serializeNBT();
				compound.putByte("Slot", (byte)i);
				nbtList.add(compound);
			}
		}
		tag.put(key, nbtList);
	}

	public static NonNullList<ItemStack> loadAllItems(CompoundTag tag, String key) {
		ListTag nbtList = tag.getList(key, Tag.TAG_COMPOUND);
		NonNullList<ItemStack> list = NonNullList.withSize(nbtList.size(), ItemStack.EMPTY);

		for (int i = 0; i < nbtList.size(); ++i) {
			CompoundTag compound = nbtList.getCompound(i);
			int j = compound.getByte("Slot") & 255;
			list.set(j, ItemStack.of(compound));
		}

		return list;
	}
	
	/**
	 * Search for the next feasible ItemStack in the players inventory. Held items and hotbar are prioritized
	 * @param player the player to search
	 * @param searchFor the item type to search for
	 * @param predicate Optional additional predicate the ItemStack must fulfill
	 * @return The found ItemStack. Null if the item is not present in the players inventory
	 */
	@Nullable
	public static ItemStack searchPlayerForItem(Player player, Item searchFor, Predicate<ItemStack> predicate) {
		ArrayList<ItemStack> stacksToSearch = new ArrayList<>();
		//Add held items
		stacksToSearch.add(player.getMainHandItem());
		stacksToSearch.add(player.getOffhandItem());
		//Add hotbar & inventory (hotbar should be prioritised by default)
		stacksToSearch.addAll(player.getInventory().items);
		
		for(ItemStack stack : stacksToSearch) {
			if(stack.getItem() == searchFor && (predicate == null || predicate.test(stack))) {
				return stack;
			}
		}
		
		return null;
	}
	
	/**
	 * fills an ItemStack array with the specified stack
	 * @param length the length of the array
	 * @param stack the stack to fill the array with
	 * @return an ItemStack array with the specified length and copies of the specified stack
	 */
	public static ItemStack[] full(int length, ItemStack stack) {
		ItemStack[] toReturn = new ItemStack[length];
		for(int i = 0; i < toReturn.length; i++) {
			toReturn[i] = stack.copy();
		}
		return toReturn;
	}
	
	public static int getCount(Ingredient ingredient) {
		return ingredient.getItems()[0].getCount();
	}
	
	public static Ingredient getIngredient(Item item, int amount) {
		return Ingredient.of(new ItemStack(item, amount));
	}

	public static Ingredient fromJson(JsonElement json) {
		Ingredient ingredient = Ingredient.fromJson(json);
		if(json.isJsonObject() && json.getAsJsonObject().has("amount")) {
			int amount = json.getAsJsonObject().get("amount").getAsInt();
			for(ItemStack stack : ingredient.getItems()) {
				stack.setCount(amount);
			}
		}
		return ingredient;
	}

}
