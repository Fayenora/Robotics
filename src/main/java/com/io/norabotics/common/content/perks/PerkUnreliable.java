package com.io.norabotics.common.content.perks;


import com.io.norabotics.Robotics;
import com.io.norabotics.common.capabilities.IPartBuilt;
import com.io.norabotics.common.capabilities.ModCapabilities;
import com.io.norabotics.common.capabilities.impl.perk.Perk;
import com.io.norabotics.common.helpers.types.SimpleDataManager;
import com.io.norabotics.common.helpers.util.Lang;
import com.io.norabotics.common.robot.EnumModuleSlot;
import com.io.norabotics.common.robot.EnumRobotPart;
import com.io.norabotics.common.robot.RobotModule;
import com.io.norabotics.common.robot.RobotPart;
import com.io.norabotics.definitions.robotics.ModModules;
import com.io.norabotics.definitions.robotics.ModPerks;
import com.io.norabotics.integration.config.RoboticsConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;

public class PerkUnreliable extends Perk {

	public PerkUnreliable(String name) {
		super(name, 1);
	}
	
	@Override
	public float onDamage(int level, Mob robot, DamageSource dmgSource, float damage, SimpleDataManager values) {
		IPartBuilt parts = robot.getCapability(ModCapabilities.PARTS).orElse(ModCapabilities.NO_PARTS);
		
		//FIXME: Works, but creates a concurrent modification exception when the last part of any perk is destroyed, as the map is currently iterating over the perks while calling this very function
		EnumModuleSlot toDestroy = null;
        for(EnumModuleSlot slotType : EnumModuleSlot.values()) {
            for(ItemStack stack : parts.getBodyParts(slotType)) {
                RobotModule module = ModModules.get(stack);
                if(module.getPerks().contains(ModPerks.PERK_UNRELIABLE.get()) && Robotics.RANDOM.nextDouble() < RoboticsConfig.general.unreliableChance.get().floatValue()) {
                    toDestroy = slotType;
                }
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
