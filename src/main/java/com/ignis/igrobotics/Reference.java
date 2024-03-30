package com.ignis.igrobotics;

import com.ignis.igrobotics.core.robot.EnumRobotMaterial;
import com.ignis.igrobotics.definitions.ModAttributes;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.UUID;

import static com.ignis.igrobotics.core.robot.EnumRobotMaterial.*;

public class Reference {

	public static final String CC_MOD_ID = "computercraft";
	
	public static final ResourceLocation MISC = new ResourceLocation(Robotics.MODID + ":textures/gui/buttons.png");
	public static final ResourceLocation ICONS = new ResourceLocation("minecraft", "textures/gui/icons.png");
	public static final ResourceLocation ENERGY_BAR = new ResourceLocation(Robotics.MODID, "textures/gui/energy_bar.png");
	
	public static final int MAX_ROBOT_NAME_LENGTH = 35;
	public static final int MAX_RENDER_LAYERS = Integer.SIZE;
	
	/** Absolute maximum for module count per slot. Needs to be present at compile time */
	public static final int MAX_MODULES = 8;
	public static final int MAX_INVENTORY_SIZE = 36;

	public static final EnumRobotMaterial[] WIRE_METALS = new EnumRobotMaterial[] {
			IRON, GOLD, COPPER, TIN, ALUMINIUM, SILVER, LEAD, BRONZE, CONSTANTAN, ELECTRUM, PLATINUM
	};
	
	public static final HashMap<Attribute, TextColor> ATTRIBUTE_COLORS = new HashMap<>();
	
	static {
		ATTRIBUTE_COLORS.put(Attributes.MAX_HEALTH, TextColor.fromLegacyFormat(ChatFormatting.RED));
		ATTRIBUTE_COLORS.put(Attributes.ATTACK_DAMAGE, TextColor.fromLegacyFormat(ChatFormatting.RED));
		ATTRIBUTE_COLORS.put(Attributes.ARMOR, TextColor.fromLegacyFormat(ChatFormatting.GRAY));
		ATTRIBUTE_COLORS.put(Attributes.ARMOR_TOUGHNESS, TextColor.fromLegacyFormat(ChatFormatting.DARK_AQUA));
		ATTRIBUTE_COLORS.put(Attributes.MOVEMENT_SPEED, TextColor.fromLegacyFormat(ChatFormatting.WHITE));
		ATTRIBUTE_COLORS.put(Attributes.KNOCKBACK_RESISTANCE, TextColor.fromLegacyFormat(ChatFormatting.DARK_AQUA));
		ATTRIBUTE_COLORS.put(ModAttributes.ENERGY_CAPACITY, TextColor.fromLegacyFormat(ChatFormatting.GOLD));
		ATTRIBUTE_COLORS.put(ModAttributes.ENERGY_CONSUMPTION, TextColor.fromLegacyFormat(ChatFormatting.YELLOW));
		for(Attribute attribute : ModAttributes.MODIFIER_SLOTS) {
			ATTRIBUTE_COLORS.put(attribute, TextColor.fromLegacyFormat(ChatFormatting.WHITE));
		}
		ATTRIBUTE_COLORS.put(ModAttributes.INVENTORY_SLOTS, TextColor.fromLegacyFormat(ChatFormatting.RED));
	}
	
	public static final Dimension GUI_DEFAULT_DIMENSIONS = new Dimension(176, 166);
	public static final Dimension GUI_COMMANDER_DIMENSIONS = new Dimension(176, 182);
	public static final Dimension GUI_ASSEMBLER_DIMENSIONS = new Dimension(GUI_DEFAULT_DIMENSIONS.width, 216);
	public static final Dimension GUI_ROBOT_FACTORY_DIMENSIONS = new Dimension(229, 219);
	public static final Dimension GUI_ROBOT_DIMENSIONS = new Dimension(GUI_DEFAULT_DIMENSIONS.width, 175);
	public static final Dimension GUI_ROBOT_COMMANDS_DIMENSION = new Dimension(256, 182);
	
	public static final UUID DEFAULT_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");
	public static final GameProfile PROFILE = new GameProfile(DEFAULT_UUID, "[" + Robotics.MODID + "]");
	
	public static final int FONT_COLOR = 0x8b8b8b;
	public static final DecimalFormat FORMAT = new DecimalFormat("############.############");
	
	static {
		FORMAT.setPositivePrefix("+");
		FORMAT.setNegativePrefix("-");
	}
	
	public static boolean isSpecialKey(int key) {
		return key == InputConstants.KEY_BACKSPACE || (key >= InputConstants.KEY_RIGHT && key <= InputConstants.KEY_UP);
	}
}
