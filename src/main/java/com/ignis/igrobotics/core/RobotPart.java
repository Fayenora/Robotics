package com.ignis.igrobotics.core;

import com.ignis.igrobotics.ModItems;
import com.ignis.igrobotics.Robotics;
import com.ignis.igrobotics.core.capabilities.perks.IPerkMap;
import com.ignis.igrobotics.core.capabilities.perks.PerkMap;
import com.ignis.igrobotics.integration.config.RoboticsConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;

/**
 * Logic behind a robot part (not the item) as the combination of a {@link EnumRobotPart specific part of the robot} and a {@link EnumRobotMaterial material}. <br>
 * Each part has associated perks loaded in with the config during pre-init. <br>
 * There should only exist a single instance for each unique combination, saved in the current {@link com.ignis.igrobotics.integration.config.PartConfig}.<br>
 * To retrieve an instance, use {@link #get(EnumRobotPart, EnumRobotMaterial)}
 * @author Ignis
 */
public class RobotPart {
	
	public static final String PATH = Robotics.MODID + ":textures/robot/";
	public static final String PARTS_PATH = PATH + "limbs/";
	public static final String COLORS_PATH = PATH + "color/";
	
	private EnumRobotPart part;
	private EnumRobotMaterial material;
	
	private IPerkMap perks = new PerkMap();
	
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
		RobotPart robot_part = new RobotPart(part, material);
		if(!RoboticsConfig.current().parts.PARTS.containsKey(part)) {
			HashMap<EnumRobotMaterial, RobotPart> material_map = new HashMap<EnumRobotMaterial, RobotPart>();
			RoboticsConfig.current().parts.PARTS.put(part, material_map);
		}
		if(!RoboticsConfig.current().parts.PARTS.get(part).containsKey(material)) {
			RoboticsConfig.current().parts.PARTS.get(part).put(material, robot_part);
			return robot_part;
		}
		return RoboticsConfig.current().parts.PARTS.get(part).get(material);
	}

	public static RobotPart getFromItem(Item item) {
		for(int i = 1; i < EnumRobotMaterial.values().length; i++) {
			for(int j = 0; j < EnumRobotPart.values().length; j++) {
				if(ModItems.MATERIALS[i][j].equals(item)) {
					return RobotPart.get(EnumRobotPart.getByID(j), EnumRobotMaterial.getByID(i));
				}
			}
		}
		return RobotPart.get(EnumRobotPart.BODY, EnumRobotMaterial.NONE);
	}

	public ItemStack getItemStack(int count) {
		return new ItemStack(ModItems.MATERIALS[material.getID() - 1][part.getID()].get(), count);
	}
	
	public static void registerPerks(EnumRobotPart part, EnumRobotMaterial material, IPerkMap perks) {
		get(part, material).getPerks().merge(perks);
	}
	
	public IPerkMap getPerks() {
		return perks;
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
}
