package com.ignis.igrobotics.core.robot;

import com.ignis.igrobotics.Robotics;
import com.ignis.igrobotics.core.capabilities.ModCapabilities;
import com.ignis.igrobotics.core.capabilities.perks.IPerkMap;
import com.ignis.igrobotics.definitions.ModItems;
import com.ignis.igrobotics.definitions.ModModules;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

/**
 * Logic behind a robot part (not the item) as the combination of a {@link EnumRobotPart specific part of the robot} and a {@link EnumRobotMaterial material}. <br>
 * To retrieve an instance, use {@link #get(EnumRobotPart, EnumRobotMaterial)}
 * @author Roe
 */
public class RobotPart {
	
	public static final String PATH = Robotics.MODID + ":textures/robot/";
	public static final String PARTS_PATH = PATH + "limbs/";
	public static final String COLORS_PATH = PATH + "color/";
	
	private final EnumRobotPart part;
	private final EnumRobotMaterial material;
	
	private RobotPart(EnumRobotPart part, EnumRobotMaterial material) {
		this.part = part;
		this.material = material;
	}
	
	public EnumRobotMaterial getMaterial() {
		return material;
	}
	
	public EnumRobotPart getPart() {
		return part;
	}
	
	public static RobotPart get(EnumRobotPart part, EnumRobotMaterial material) {
		return new RobotPart(part, material);
	}

	@Nullable
	public static RobotPart getFromItem(Item item) {
		for(int i = 0; i < EnumRobotMaterial.valuesWithoutEmpty().length; i++) {
			for(int j = 0; j < EnumRobotPart.values().length; j++) {
				if(ModItems.MATERIALS[i][j].get().equals(item)) {
					return RobotPart.get(EnumRobotPart.byId(j), EnumRobotMaterial.byId(i + 1));
				}
			}
		}
		return null;
	}

	public Item getItem() {
		if(material == EnumRobotMaterial.NONE) return Items.AIR;
		return ModItems.MATERIALS[material.getID() - 1][part.getID()].get();
	}

	public ItemStack getItemStack(int count) {
		if(material == EnumRobotMaterial.NONE) return ItemStack.EMPTY;
		return new ItemStack(getItem(), count);
	}
	
	/*
	 * Resource Locations
	 */

	public ResourceLocation getLimbResourceLocation() {
		return new ResourceLocation(PARTS_PATH + material.getName() + "/" + part.getName() + ".png");
	}
	
	public ResourceLocation getColorResourceLocation(DyeColor color) {
		return new ResourceLocation(COLORS_PATH + color.getName() + "/" + part.getName() + ".png");
	}

	@Override
	public String toString() {
		return material + " " + part;
	}

	public IPerkMap getPerks() {
		RobotModule perkModule = ModModules.get(getItem());
		return perkModule == null ? ModCapabilities.NO_PERKS : perkModule.getPerks();
	}
}
