package com.ignis.igrobotics.common.perks;

import com.ignis.igrobotics.core.SimpleDataManager;
import com.ignis.igrobotics.core.capabilities.perks.Perk;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

public class PerkImpact extends Perk {

	public PerkImpact(String name) {
		super(name, 5);
	}
	
	@Override
	public float attackEntityAsMob(int level, Mob attacker, Entity toAttack, SimpleDataManager values) {
		if(!(toAttack instanceof LivingEntity living)) return 0;
		//Won't kill the target; other functions should handle this
		//Killing this way would result in onDeath not being called and other unexpected behavior
		living.setHealth(Math.max(living.getHealth() - level, 1)); 
		return 0;
	}

}
