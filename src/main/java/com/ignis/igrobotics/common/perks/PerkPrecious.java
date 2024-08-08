package com.ignis.igrobotics.common.perks;

import com.google.common.base.Predicates;
import com.ignis.igrobotics.core.EntitySearch;
import com.ignis.igrobotics.core.SimpleDataManager;
import com.ignis.igrobotics.core.capabilities.ModCapabilities;
import com.ignis.igrobotics.core.capabilities.perks.Perk;
import com.ignis.igrobotics.core.capabilities.robot.IRobot;
import com.ignis.igrobotics.core.robot.RobotCommand;
import com.ignis.igrobotics.core.robot.Selection;
import com.ignis.igrobotics.definitions.ModCommands;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;

import java.util.List;

public class PerkPrecious extends Perk {
    public PerkPrecious(String name) {
        super(name);
    }

    @Override
    public float onDamage(int level, Mob robot, DamageSource dmgSource, float damage, SimpleDataManager values) {
        //TODO FORCE allies to support this robot
        IRobot iRobot = robot.getCapability(ModCapabilities.ROBOT).resolve().get();
        if(iRobot.hasOwner() && dmgSource.getEntity() != null) {
            for(Entity ent : alliesInArea(robot, 16, iRobot.getOwner(), Predicates.alwaysTrue())) {
                ent.getCapability(ModCapabilities.COMMANDS).ifPresent(commands -> {
                    commands.addCommand(new RobotCommand(ModCommands.ATTACK.get(), List.of(Selection.of(new EntitySearch(dmgSource.getEntity().getUUID())))));
                });
            }
        }
        return super.onDamage(level, robot, dmgSource, damage, values);
    }
}
