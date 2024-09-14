package com.io.norabotics.common.content.perks.modules;

import com.io.norabotics.common.capabilities.impl.perk.Perk;
import com.io.norabotics.common.helpers.types.SimpleDataManager;
import com.io.norabotics.common.helpers.util.Lang;
import com.io.norabotics.integration.config.RoboticsConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.LightLayer;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

public class PerkSolarPanel extends Perk {

	public PerkSolarPanel(String name) {
		super(name, Integer.MAX_VALUE);
	}
	
	@Override
	public void onEntityUpdate(int level, Mob entity, SimpleDataManager values) {
		float f = entity.level().getSunAngle(1.0F);
		float f1 = f < (float)Math.PI ? 0.0F : ((float)Math.PI * 2F);
		f += (f1 - f) * 0.2F;
		int skylight = entity.level().getBrightness(LightLayer.SKY, entity.blockPosition().above()) - entity.level().getSkyDarken();
		skylight = Mth.clamp(Math.round((float)skylight * Mth.cos(f)), 0, 15);
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
