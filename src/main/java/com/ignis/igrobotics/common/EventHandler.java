package com.ignis.igrobotics.common;

import com.ignis.igrobotics.Robotics;
import com.ignis.igrobotics.core.capabilities.ModCapabilities;
import com.ignis.igrobotics.integration.config.RoboticsConfig;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Robotics.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EventHandler {

    @SubscribeEvent
    public static void onEntitySpawn(EntityJoinLevelEvent event) {
        if(event.getLevel().isClientSide()) return;
		if(!(event.getEntity() instanceof PathfinderMob mob)) return;
        if(mob.getMobType() == MobType.UNDEAD) {
            mob.goalSelector.addGoal(3, new AvoidEntityGoal<>(mob, Mob.class, 32, 0, 0.1, entity ->
                        entity.getCapability(ModCapabilities.PERKS).orElse(ModCapabilities.NO_PERKS).contains(RoboticsConfig.current().perks.PERK_LUMINOUS)
            ));
        }
    }
}
