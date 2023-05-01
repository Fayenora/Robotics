package com.ignis.igrobotics.common;

import com.ignis.igrobotics.Robotics;
import com.ignis.igrobotics.core.capabilities.ModCapabilities;
import com.ignis.igrobotics.core.capabilities.perks.Perk;
import com.ignis.igrobotics.core.util.Tuple;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Robotics.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PerkBehavior {

    public static final int PERK_TICK_RATE = 10;
    private static int ticks;

    @SubscribeEvent
    public static void onEntityUpdate(LivingEvent.LivingTickEvent event) {
        if(ticks++ > PERK_TICK_RATE) {
            event.getEntity().getCapability(ModCapabilities.PERKS).ifPresent(perks -> {
                for(Tuple<Perk, Integer> tup : perks) {
                    tup.getFirst().onEntityUpdate(tup.getSecond(), event.getEntity(), perks.values());
                }
            });
        }
    }

    @SubscribeEvent
    public static void jump(LivingEvent.LivingJumpEvent event) {
        event.getEntity().getCapability(ModCapabilities.PERKS).ifPresent(perks -> {
            for(Tuple<Perk, Integer> tup : perks) {
                tup.getFirst().onEntityJump(tup.getSecond(), event.getEntity(), perks.values());
            }
        });
    }

    @SubscribeEvent
    public static void onDamage(LivingHurtEvent event) {
        Entity causingEntity = event.getSource().getEntity();
        Entity targetEntity = event.getSource().getDirectEntity();
        // First, apply the perks of the attacker
        if(causingEntity != null && targetEntity != null) {
            causingEntity.getCapability(ModCapabilities.PERKS).ifPresent(perks -> {
                int damageChange = 0;
                for(Tuple<Perk, Integer> tup : perks) {
                    damageChange += tup.getFirst().attackEntityAsMob(tup.getSecond(), causingEntity, targetEntity, perks.values());
                }
                event.setAmount(event.getAmount() + damageChange);
            });
        }
        // Secondly, apply the perks of the hurt entity
        if(targetEntity != null) {
            targetEntity.getCapability(ModCapabilities.PERKS).ifPresent(perks -> {
                for(Tuple<Perk, Integer> tup : perks) {
                    event.setAmount(tup.getFirst().damageEntity(tup.getSecond(), targetEntity, event.getSource(), event.getAmount(), perks.values()));
                }
            });
        }
    }
}
