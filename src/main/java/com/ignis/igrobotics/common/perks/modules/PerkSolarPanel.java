package com.ignis.igrobotics.common.perks.modules;

import com.ignis.igrobotics.core.SimpleDataManager;
import com.ignis.igrobotics.core.capabilities.perks.Perk;
import com.ignis.igrobotics.core.util.Lang;
import com.ignis.igrobotics.definitions.ModAttributes;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.DaylightDetectorBlock;
import net.minecraft.world.level.block.entity.DaylightDetectorBlockEntity;

import java.util.UUID;

public class PerkSolarPanel extends Perk {
	
	public static final UUID MODIFIER_UUID = UUID.fromString("4f0e6873-a24e-4881-aaa2-d9f5273e0d04");
	public static final String SOLAR_PANEL = "perk_solar_panel";

	public PerkSolarPanel(String name) {
		super(name, Integer.MAX_VALUE);
		setDisplayColor(TextColor.fromLegacyFormat(ChatFormatting.YELLOW));
	}
	
	@Override
	public void onEntityUpdate(int level, Entity entity, SimpleDataManager values) {
		if(!(entity instanceof Mob mob)) return;
		int skylight = entity.level.getBrightness(LightLayer.SKY, entity.blockPosition().above()) - entity.level.getSkyDarken();
		int energy_gain = skylight * 10 * level;
		AttributeInstance energy_attribute = mob.getAttributes().getInstance(ModAttributes.ENERGY_CONSUMPTION);
		//TODO: This rapid changing of attribute modifiers might cause efficiency or instability issues
		//Try to more intelligently only reapply when the values actually change
		energy_attribute.removeModifier(MODIFIER_UUID);
		energy_attribute.addTransientModifier(new AttributeModifier(MODIFIER_UUID, SOLAR_PANEL, energy_gain, AttributeModifier.Operation.ADDITION));
	}

	@Override
	public Component getDisplayText(int level) {
		return Lang.localise(getUnlocalizedName()).withStyle(Style.EMPTY.withColor(displayColor));
	}

}
