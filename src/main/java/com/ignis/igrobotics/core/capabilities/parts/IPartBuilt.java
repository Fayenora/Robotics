package com.ignis.igrobotics.core.capabilities.parts;

import com.ignis.igrobotics.core.IEntityHook;
import com.ignis.igrobotics.core.INBTSerializer;
import com.ignis.igrobotics.core.RobotPart;
import net.minecraft.world.item.DyeColor;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;

@AutoRegisterCapability
public interface IPartBuilt extends INBTSerializer, IEntityHook {
	
	public RobotPart[] getBodyParts();
	
	public RobotPart getBodyPart(RobotPart.EnumRobotPart part);
	
	public void setBodyPart(RobotPart part);
	
	public void destroyBodyPart(RobotPart.EnumRobotPart part);
	
	public default void setBodyPart(RobotPart.EnumRobotPart part, RobotPart.EnumRobotMaterial material) {
		setBodyPart(RobotPart.get(part, material));
	}
	
	public default boolean hasBodyPart(RobotPart.EnumRobotPart part) {
		return getBodyPart(part).getMaterial() != RobotPart.EnumRobotMaterial.NONE;
	}
	
	public default boolean hasAnyBodyPart() {
		for(RobotPart part : getBodyParts()) {
			if(hasBodyPart(part.getPart())) return true;
		}
		return false;
	}
	
	public default void clear() {
		for(RobotPart part : getBodyParts()) {
			RobotPart newPart = RobotPart.get(part.getPart(), RobotPart.EnumRobotMaterial.NONE);
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
