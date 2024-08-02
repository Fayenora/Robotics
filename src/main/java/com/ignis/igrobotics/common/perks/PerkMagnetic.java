package com.ignis.igrobotics.common.perks;

import com.ignis.igrobotics.core.SimpleDataManager;
import com.ignis.igrobotics.core.capabilities.perks.Perk;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;

public class PerkMagnetic extends Perk {

    public static float FORCE = 0.1f;

    public PerkMagnetic(String name) {
        super(name);
    }

    @Override
    public void onEntityUpdate(int level, Mob entity, SimpleDataManager values) {
        for(Entity ent : entitiesInArea(entity, 4, ent -> ent instanceof ItemEntity)) {
            if(!(ent instanceof ItemEntity item)) continue;
            item.addDeltaMovement(item.position().subtract(entity.position()).scale(FORCE));
        }
        super.onEntityUpdate(level, entity, values);
    }
}
