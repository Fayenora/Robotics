package com.ignis.igrobotics.common.actions;

import com.ignis.igrobotics.core.capabilities.ModCapabilities;
import com.ignis.igrobotics.core.capabilities.shield.IShielded;
import com.ignis.igrobotics.definitions.ModActions;
import com.mojang.serialization.Codec;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.entity.LivingEntity;

public class ShieldAction implements IAction {

    private final String name;
    private final TextColor color;

    public ShieldAction(String name, TextColor color) {
        this.name = name;
        this.color = color;
    }

    @Override
    public boolean execute(LivingEntity caster, int duration) {
        if(caster.level().isClientSide || !caster.isAlive()) return false;
        if(!caster.getCapability(ModCapabilities.SHIELDED).isPresent()) return false;
        IShielded shield = caster.getCapability(ModCapabilities.SHIELDED).resolve().get();
        return shield.setActive(!shield.isShielded());
    }

    @Override
    public Codec<? extends IAction> codec() {
        return ModActions.SHIELD.get();
    }

    @Override
    public TextColor getColor() {
        return color;
    }

    @Override
    public String toString() {
        return name;
    }
}
