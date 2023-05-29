package com.ignis.igrobotics.common.perks;

import com.ignis.igrobotics.core.SimpleDataManager;
import com.ignis.igrobotics.core.capabilities.perks.Perk;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.UUID;

public class PerkAcceleration extends Perk {
	
	public static final UUID MODIFIER_UUID = UUID.fromString("0b956262-68ed-11ed-9022-0242ac120002");
	public static final String ACCELERATION = "perk_acceleration";
	public static final int MAX_ACC_TICKS = 50;

	public PerkAcceleration(String name) {
		super(name, 2);
		setStackable(false);
	}
	
	@Override
	public void onEntityUpdate(int level, Entity entity, SimpleDataManager values) {
		if(!(entity instanceof Mob mob)) return;
		float currentAcc = Math.min(values.get(ACCELERATION), MAX_ACC_TICKS);
		if(entity.moveDist > 0.15) {
			values.increment(ACCELERATION);
		} else {
			int decrementedValue = Math.max(values.get(ACCELERATION) - 30, 0);
			values.set(ACCELERATION, decrementedValue);
		}
		float acceleration = Math.min(values.get(ACCELERATION), MAX_ACC_TICKS);
		if(acceleration == currentAcc) return;
		float scalar =  (float) (0.45 * (acceleration / MAX_ACC_TICKS) - 0.15);
		//TODO: This rapid changing of attribute modifiers might cause efficiency or instability issues
		//Try to more intelligently only reapply when the values actually change
		mob.getAttributes().getInstance(Attributes.MOVEMENT_SPEED).removeModifier(MODIFIER_UUID);
		mob.getAttributes().getInstance(Attributes.MOVEMENT_SPEED).addTransientModifier(new AttributeModifier(MODIFIER_UUID, ACCELERATION, scalar, AttributeModifier.Operation.MULTIPLY_BASE));
	}

}
