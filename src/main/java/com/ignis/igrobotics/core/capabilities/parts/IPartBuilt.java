package com.ignis.igrobotics.core.capabilities.parts;

import com.ignis.igrobotics.core.robot.EnumRobotMaterial;
import com.ignis.igrobotics.core.robot.EnumRobotPart;
import com.ignis.igrobotics.core.robot.RobotPart;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.DyeColor;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;
import net.minecraftforge.common.util.INBTSerializable;

@AutoRegisterCapability
public interface IPartBuilt extends INBTSerializable<CompoundTag> {
	
	RobotPart[] getBodyParts();
	
	RobotPart getBodyPart(EnumRobotPart part);
	
	void setBodyPart(RobotPart part);
	
	void destroyBodyPart(EnumRobotPart part);
	
	default void setBodyPart(EnumRobotPart part, EnumRobotMaterial material) {
		setBodyPart(RobotPart.get(part, material));
	}
	
	default boolean hasBodyPart(EnumRobotPart part) {
		return getBodyPart(part).getMaterial() != EnumRobotMaterial.NONE;
	}
	
	default boolean hasAnyBodyPart() {
		for(RobotPart part : getBodyParts()) {
			if(hasBodyPart(part.getPart())) return true;
		}
		return false;
	}
	
	default void clear() {
		for(RobotPart part : getBodyParts()) {
			RobotPart newPart = RobotPart.get(part.getPart(), EnumRobotMaterial.NONE);
			setBodyPart(newPart);
		}
	}
	
	/**
	 * Changes both the temporary and permanent color
	 * @param color the new permanent color
	 */
	void setColor(DyeColor color);
	
	DyeColor getColor();
	
	void setTemporaryColor(DyeColor color);
	
	DyeColor getTemporaryColor();

}
