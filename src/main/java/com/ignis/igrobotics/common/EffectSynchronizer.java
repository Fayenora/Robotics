package com.ignis.igrobotics.common;

import com.google.common.collect.HashMultimap;
import com.ignis.igrobotics.Robotics;
import com.ignis.igrobotics.network.messages.NetworkHandler;
import com.ignis.igrobotics.network.messages.client.PacketSetEntityEffects;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = Robotics.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EffectSynchronizer {

    private static final HashMultimap<Integer, UUID> entitiesToSynchronize = HashMultimap.create();

    public static void onEffectChanged(LivingEntity entity, Collection<MobEffectInstance> effectInstances) {
        for(UUID uuid : entitiesToSynchronize.get(entity.getId())) {
            ServerPlayer player = (ServerPlayer) entity.level.getPlayerByUUID(uuid);
            NetworkHandler.sendToPlayer(new PacketSetEntityEffects(entity.getId(), effectInstances), player);
        }
    }

    @SubscribeEvent
    public static void onEffectAdded(MobEffectEvent.Added event) {
        if(event.getEntity().level.isClientSide()) return;
        if(!entitiesToSynchronize.containsKey(event.getEntity().getId())) return;
        Collection<MobEffectInstance> instances = new ArrayList(event.getEntity().getActiveEffects());
        instances.add(event.getEffectInstance());
        onEffectChanged(event.getEntity(), instances);
    }

    @SubscribeEvent
    public static void onEffectRemoved(MobEffectEvent.Remove event) {
        if(event.getEntity().level.isClientSide()) return;
        if(!entitiesToSynchronize.containsKey(event.getEntity().getId())) return;
        Collection<MobEffectInstance> instances = new ArrayList(event.getEntity().getActiveEffects());
        instances.remove(event.getEffectInstance());
        onEffectChanged(event.getEntity(), instances);
    }

    @SubscribeEvent
    public static void onEffectExpired(MobEffectEvent.Expired event) {
        if(event.getEntity().level.isClientSide()) return;
        if(!entitiesToSynchronize.containsKey(event.getEntity().getId())) return;
        Collection<MobEffectInstance> instances = new ArrayList(event.getEntity().getActiveEffects());
        instances.remove(event.getEffectInstance());
        onEffectChanged(event.getEntity(), instances);
    }

    public static void addWatcher(Entity entity, ServerPlayer watcher) {
        entitiesToSynchronize.put(entity.getId(), watcher.getUUID());
    }

    public static void removeWatcher(Entity entity, ServerPlayer watcher) {
        entitiesToSynchronize.remove(entity.getId(), watcher.getUUID());
    }
}
