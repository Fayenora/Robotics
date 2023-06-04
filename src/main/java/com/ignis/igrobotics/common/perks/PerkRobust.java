package com.ignis.igrobotics.common.perks;


import com.ignis.igrobotics.core.SimpleDataManager;
import com.ignis.igrobotics.core.capabilities.perks.Perk;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Mob;

public class PerkRobust extends Perk {

	public PerkRobust(String name) {
		super(name, 3);
	}
	
	@Override
	public float damageEntity(int level, Mob robot, DamageSource dmgSource, float damage, SimpleDataManager values) {
		if(dmgSource.is(DamageTypeTags.BYPASSES_EFFECTS)) return damage;
		return damage - level;
	}

}
