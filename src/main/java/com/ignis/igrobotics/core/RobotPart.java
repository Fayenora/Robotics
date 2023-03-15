package com.ignis.igrobotics.core;

import com.ignis.igrobotics.ModItems;
import com.ignis.igrobotics.Robotics;
import com.ignis.igrobotics.core.capabilities.perks.IPerkMap;
import com.ignis.igrobotics.core.capabilities.perks.PerkMap;
import com.ignis.igrobotics.integration.config.RoboticsConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
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

	public ItemStack getItemStack(int count) {
		return new ItemStack(ModItems.MATERIALS[material.getID() - 1][part.getID()].get(), count);
	}

	/*
	public ItemStack getItemStack(int count) {
		return new ItemStack(ModItems.materials[getMaterial().getID()], count, getPart().getID());
	}
	 */
	
	public static void registerPerks(EnumRobotPart part, EnumRobotMaterial material, IPerkMap perks) {
		get(part, material).getPerks().merge(perks);
	}
	
	public IPerkMap getPerks() {
		return perks;
	}
	
	/*
	 * Resource Locations
	 */
	
	/**
	 * Get the resource location for the entity part
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

	/*
	 * Possible Values
	 */
	
	public enum EnumRobotPart {
		HEAD(0, "head"),
		BODY(1, "body"),
		LEFT_ARM(2, "left_arm"),
		RIGHT_ARM(3, "right_arm"),
		LEFT_LEG(4, "left_leg"),
		RIGHT_LEG(5, "right_leg");
		
		private String name;
		private int id;
		
		EnumRobotPart(int id, String name) {
			this.id = id;
			this.name = name;
		}
		
		public int getID() { return id; }
		
		public static EnumRobotPart getByID(int id) {
			return values()[id];
		}
		
		public String getName() { return name; }
		
		@Override
		public String toString() {
			return name;
		}
	}
	
	
	
	public enum EnumRobotMaterial {
		NONE(0, "none", 0),
		IRON(1, "iron", 1),
		GOLD(2, "gold", 1),
		COPPER(3, "copper", 1),
		TIN(4, "tin", 1),
		ALUMINIUM(5, "aluminium", 2),
		NICKEL(6, "nickel", 2),
		SILVER(7, "silver", 2),
		LEAD(8, "lead", 5),
		BRONZE(9, "bronze", 2),
		CONSTANTAN(10, "constantan", 2),
		STEEL(11, "steel", 3),
		ELECTRUM(12, "electrum", 2),
		//INVAR?
		
		//Thermal Series
		PLATINUM(13, "platinum", 4),
		IRIDIUM(14, "iridium", 5),
		SIGNALUM(15, "signalum", 3),
		LUMIUM(16, "lumium", 1),
		
		//Ender IO
		DARK_STEEL(17, "dark_steel", 4),
		END_STEEL(18, "end_steel", 5),
		
		//Nuclear Craft
		TOUGH_ALLOY(19, "tough_alloy", 4),
		
		//Tinkers Construct
		COBALT(20, "cobalt", 3),
		ARDITE(21, "ardite", 3),
		MANYULLIN(22, "manyullin", 4),
		
		//Mekanism
		OSMIUM(23, "osmium", 3),
		
		//Psi
		PSIMETAL(24, "psimetal", 3);
		
		private String name;
		private int id, stiffness;
		
		/**
		 * @param id
		 * @param name
		 * @param stiffness used for energy processing costs in processing; range [0, 5]
		 */
		EnumRobotMaterial(int id, String name, int stiffness) {
			this.id = id;
			this.name = name;
			this.stiffness = stiffness;
		}
		
		public String getName() { return this.name; }
		
		public int getID() { return this.id; }
		
		public int getStiffness() { return this.stiffness; }
		
		public String getMaterialOreName() {
			return "ingot" + name.substring(0, 1).toUpperCase() + name.substring(1);
		}
		
		public static EnumRobotMaterial getByID(int id) { return EnumRobotMaterial.values()[id]; }
		
		public static EnumRobotMaterial[] valuesWithoutEmpty() {
			EnumRobotMaterial[] values = new EnumRobotMaterial[values().length - 1];
			int i = 0;
			for(EnumRobotMaterial mat : values()) {
				if(mat.equals(NONE)) continue;
				values[i++] = mat;
			}
			return values;
		}
		
		@Override
		public String toString() {
			return name;
		}
	}

}
