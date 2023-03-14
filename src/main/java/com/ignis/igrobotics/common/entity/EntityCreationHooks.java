package com.ignis.igrobotics.common.entity;

import com.ignis.igrobotics.Robotics;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Robotics.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class EntityCreationHooks {

    @SubscribeEvent
    public static void robotAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntityTypes.ROBOT.get(), RobotEntity.defaultRobotAttributes());
    }

}
