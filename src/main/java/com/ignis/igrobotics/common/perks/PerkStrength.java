package com.ignis.igrobotics.common.perks;


import com.ignis.igrobotics.core.SimpleDataManager;
import com.ignis.igrobotics.core.capabilities.perks.Perk;
import net.minecraft.world.entity.Entity;

public class PerkStrength extends Perk {

	public PerkStrength(String name) {
		super(name, 3);
	}
	
	@Override
	public float attackEntityAsMob(int level, Entity attacker, Entity toAttack, SimpleDataManager values) {
		return level;
	}

}
