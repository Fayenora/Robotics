package com.io.norabotics.common.capabilities;

import com.io.norabotics.common.robot.*;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.HashMap;
import java.util.Map;

@AutoRegisterCapability
public interface IPartBuilt extends INBTSerializable<CompoundTag> {

	NonNullList<ItemStack> getBodyParts(EnumModuleSlot slotType);

	void setBodyParts(EnumModuleSlot slotType, NonNullList<ItemStack> components);

	EnumRobotMaterial materialForSlot(EnumModuleSlot slotType);

	void destroyBodyPart(EnumModuleSlot slotType);

	boolean hasRenderLayer(int id);

	int getMaxBodyParts(EnumModuleSlot slotType);

	void setMaxBodyParts(EnumModuleSlot slotType, int size);

	//////////////
	// Colors
	//////////////

	/**
	 * Changes both the temporary and permanent color
	 * @param color the new permanent color
	 */
	void setColor(DyeColor color);

	DyeColor getColor();

	void setTemporaryColor(DyeColor color);

	DyeColor getTemporaryColor();

	//////////////
	// Defaults
	//////////////

	default Map<EnumModuleSlot, Integer> getPartSlots() {
		Map<EnumModuleSlot, Integer> partSlots = new HashMap<>(EnumModuleSlot.values().length);
		for(EnumModuleSlot slotType : EnumModuleSlot.values()) {
			partSlots.put(slotType, getMaxBodyParts(slotType));
		}
		return partSlots;
	}
	
	default void setBodyPart(EnumRobotPart part, EnumRobotMaterial material) {
		setBodyParts(part.toModuleSlot(), NonNullList.of(ItemStack.EMPTY, RobotPart.get(part, material).getItemStack(1)));
	}

	default void setBodyPart(RobotPart part) {
		setBodyPart(part.getPart(), part.getMaterial());
	}
	
	default boolean hasBodyPart(EnumModuleSlot slotType) {
		return getBodyParts(slotType).stream().anyMatch(i -> !i.isEmpty());
	}

	default boolean isValid() {
		return hasBodyPart(EnumModuleSlot.BODY) && hasBodyPart(EnumModuleSlot.HEAD) &&
				(hasBodyPart(EnumModuleSlot.LEFT_LEG) || hasBodyPart(EnumModuleSlot.RIGHT_LEG));
	}

	default void onCreation() {
		//If the robot has no body parts, initialize with iron
		for(EnumModuleSlot slotType : EnumModuleSlot.values()) {
			if(hasBodyPart(slotType)) return;
		}
		for(EnumModuleSlot slotType : EnumModuleSlot.primaries()) {
			setBodyPart(EnumRobotPart.valueOf(slotType), EnumRobotMaterial.IRON);
		}
	}
	
	default void clear() {
		for(EnumModuleSlot slotType : EnumModuleSlot.values()) {
			setBodyParts(slotType, NonNullList.of(ItemStack.EMPTY));
		}
	}

}
