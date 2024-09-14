package com.io.norabotics.common.handlers;

import com.io.norabotics.Robotics;
import com.io.norabotics.common.capabilities.ModCapabilities;
import com.io.norabotics.common.helpers.util.InventoryUtil;
import com.io.norabotics.common.robot.EnumModuleSlot;
import com.io.norabotics.common.robot.RobotPart;
import com.io.norabotics.integration.config.RoboticsConfig;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;

@Mod.EventBusSubscriber(modid = Robotics.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PartsBehavior {

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if(entity.level().isClientSide() || !entity.level().getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) return;
        entity.getCapability(ModCapabilities.PARTS).ifPresent(parts -> {
            for(EnumModuleSlot slot : EnumModuleSlot.values()) {
                for(ItemStack stack : parts.getBodyParts(slot)) {
                    if(Robotics.RANDOM.nextDouble() < RoboticsConfig.general.limbDropChance.get()) {
                        InventoryUtil.dropItem(entity, stack);
                    }
                }
            }
        });
    }
}
