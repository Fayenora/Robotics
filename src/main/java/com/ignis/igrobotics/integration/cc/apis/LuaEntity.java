package com.ignis.igrobotics.integration.cc.apis;

import dan200.computercraft.api.lua.LuaFunction;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.registries.ForgeRegistries;

public class LuaEntity {

    private final Entity entity;

    public LuaEntity(Entity entity) {
        this.entity = entity;
    }

    @LuaFunction
    public final String getName() {
        return entity.getName().getString();
    }

    @LuaFunction
    public final String getUUID() {
        return entity.getStringUUID();
    }

    public final String getType() {
        return ForgeRegistries.ENTITY_TYPES.getKey(entity.getType()).toString();
    }
}
