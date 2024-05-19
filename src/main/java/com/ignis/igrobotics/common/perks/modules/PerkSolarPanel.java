package com.ignis.igrobotics.common.perks.modules;

import com.ignis.igrobotics.core.SimpleDataManager;
import com.ignis.igrobotics.core.capabilities.perks.Perk;
import com.ignis.igrobotics.core.util.Lang;
import com.ignis.igrobotics.integration.config.RoboticsConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.LightLayer;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

public class PerkSolarPanel extends Perk {

	public PerkSolarPanel(String name) {
		super(name, Integer.MAX_VALUE);
	}
	
	@Override
	public void onEntityUpdate(int level, Mob entity, SimpleDataManager values) {
		int skylight = entity.level().getBrightness(LightLayer.SKY, entity.blockPosition().above()) - entity.level().getSkyDarken();
		int energy_gain = skylight * RoboticsConfig.general.solarGeneratorMult.get() * level;
		entity.getCapability(ForgeCapabilities.ENERGY).ifPresent(energy -> {
			energy.receiveEnergy(energy_gain, false);
		});
	}

	@Override
	public Component getDisplayText(int level) {
		return localized().withStyle(Style.EMPTY.withColor(displayColor));
	}

	@Override
	public Component getDescriptionText() {
		return Lang.localise("perk.solar_panel.desc", RoboticsConfig.general.solarGeneratorMult.get() * 15);
	}
}
