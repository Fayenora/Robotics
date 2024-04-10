package com.ignis.igrobotics.integration.cc.apis;

import dan200.computercraft.api.lua.ILuaAPI;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.core.apis.IAPIEnvironment;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.level.LightLayer;
import org.apache.commons.compress.utils.Lists;

import java.util.List;

public class SensorAPI implements ILuaAPI {

    public static final int VISUAL_RANGE = 32;

    private final IAPIEnvironment environment;
    private final Mob mob;

    public SensorAPI(IAPIEnvironment environment, Mob mob) {
        this.environment = environment;
        this.mob = mob;
    }

    /**
     * Get the brightness of the block the robot is standing on
     * @return a number between 0-15
     */
    @LuaFunction
    public final int getBrightness() {
        return mob.level().getBrightness(LightLayer.BLOCK, mob.getOnPos());
    }

    /**
     * Get all living entities sensed by visual sensors
     * @return a list of entities
     */
    @LuaFunction
    public final List<LuaEntity> getSeenEntities() {
        TargetingConditions target = TargetingConditions.forNonCombat();
        List<LuaEntity> entities = Lists.newArrayList();
        for(Entity ent : mob.level().getEntities(mob, mob.getBoundingBox().inflate(VISUAL_RANGE))) {
            if(ent instanceof LivingEntity living && target.test(mob, living)) {
                entities.add(new LuaEntity(ent));
            }
        }
        return entities;
    }

    /* TODO
    @LuaFunction
    public final List<String> getHeardSounds() {
        return List.of();
    }
     */

    @Override
    public String[] getNames() {
        return new String[] {"sensors"};
    }
}
