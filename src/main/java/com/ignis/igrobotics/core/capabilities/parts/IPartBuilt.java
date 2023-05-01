package com.ignis.igrobotics.core.capabilities.parts;

import com.ignis.igrobotics.core.*;
import com.ignis.igrobotics.core.robot.EnumRobotMaterial;
import com.ignis.igrobotics.core.robot.EnumRobotPart;
import com.ignis.igrobotics.core.robot.RobotPart;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.DyeColor;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;
import net.minecraftforge.common.util.INBTSerializable;

@AutoRegisterCapability
public interface IPartBuilt extends INBTSerializable<CompoundTag> {
	
	public RobotPart[] getBodyParts();
	
	public RobotPart getBodyPart(EnumRobotPart part);
	
	public void setBodyPart(RobotPart part);
	
	public void destroyBodyPart(EnumRobotPart part);
	
	public default void setBodyPart(EnumRobotPart part, EnumRobotMaterial material) {
		setBodyPart(RobotPart.get(part, material));
	}
	
	public default boolean hasBodyPart(EnumRobotPart part) {
		return getBodyPart(part).getMaterial() != EnumRobotMaterial.NONE;
	}
	
	public default boolean hasAnyBodyPart() {
		for(RobotPart part : getBodyParts()) {
			if(hasBodyPart(part.getPart())) return true;
		}
		return false;
	}
	
	public default void clear() {
		for(RobotPart part : getBodyParts()) {
			RobotPart newPart = RobotPart.get(part.getPart(), EnumRobotMaterial.NONE);
			setBodyPart(newPart);
		}
	}
	
	/**
	 * Changes both the temporary and permanent color
	 * @param color
	 */
	public void setColor(DyeColor color);
	
	public DyeColor getColor();
	
	public void setTemporaryColor(DyeColor color);
	
	public DyeColor getTemporaryColor();

}
