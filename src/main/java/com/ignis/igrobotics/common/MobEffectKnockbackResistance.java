package com.ignis.igrobotics.common;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class MobEffectKnockbackResistance extends MobEffect {
    public MobEffectKnockbackResistance() {
        super(MobEffectCategory.BENEFICIAL, 0x000000);
        addAttributeModifier(Attributes.KNOCKBACK_RESISTANCE, "7107DE5E-7CE8-4030-940E-514C1F160895", 1, AttributeModifier.Operation.ADDITION);
    }
}
