package com.ignis.igrobotics.core.robot;

import com.ignis.igrobotics.Robotics;
import com.ignis.igrobotics.core.capabilities.perks.IPerkMap;
import com.ignis.igrobotics.core.capabilities.perks.PerkMap;
import com.ignis.igrobotics.core.util.Tuple;
import com.ignis.igrobotics.definitions.ModItems;
import com.ignis.igrobotics.integration.config.PartConfig;
import com.ignis.igrobotics.integration.config.RoboticsConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Logic behind a robot part (not the item) as the combination of a {@link EnumRobotPart specific part of the robot} and a {@link EnumRobotMaterial material}. <br>
 * Each part has associated perks loaded in with the config during pre-server startup. <br>
 * There should only exist a single instance for each unique combination, saved in the current {@link com.ignis.igrobotics.integration.config.PartConfig}.<br>
 * To retrieve an instance, use {@link #get(EnumRobotPart, EnumRobotMaterial)}
 * @author Ignis
 */
public class RobotPart {
	
	public static final String PATH = Robotics.MODID + ":textures/robot/";
	public static final String PARTS_PATH = PATH + "limbs/";
	public static final String COLORS_PATH = PATH + "color/";
	
	private final EnumRobotPart part;
	private final EnumRobotMaterial material;
	
	private final IPerkMap perks = new PerkMap();
	
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
		return get(RoboticsConfig.current().parts, part, material);
	}

	public static RobotPart get(PartConfig config, EnumRobotPart part, EnumRobotMaterial material) {
		Tuple<EnumRobotPart, EnumRobotMaterial> key = new Tuple<>(part, material);
		if(!config.PARTS.containsKey(key)) {
			config.PARTS.put(key, new RobotPart(part, material));
		}
		return config.PARTS.get(key);
	}

	public static RobotPart getFromItem(Item item) {
		for(int i = 0; i < EnumRobotMaterial.valuesWithoutEmpty().length; i++) {
			for(int j = 0; j < EnumRobotPart.values().length; j++) {
				if(ModItems.MATERIALS[i][j].get().equals(item)) {
					return RobotPart.get(EnumRobotPart.byId(j), EnumRobotMaterial.byId(i + 1));
				}
			}
		}
		return RobotPart.get(EnumRobotPart.BODY, EnumRobotMaterial.NONE);
	}

	public ItemStack getItemStack(int count) {
		if(material == EnumRobotMaterial.NONE) return ItemStack.EMPTY;
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
