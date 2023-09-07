package com.ignis.igrobotics.common.perks;


import com.ignis.igrobotics.common.PerkBehavior;
import com.ignis.igrobotics.core.SimpleDataManager;
import com.ignis.igrobotics.core.capabilities.perks.Perk;
import com.ignis.igrobotics.core.util.Lang;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;

public class PerkLuminous extends Perk {

	public PerkLuminous(String name) {
		super(name, 1);
	}
	
	@Override
	public void onEntityUpdate(int level, Mob entity, SimpleDataManager values) {
		entity.addEffect(new MobEffectInstance(MobEffects.GLOWING, PerkBehavior.PERK_TICK_RATE * 2, 0, true, false));
	}

	@Override
	public Component getDescriptionText() {
		return Lang.localise("perk.glowing.desc");
	}

}
