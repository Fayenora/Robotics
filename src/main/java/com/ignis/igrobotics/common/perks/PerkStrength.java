package com.ignis.igrobotics.common.perks;


import com.ignis.igrobotics.core.SimpleDataManager;
import com.ignis.igrobotics.core.capabilities.perks.Perk;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;

public class PerkStrength extends Perk {

	public PerkStrength(String name) {
		super(name, 3);
	}
	
	@Override
	public float attackEntityAsMob(int level, Mob attacker, Entity toAttack, SimpleDataManager values) {
		return level;
	}

}
