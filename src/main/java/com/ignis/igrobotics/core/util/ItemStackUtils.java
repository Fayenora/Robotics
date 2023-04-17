package com.ignis.igrobotics.core.util;

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
	 * @param inventory
	 * @param stack
	 * @param simulate
	 * @return the remainder. If this is empty, the entire stack fits in the inventory
	 */
	public static ItemStack insert(IItemHandler inventory, ItemStack stack, boolean simulate) {
		return insert(inventory, 0, stack, simulate);
	}
	
	/**
	 * Try to insert the given itemstack in the inventory
	 * @param inventory
	 * @param startSlot
	 * @param stack
	 * @param simulate
	 * @return the remainder. If this is empty, the entire stack fits in the inventory
	 */
	public static ItemStack insert(IItemHandler inventory, int startSlot, ItemStack stack, boolean simulate) {
		return insert(inventory, startSlot, inventory.getSlots(), stack, simulate);
	}
	
	/**
	 * Try to insert the given itemstack in the inventory
	 * @param inventory
	 * @param startSlot
	 * @param endSlot
	 * @param stack
	 * @param simulate
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
	
	public static boolean areItemsEqual(ItemStack[] stacksa, ItemStack[] stacksb) {
		if(stacksa.length != stacksb.length) {
			return false;
		}
		for(int i = 0; i < stacksa.length; i++) {
			if(!stacksa[i].getItem().equals(stacksb[i].getItem())) {
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

	public static CompoundTag saveAllItems(CompoundTag tag, NonNullList<ItemStack> list, String key) {
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
		return tag;
	}

	public static void loadAllItems(CompoundTag tag, NonNullList<ItemStack> list, String key) {
		ListTag nbtList = tag.getList(key, Tag.TAG_COMPOUND);

		for (int i = 0; i < nbtList.size(); ++i) {
			CompoundTag compound = nbtList.getCompound(i);
			int j = compound.getByte("Slot") & 255;

			if (j >= 0 && j < nbtList.size()) {
				list.set(j, ItemStack.of(compound));
			}
		}
	}
	
	/**
	 * Search for the next feasible ItemStack in the players inventory. Held items and hotbar are prioritized
	 * @param player
	 * @param search_for
	 * @param pred Optional predicate the ItemStack must fulfill
	 * @return The found ItemStack. Null if the item is not present in the players inventory
	 */
	@Nullable
	public static ItemStack searchPlayerForItem(Player player, Item search_for, Predicate<ItemStack> pred) {
		ArrayList<ItemStack> stacksToSearch = new ArrayList<ItemStack>();
		//Add held items
		stacksToSearch.add(player.getMainHandItem());
		stacksToSearch.add(player.getOffhandItem());
		//Add hotbar & inventory (hotbar should be prioritised by default)
		stacksToSearch.addAll(player.getInventory().items);
		
		for(ItemStack stack : stacksToSearch) {
			if(stack.getItem() == search_for && (pred == null || pred.test(stack))) {
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
	
	public static int getCount(Ingredient ingr) {
		return ingr.getItems()[0].getCount();
	}
	
	public static Ingredient getIngredient(Item item, int amount) {
		return Ingredient.of(new ItemStack(item, amount));
	}

}
