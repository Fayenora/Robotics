package com.ignis.norabotics.common.content.perks;

import com.ignis.norabotics.common.capabilities.impl.perk.Perk;
import com.ignis.norabotics.common.helpers.types.SimpleDataManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;

public class PerkAttractant extends Perk {

    public static float FORCE = 0.1f;

    public PerkAttractant(String name) {
        super(name);
    }

    @Override
    public void onEntityUpdate(int level, Mob entity, SimpleDataManager values) {
        for(Entity ent : entitiesInArea(entity, 4, ent -> ent instanceof Projectile)) {
            if(!(ent instanceof Projectile projectile)) continue;
            Vec3 idealHitDirection = projectile.position().subtract(entity.position().add(0, 1, 0)).scale(projectile.getDeltaMovement().length());
            projectile.setDeltaMovement(projectile.getDeltaMovement().lerp(idealHitDirection, FORCE));
        }
        super.onEntityUpdate(level, entity, values);
    }
}
