package com.ignis.igrobotics.common.content.perks;

import com.ignis.igrobotics.common.capabilities.IPerkMap;
import com.ignis.igrobotics.common.capabilities.IRobot;
import com.ignis.igrobotics.common.capabilities.ModCapabilities;
import com.ignis.igrobotics.common.capabilities.impl.perk.Perk;
import com.ignis.igrobotics.common.handlers.PerkBehavior;
import com.ignis.igrobotics.common.helpers.types.SimpleDataManager;
import com.ignis.igrobotics.common.helpers.util.Lang;
import com.ignis.igrobotics.definitions.ModPerks;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class PerkMassProduced extends Perk {

	public static final int UPDATE_RATE = 9;
	public static final int MAX_ALLIES = 6;
	public static final int AREA_SIZE = 8;
	public static final String TICK_COUNTER = "mass_produced:past_ticks";
	public static final Map<MobEffect, Integer[][]> effectStrengths = new HashMap<>();

	static {
		effectStrengths.put(MobEffects.DAMAGE_BOOST, new Integer[][] {{1, 1, 2, 2}, {1, 2, 2, 3}, {1, 2, 3, 4}});
		effectStrengths.put(MobEffects.MOVEMENT_SPEED, new Integer[][] {{0, 1, 1, 2}, {0, 1, 2, 3}, {1, 2, 3, 3}});
		effectStrengths.put(MobEffects.DAMAGE_RESISTANCE, new Integer[][] {{0, 0, 0, 1}, {0, 0, 1, 1}, {0, 1, 1, 2}});
	}

	public PerkMassProduced(String name) {
		super(name, 3);
	}

	@Override
	public void onEntityUpdate(int level, Mob entity, SimpleDataManager values) {
		if(!entity.getCapability(ModCapabilities.ROBOT).isPresent()) return;
		IRobot robot = entity.getCapability(ModCapabilities.ROBOT).resolve().get();
		if(!robot.isActive()) return;

		values.increment(TICK_COUNTER);
		if(values.get(TICK_COUNTER) < UPDATE_RATE) return;
		if(values.get(TICK_COUNTER) > UPDATE_RATE) { //Leave one tick in between for every robot to update
			values.set(TICK_COUNTER, 0);
			return;
		}

		int allies = alliesInArea(entity, AREA_SIZE, robot.getOwner(), ent -> {
			Optional<IPerkMap> perkMap = ent.getCapability(ModCapabilities.PERKS).resolve();
			return perkMap.isPresent() && perkMap.get().contains(ModPerks.PERK_MASS_PRODUCED.get());
		}).size();

		allies = Math.min(allies, MAX_ALLIES);
		allies = (int) Math.ceil((double) allies / 2); //Effects only increase every 2 allies and are capped at 8 allies
		int duration_ticks = UPDATE_RATE * PerkBehavior.PERK_TICK_RATE * 2 + 40;

		for(MobEffect effect : effectStrengths.keySet()) {
			int amplifier = effectStrengths.get(effect)[level - 1][allies];
			if(amplifier <= 0) continue;
			entity.addEffect(new MobEffectInstance(effect, duration_ticks, amplifier - 1, true, false));
		}
	}

	@Override
	public Component getDescriptionText() {
		return Lang.localise("perk.mass_produced.desc", MAX_ALLIES, AREA_SIZE);
	}
}
