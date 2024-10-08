package com.io.norabotics.common.content.perks;

import com.io.norabotics.common.capabilities.ModCapabilities;
import com.io.norabotics.common.capabilities.impl.perk.Perk;
import com.io.norabotics.common.helpers.types.SimpleDataManager;
import com.io.norabotics.definitions.robotics.ModPerks;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.Vec3;

public class PerkMagnetic extends Perk {

    public static float FORCE = 0.1f;

    public PerkMagnetic(String name) {
        super(name);
    }

    @Override
    public void onEntityUpdate(int level, Mob entity, SimpleDataManager values) {
        entity.aiStep();
        for(Entity ent : entitiesInArea(entity, 5, ent -> ent instanceof ItemEntity)) {
            if(!(ent instanceof ItemEntity item)) continue;
            if(item.getOwner() != null && item.getOwner().getUUID().equals(entity.getUUID())) {
                float distance = item.distanceTo(entity);
                Vec3 movement = entity.position().subtract(item.position()).scale(FORCE).add(0, 0.02, 0);
                item.addDeltaMovement(movement);
                item.setPickUpDelay((int) (distance * distance));
                item.setNoGravity(true);
            } else if(item.getOwner() == null || !item.getOwner().getCapability(ModCapabilities.PERKS).orElse(ModCapabilities.NO_PERKS).contains(ModPerks.PERK_MAGNETIC.get())) {
                item.setThrower(entity.getUUID());
            }
        }
        super.onEntityUpdate(level, entity, values);
    }
}
