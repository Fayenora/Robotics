package com.ignis.igrobotics.common;

import com.ignis.igrobotics.Robotics;
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
    public static final HashMap<String, LivingEntity> allLivingEntities = new HashMap<>();

    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        if(!allLivingEntities.isEmpty()) return;
        if(!(event.getLevel() instanceof Level level)) return;
        for(EntityType<?> type : ForgeRegistries.ENTITY_TYPES.getValues()) {
            if(LivingEntity.class.isAssignableFrom(type.getBaseClass())) {
                allLivingEntities.put(type.getDescriptionId(), (LivingEntity) type.create(level));
            }
        }
    }
}
