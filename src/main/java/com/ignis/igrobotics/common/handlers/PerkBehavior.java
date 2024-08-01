package com.ignis.igrobotics.common.handlers;

import com.ignis.igrobotics.Robotics;
import com.ignis.igrobotics.core.capabilities.ModCapabilities;
import com.ignis.igrobotics.core.capabilities.perks.Perk;
import com.ignis.igrobotics.core.events.PerkChangeEvent;
import com.ignis.igrobotics.core.util.Tuple;
import com.ignis.igrobotics.definitions.ModAttributes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = Robotics.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PerkBehavior {

    public static final int PERK_TICK_RATE = 10;

    @SubscribeEvent
    public static void onPerkChange(PerkChangeEvent event) {
        LivingEntity entity = event.getEntity();
        @SuppressWarnings("unchecked")
        AttributeSupplier defaults = DefaultAttributes.getSupplier((EntityType<? extends LivingEntity>) entity.getType());
        AttributeMap tempAttributeMap = new AttributeMap(defaults);
        for(Tuple<Perk, Integer> tup : event.getPerks()) {
            tempAttributeMap.addTransientAttributeModifiers(tup.getFirst().getAttributeModifiers(tup.getSecond()));
        }
        //Copy the values to the actual attribute map
        for(Attribute attribute : ForgeRegistries.ATTRIBUTES.getValues()) {
            AttributeInstance instance = entity.getAttributes().getInstance(attribute);
            if(tempAttributeMap.hasAttribute(attribute) && instance != null) {
                instance.setBaseValue(tempAttributeMap.getValue(attribute));
                ModAttributes.onAttributeChanged(entity, instance);
            }
        }
    }

    @SubscribeEvent
    public static void onEntityUpdate(LivingEvent.LivingTickEvent event) {
        if(!(event.getEntity() instanceof Mob mob)) return;
        if(mob.tickCount % PERK_TICK_RATE == 0) {
            mob.getCapability(ModCapabilities.PERKS).ifPresent(perks -> {
                for(Tuple<Perk, Integer> tup : perks) {
                    tup.getFirst().onEntityUpdate(tup.getSecond(), mob, perks.values());
                }
            });
        }
    }

    @SubscribeEvent
    public static void jump(LivingEvent.LivingJumpEvent event) {
        if(!(event.getEntity() instanceof Mob mob)) return;
        event.getEntity().getCapability(ModCapabilities.PERKS).ifPresent(perks -> {
            for(Tuple<Perk, Integer> tup : perks) {
                tup.getFirst().onEntityJump(tup.getSecond(), mob, perks.values());
            }
        });
    }

    @SubscribeEvent
    public static void onDamage(LivingHurtEvent event) {
        Entity causingEntity = event.getSource().getEntity();
        Entity targetEntity = event.getEntity();
        // First, apply the perks of the attacker
        if(causingEntity != null && targetEntity != null) {
            if(!(causingEntity instanceof Mob mob)) return;
            causingEntity.getCapability(ModCapabilities.PERKS).ifPresent(perks -> {
                int damageChange = 0;
                for(Tuple<Perk, Integer> tup : perks) {
                    damageChange += tup.getFirst().attackEntityAsMob(tup.getSecond(), mob, targetEntity, perks.values());
                }
                event.setAmount(event.getAmount() + damageChange);
            });
        }
        // Secondly, apply the perks of the hurt entity
        if(targetEntity instanceof Mob mob) {
            targetEntity.getCapability(ModCapabilities.PERKS).ifPresent(perks -> {
                for(Tuple<Perk, Integer> tup : perks) {
                    event.setAmount(tup.getFirst().damageEntity(tup.getSecond(), mob, event.getSource(), event.getAmount(), perks.values()));
                }
            });
        }
    }
}
