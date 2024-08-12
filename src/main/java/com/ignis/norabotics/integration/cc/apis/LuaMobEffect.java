package com.ignis.norabotics.integration.cc.apis;

import dan200.computercraft.api.lua.LuaFunction;
import net.minecraft.world.effect.MobEffectInstance;

public class LuaMobEffect {

    private final String type;
    private final int amplifier, duration;

    public LuaMobEffect(MobEffectInstance effectInstance) {
        this.type = effectInstance.getDescriptionId();
        amplifier = effectInstance.getAmplifier();
        duration = effectInstance.getDuration();
    }

    @LuaFunction
    public final String getType() {
        return type;
    }

    @LuaFunction
    public final int getAmplifier() {
        return amplifier;
    }

    //FIXME: Seems to return client values? Values are in ranges 250-150 and get updated periodically
    @LuaFunction
    public final int getDuration() {
        return duration;
    }
}
