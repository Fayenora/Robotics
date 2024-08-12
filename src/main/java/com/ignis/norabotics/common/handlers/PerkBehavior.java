package com.ignis.norabotics.common.handlers;

import com.ignis.norabotics.Robotics;
import com.ignis.norabotics.common.capabilities.ModCapabilities;
import com.ignis.norabotics.common.capabilities.impl.perk.Perk;
import com.ignis.norabotics.common.content.events.ModuleActivationEvent;
import com.ignis.norabotics.common.content.events.PerkChangeEvent;
import com.ignis.norabotics.common.helpers.types.Tuple;
import com.ignis.norabotics.common.robot.EnumRobotPart;
import com.ignis.norabotics.common.robot.RobotPart;
import com.ignis.norabotics.definitions.ModAttributes;
import com.ignis.norabotics.definitions.ModPerks;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = Robotics.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PerkBehavior {

    public static final int PERK_TICK_RATE = 10;
    public static final float CORROSION_CHANCE_PER_DAY = 0.3f;

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
    public static void onDayChange(TickEvent.ServerTickEvent event) {
        if(0 == event.getServer().overworld().dayTime()) {
            for(ServerLevel level : event.getServer().getAllLevels()) {
                for(Entity entity : level.getAllEntities()) {
                    entity.getCapability(ModCapabilities.PARTS).ifPresent(parts -> {
                        for(EnumRobotPart part : EnumRobotPart.values()) {
                            if(Math.random() > CORROSION_CHANCE_PER_DAY) continue;
                            RobotPart robotPart = parts.getBodyPart(part);
                            if(robotPart.getPerks().contains(ModPerks.PERK_CORRODABLE.get())) {
                                parts.setBodyPart(part, robotPart.getMaterial().getWeatheredMaterial());
                            }
                        }
                    });
                }
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
    public static void onModuleActivated(ModuleActivationEvent event) {
        if(!(event.getCaster() instanceof Mob mob)) return;
        event.getCaster().getCapability(ModCapabilities.PERKS).ifPresent(perks -> {
            for(Tuple<Perk, Integer> tup : perks) {
                tup.getFirst().onModuleActivated(tup.getSecond(), mob, perks.values());
            }
        });
    }

    @SubscribeEvent
    public static void onDamage(LivingHurtEvent event) {
        Entity causingEntity = event.getSource().getEntity();
        Entity targetEntity = event.getEntity();
        float originalAmount = event.getAmount();
        // First, apply the perks of the attacker
        if(causingEntity != null && targetEntity != null) {
            if(!(causingEntity instanceof Mob mob)) return;
            causingEntity.getCapability(ModCapabilities.PERKS).ifPresent(perks -> {
                float damageChange = 0;
                for(Tuple<Perk, Integer> tup : perks) {
                    damageChange += tup.getFirst().onAttack(tup.getSecond(), mob, targetEntity, perks.values());
                }
                event.setAmount(event.getAmount() + damageChange);
            });
        }
        // Secondly, apply the perks of the hurt entity
        if(targetEntity instanceof Mob mob) {
            targetEntity.getCapability(ModCapabilities.PERKS).ifPresent(perks -> {
                for(Tuple<Perk, Integer> tup : perks) {
                    event.setAmount(tup.getFirst().onDamage(tup.getSecond(), mob, event.getSource(), event.getAmount(), perks.values()));
                }
            });
        }
        if(event.getAmount() <= 0 && originalAmount != event.getAmount()) {
            event.setCanceled(true);
        }
    }
}
