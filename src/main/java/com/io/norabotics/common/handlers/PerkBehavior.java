package com.io.norabotics.common.handlers;

import com.io.norabotics.Robotics;
import com.io.norabotics.common.capabilities.ModCapabilities;
import com.io.norabotics.common.capabilities.impl.perk.Perk;
import com.io.norabotics.common.content.events.ModuleActivationEvent;
import com.io.norabotics.common.content.events.PerkChangeEvent;
import com.io.norabotics.common.helpers.types.TempAttrMap;
import com.io.norabotics.common.helpers.types.Tuple;
import com.io.norabotics.common.robot.*;
import com.io.norabotics.definitions.ModAttributes;
import com.io.norabotics.definitions.robotics.ModModules;
import com.io.norabotics.definitions.robotics.ModPerks;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.item.ItemStack;
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
        if(entity.level().isClientSide) return;
        @SuppressWarnings("unchecked")
        AttributeSupplier defaults = DefaultAttributes.getSupplier((EntityType<? extends LivingEntity>) entity.getType());
        TempAttrMap tempAttributeMap = new TempAttrMap(defaults);
        for(Tuple<Perk, Integer> tup : event.getPerks()) {
            tempAttributeMap.addTransientAttributeModifiers(tup.getFirst().getAttributeModifiers(tup.getSecond()));
        }
        //Copy the values to the actual attribute map
        for(Attribute attribute : tempAttributeMap.getAttributes()) {
            AttributeInstance instance = entity.getAttributes().getInstance(attribute);
            if(instance != null) {
                instance.setBaseValue(tempAttributeMap.getValue(attribute));
                ModAttributes.onAttributeChanged(entity, instance);
            }
        }
    }

    @SubscribeEvent
    public static void onDayChange(TickEvent.ServerTickEvent event) {
        if(0 != event.getServer().overworld().dayTime()) return;
        for(ServerLevel level : event.getServer().getAllLevels()) {
            for(Entity entity : level.getAllEntities()) {
                entity.getCapability(ModCapabilities.PARTS).ifPresent(parts -> {
                    for(EnumModuleSlot slot : EnumModuleSlot.primaries()) {
                        if(Math.random() > CORROSION_CHANCE_PER_DAY) continue;
                        EnumRobotMaterial material = parts.materialForSlot(slot);
                        for(ItemStack stack : parts.getBodyParts(slot)) {
                            if(ModModules.get(stack).getPerks().contains(ModPerks.PERK_CORRODABLE.get())) {
                                parts.setBodyPart(EnumRobotPart.valueOf(slot), material.getWeatheredMaterial());
                            }
                        }
                    }
                });
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
