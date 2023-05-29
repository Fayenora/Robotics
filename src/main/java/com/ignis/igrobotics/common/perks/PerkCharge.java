package com.ignis.igrobotics.common.perks;

import com.ignis.igrobotics.core.SimpleDataManager;
import com.ignis.igrobotics.core.capabilities.perks.Perk;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;

public class PerkCharge extends Perk {
	
	public static final String CHARGE = "perk_charge:charge";

	public PerkCharge(String name) {
		super(name, 3);
	}
	
	@Override
	public void onEntityUpdate(int level, Mob entity, SimpleDataManager values) {
		if(entity.moveDist > 0.1) {
			values.increment(CHARGE);
		}
		if(values.get(CHARGE) > 100) {
			entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 10, 0));
			entity.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 10, 1));
			values.set(CHARGE, 0);
			//TODO Play a sound here
		}
	}
	
	@Override
	public void onEntityJump(int level, Mob entity, SimpleDataManager values) {
		values.set(CHARGE, values.get(CHARGE) + 5);
	}
	
	@Override
	public float attackEntityAsMob(int level, Mob attacker, Entity toAttack, SimpleDataManager values) {
		values.set(CHARGE, values.get(CHARGE) + 10);
		return super.attackEntityAsMob(level, attacker, toAttack, values);
	}

}
