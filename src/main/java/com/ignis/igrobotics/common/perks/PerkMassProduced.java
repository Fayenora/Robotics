package com.ignis.igrobotics.common.perks;

import com.ignis.igrobotics.common.handlers.PerkBehavior;
import com.ignis.igrobotics.common.handlers.RobotBehavior;
import com.ignis.igrobotics.core.SimpleDataManager;
import com.ignis.igrobotics.core.access.EnumPermission;
import com.ignis.igrobotics.core.capabilities.ModCapabilities;
import com.ignis.igrobotics.core.capabilities.perks.IPerkMapCap;
import com.ignis.igrobotics.core.capabilities.perks.Perk;
import com.ignis.igrobotics.core.capabilities.robot.IRobot;
import com.ignis.igrobotics.core.util.Lang;
import com.ignis.igrobotics.integration.config.RoboticsConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;

import java.util.HashMap;
import java.util.Optional;

public class PerkMassProduced extends Perk {

	public static final int UPDATE_RATE = 9;
	public static final int MAX_ALLIES = 6;
	public static final int AREA_SIZE = 8;
	public static final String TICK_COUNTER = "mass_produced:past_ticks";
	public static final HashMap<MobEffect, Integer[][]> effectStrengths = new HashMap<>();

	static {
		effectStrengths.put(MobEffects.DAMAGE_BOOST, new Integer[][] {{1, 1, 2, 2}, {1, 2, 2, 3}, {1, 2, 3, 4}});
		effectStrengths.put(MobEffects.MOVEMENT_SPEED, new Integer[][] {{0, 1, 1, 2}, {0, 1, 2, 3}, {1, 2, 3, 3}});
		effectStrengths.put(MobEffects.DAMAGE_RESISTANCE, new Integer[][] {{0, 0, 0, 1}, {0, 0, 1, 1}, {0, 1, 1, 2}});
		//TODO: Custom regeneration effect?
	}

	public PerkMassProduced(String name) {
		super(name, 3);
	}

	@Override
	public void onEntityUpdate(int level, Mob entity, SimpleDataManager values) {
		entity.getCapability(ModCapabilities.ROBOT).ifPresent(robot -> {
			if(!robot.isActive()) return;

			values.increment(TICK_COUNTER);
			if(values.get(TICK_COUNTER) < UPDATE_RATE) return;
			if(values.get(TICK_COUNTER) > UPDATE_RATE) { //Leave one tick in between for every robot to update
				values.set(TICK_COUNTER, 0);
				return;
			}

			BlockPos lower = entity.blockPosition().relative(Direction.DOWN, AREA_SIZE).relative(Direction.SOUTH, AREA_SIZE).relative(Direction.EAST, AREA_SIZE);
			BlockPos upper = entity.blockPosition().relative(Direction.UP, AREA_SIZE).relative(Direction.NORTH, AREA_SIZE).relative(Direction.WEST, AREA_SIZE);
			AABB area = new AABB(lower, upper);
			int allies = entity.level.getEntities(entity, area, ent -> {
				Optional<IRobot> otherRobot = entity.getCapability(ModCapabilities.ROBOT).resolve();
				Optional<IPerkMapCap> perkMap = entity.getCapability(ModCapabilities.PERKS).resolve();
				if(otherRobot.isEmpty() || !otherRobot.get().isActive()) return false;
				if(!RobotBehavior.hasAccess(robot.getOwner(), ent, EnumPermission.ALLY)) return false;
				return perkMap.isPresent() && perkMap.get().contains(RoboticsConfig.current().perks.PERK_MASS_PRODUCED);
			}).size();

			if(allies <= 1) return; //No allies but itself
			allies = Math.min(allies, MAX_ALLIES);
			allies = (int) Math.ceil((double) allies / 2); //Effects only increase every 2 allies and are capped at 8 allies
			int duration_ticks = UPDATE_RATE * PerkBehavior.PERK_TICK_RATE * 2 + 40;

			for(MobEffect effect : effectStrengths.keySet()) {
				int amplifier = effectStrengths.get(effect)[level - 1][allies];
				if(amplifier <= 0) continue;
				entity.addEffect(new MobEffectInstance(effect, duration_ticks, amplifier, true, false));
			}
		});
	}

	@Override
	public Component getDescriptionText() {
		return Lang.localise("perk.mass_produced.desc", MAX_ALLIES, AREA_SIZE);
	}
}
