package com.ignis.igrobotics.common.perks;


import com.ignis.igrobotics.common.RobotBehavior;
import com.ignis.igrobotics.core.SimpleDataManager;
import com.ignis.igrobotics.core.capabilities.ModCapabilities;
import com.ignis.igrobotics.core.capabilities.parts.IPartBuilt;
import com.ignis.igrobotics.core.capabilities.perks.Perk;
import com.ignis.igrobotics.core.robot.EnumRobotPart;
import com.ignis.igrobotics.core.robot.RobotPart;
import com.ignis.igrobotics.integration.config.RoboticsConfig;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;

import java.util.Random;

public class PerkUnreliable extends Perk {
	
	private Random r = new Random();

	public PerkUnreliable(String name) {
		super(name, 1);
	}
	
	@Override
	public float damageEntity(int level, Entity robot, DamageSource dmgSource, float damage, SimpleDataManager values) {
		IPartBuilt parts = robot.getCapability(ModCapabilities.PARTS).orElse(ModCapabilities.NO_PARTS);
		
		//FIXME: Works, but creates a concurrent modification exception when the last part of any perk is destroyed, as the map is currently iterating over the perks
		EnumRobotPart toDestroy = null;
		for(RobotPart part : parts.getBodyParts()) {
			if(part.getPerks().contains(RoboticsConfig.current().perks.PERK_UNRELIABLE) && r.nextDouble() < 0.5) {
				toDestroy = part.getPart();
			}
		}
		if(toDestroy != null) {
			parts.destroyBodyPart(toDestroy);
		}
		
		return damage;
	}

}
