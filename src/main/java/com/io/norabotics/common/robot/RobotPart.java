package com.io.norabotics.common.robot;

import com.io.norabotics.Robotics;
import com.io.norabotics.common.capabilities.IPerkMap;
import com.io.norabotics.common.capabilities.ModCapabilities;
import com.io.norabotics.definitions.ModItems;
import com.io.norabotics.definitions.robotics.ModModules;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

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
	private final ResourceLocation limbResourceLocation;
	private final Map<DyeColor, ResourceLocation> colorResourceLocations;
	
	private RobotPart(EnumRobotPart part, EnumRobotMaterial material) {
		this.part = part;
		this.material = material;
		this.limbResourceLocation = new ResourceLocation(PARTS_PATH + material.getName() + "/" + part.getName() + ".png");
		this.colorResourceLocations = new HashMap<>();
		for(DyeColor color : DyeColor.values()) {
			colorResourceLocations.put(color, new ResourceLocation(COLORS_PATH + color.getName() + "/" + part.getName() + ".png"));
		}
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
		for(EnumRobotMaterial material : ModItems.MATERIALS.keySet()) {
			for(EnumRobotPart part : ModItems.MATERIALS.get(material).keySet()) {
				if(ModItems.MATERIALS.get(material).get(part).get().equals(item)) {
					return RobotPart.get(part, material);
				}
			}
		}
		return null;
	}

	public Item getItem() {
		if(material == EnumRobotMaterial.NONE) return Items.AIR;
		return ModItems.MATERIALS.get(material).get(part).get();
	}

	public ItemStack getItemStack(int count) {
		if(material == EnumRobotMaterial.NONE) return ItemStack.EMPTY;
		return new ItemStack(getItem(), count);
	}
	
	/*
	 * Resource Locations
	 */

	public ResourceLocation getLimbResourceLocation() {
		return limbResourceLocation;
	}
	
	public ResourceLocation getColorResourceLocation(DyeColor color) {
		return colorResourceLocations.get(color);
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
