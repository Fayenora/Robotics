package com.ignis.igrobotics.core.events;

import com.ignis.igrobotics.core.capabilities.perks.IPerkMap;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Event;

public class PerkChangeEvent extends Event {

    private final LivingEntity entity;
    private final IPerkMap perks;

    public PerkChangeEvent(LivingEntity entity, IPerkMap perks) {
        this.entity = entity;
        this.perks = perks;
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public IPerkMap getPerks() {
        return perks;
    }
}
