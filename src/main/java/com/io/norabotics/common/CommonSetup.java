package com.io.norabotics.common;

import com.io.norabotics.Robotics;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;

@Mod.EventBusSubscriber(modid = Robotics.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CommonSetup {

    /**
     * List of basic instances of most living entities, excluding special ones like lightning. Only contains elements once a world is loaded
     */
    public static final HashMap<EntityType<?>, LivingEntity> allLivingEntities = new HashMap<>();

    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        if(!allLivingEntities.isEmpty() && event.getLevel().equals(allLivingEntities.get(EntityType.CREEPER).level())) return;
        if(!(event.getLevel() instanceof Level level)) return;
        for(EntityType<?> type : ForgeRegistries.ENTITY_TYPES.getValues()) {
            Entity entity = type.create(level);
            if(entity instanceof LivingEntity living) {
                allLivingEntities.put(type, living);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends Entity> Class<T> getClassOf(EntityType<T> type) {
        return (Class<T>) allLivingEntities.get(type).getClass();
    }
}
