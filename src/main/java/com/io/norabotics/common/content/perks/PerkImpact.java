package com.io.norabotics.common.content.perks;

import com.io.norabotics.common.capabilities.impl.perk.Perk;
import com.io.norabotics.common.helpers.types.SimpleDataManager;
import com.io.norabotics.common.helpers.util.Lang;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

public class PerkImpact extends Perk {

	public static final float DAMAGE_MULTIPLIER = 1;

	public PerkImpact(String name) {
		super(name, 5);
	}

	@Override
	public float onAttack(int level, Mob attacker, Entity toAttack, SimpleDataManager values) {
		if(!(toAttack instanceof LivingEntity living)) return 0;
		//Won't kill the target; other functions should handle this
		//Killing this way would result in onDeath not being called and other unexpected behavior
		living.setHealth(Math.max(living.getHealth() - level * DAMAGE_MULTIPLIER, 1));
		return 0;
	}

	@Override
	public Component getDescriptionText() {
		return Lang.localise("perk.impact.desc", String.format("%.2f", DAMAGE_MULTIPLIER));
	}
}
