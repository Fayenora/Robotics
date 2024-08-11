package com.ignis.igrobotics.common.content.perks;


import com.ignis.igrobotics.common.capabilities.impl.perk.Perk;
import com.ignis.igrobotics.common.helpers.types.SimpleDataManager;
import com.ignis.igrobotics.common.helpers.util.Lang;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;

public class PerkStrength extends Perk {

	public static final float DAMAGE_MULTIPLIER = 1;

	public PerkStrength(String name) {
		super(name, 3);
	}
	
	@Override
	public float onAttack(int level, Mob attacker, Entity toAttack, SimpleDataManager values) {
		return DAMAGE_MULTIPLIER * level;
	}

	@Override
	public Component getDescriptionText() {
		return Lang.localise("perk.strength.desc", String.format("%.2f", DAMAGE_MULTIPLIER));
	}

}
