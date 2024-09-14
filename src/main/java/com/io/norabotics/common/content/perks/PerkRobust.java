package com.io.norabotics.common.content.perks;


import com.io.norabotics.common.capabilities.impl.perk.Perk;
import com.io.norabotics.common.helpers.types.SimpleDataManager;
import com.io.norabotics.common.helpers.util.Lang;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Mob;

public class PerkRobust extends Perk {

	public static final float PROTECTION_MULTIPLIER = 1;

	public PerkRobust(String name) {
		super(name, 3);
	}
	
	@Override
	public float onDamage(int level, Mob robot, DamageSource dmgSource, float damage, SimpleDataManager values) {
		if(dmgSource.is(DamageTypeTags.BYPASSES_EFFECTS)) return damage;
		return damage - level * PROTECTION_MULTIPLIER;
	}

	@Override
	public Component getDescriptionText() {
		return Lang.localise("perk.robust.desc", String.format("%.2f", PROTECTION_MULTIPLIER));
	}
}
