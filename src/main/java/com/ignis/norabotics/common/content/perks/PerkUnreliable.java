package com.ignis.norabotics.common.content.perks;


import com.ignis.norabotics.Robotics;
import com.ignis.norabotics.common.capabilities.IPartBuilt;
import com.ignis.norabotics.common.capabilities.ModCapabilities;
import com.ignis.norabotics.common.capabilities.impl.perk.Perk;
import com.ignis.norabotics.common.helpers.types.SimpleDataManager;
import com.ignis.norabotics.common.helpers.util.Lang;
import com.ignis.norabotics.common.robot.EnumRobotPart;
import com.ignis.norabotics.common.robot.RobotPart;
import com.ignis.norabotics.definitions.ModPerks;
import com.ignis.norabotics.integration.config.RoboticsConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Mob;

import java.util.Random;

public class PerkUnreliable extends Perk {

	public PerkUnreliable(String name) {
		super(name, 1);
	}
	
	@Override
	public float onDamage(int level, Mob robot, DamageSource dmgSource, float damage, SimpleDataManager values) {
		IPartBuilt parts = robot.getCapability(ModCapabilities.PARTS).orElse(ModCapabilities.NO_PARTS);
		
		//FIXME: Works, but creates a concurrent modification exception when the last part of any perk is destroyed, as the map is currently iterating over the perks
		EnumRobotPart toDestroy = null;
		for(RobotPart part : parts.getBodyParts()) {
			if(part.getPerks().contains(ModPerks.PERK_UNRELIABLE.get()) && Robotics.RANDOM.nextDouble() < RoboticsConfig.general.unreliableChance.get().floatValue()) {
				toDestroy = part.getPart();
			}
		}
		if(toDestroy != null) {
			parts.destroyBodyPart(toDestroy);
		}
		
		return damage;
	}

	@Override
	public Component getDescriptionText() {
		return Lang.localise("perk.unreliable.desc", String.format("%.3f%%", RoboticsConfig.general.unreliableChance.get().floatValue() * 100));
	}
}
