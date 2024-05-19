package com.ignis.igrobotics.common.perks.modules;

import com.ignis.igrobotics.core.SimpleDataManager;
import com.ignis.igrobotics.core.capabilities.perks.Perk;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;

import java.util.List;

public class PerkUnarmedAttack extends Perk {

    public PerkUnarmedAttack(String name) {
        super(name, 10);
    }

    @Override
    public float attackEntityAsMob(int level, Mob attacker, Entity toAttack, SimpleDataManager values) {
        float damageToAdd = 0;
        if(attacker.getMainHandItem().isEmpty()) {
            damageToAdd = (float) level / 2;
        }
        return super.attackEntityAsMob(level, attacker, toAttack, values) + damageToAdd;
    }

    @Override
    public Component getDisplayText(int level) {
        return ComponentUtils.formatList(List.of(Component.literal("+" + level + " "), localized()), Component.empty());
    }
}
