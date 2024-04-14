package com.ignis.igrobotics.common.perks;

import com.ignis.igrobotics.core.SimpleDataManager;
import com.ignis.igrobotics.core.capabilities.perks.Perk;
import com.ignis.igrobotics.core.util.Lang;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.UUID;

public class PerkAcceleration extends Perk {

	public static final UUID MODIFIER_UUID = UUID.fromString("0b956262-68ed-11ed-9022-0242ac120002");
	public static final String ACCELERATION = "perk_acceleration";
	public static final int MAX_ACC_TICKS = 50;
	public static final float MAX_ACCELERATION = 0.3f;
	public static final float SPEED_SLOWDOWN = 0.15f;

	public PerkAcceleration(String name) {
		super(name, 2);
		setStackable(false);
	}

	@Override
	public void onEntityUpdate(int level, Mob entity, SimpleDataManager values) {
		float currentAcc = values.get(ACCELERATION);
		if(entity.moveDist > 0.15) {
			values.set(ACCELERATION, Math.min(values.get(ACCELERATION) + 1, MAX_ACC_TICKS));
		} else {
			values.set(ACCELERATION, Math.max(values.get(ACCELERATION) - 20, 0));
		}
		float acceleration = values.get(ACCELERATION);
		if(acceleration == currentAcc) return;
		float scalar = (MAX_ACCELERATION + SPEED_SLOWDOWN) * (acceleration / MAX_ACC_TICKS) - SPEED_SLOWDOWN;
		AttributeInstance attributeInstance = entity.getAttribute(Attributes.MOVEMENT_SPEED);
		if(attributeInstance == null) return;
		if(attributeInstance.getModifier(MODIFIER_UUID) == null || attributeInstance.getModifier(MODIFIER_UUID).getAmount() != scalar) {
			attributeInstance.removeModifier(MODIFIER_UUID);
			attributeInstance.addTransientModifier(new AttributeModifier(MODIFIER_UUID, ACCELERATION, scalar, AttributeModifier.Operation.MULTIPLY_BASE));
		}
	}

	@Override
	public Component getDescriptionText() {
		return Lang.localise("perk.acceleration.desc", String.format("%.1f%%", SPEED_SLOWDOWN * 100), MAX_ACC_TICKS / 20, String.format("%.1f%%", MAX_ACCELERATION * 100));
	}
}
