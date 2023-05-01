package com.ignis.igrobotics.common;

import com.ignis.igrobotics.Robotics;
import com.ignis.igrobotics.core.capabilities.ModCapabilities;
import com.ignis.igrobotics.core.robot.RobotPart;
import com.ignis.igrobotics.core.util.ItemStackUtils;
import com.ignis.igrobotics.integration.config.RoboticsConfig;
import net.minecraft.world.entity.LivingEntity;
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
        if(entity.level.isClientSide() || !entity.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) return;
        entity.getCapability(ModCapabilities.PARTS).ifPresent(parts -> {
            Random r = new Random();
            for(RobotPart part : parts.getBodyParts()) {
                if (parts.hasBodyPart(part.getPart()) && r.nextDouble() < RoboticsConfig.general.limbDropChance.get()) {
                    ItemStackUtils.dropItem(entity, part.getItemStack(1));
                }
            }
        });
    }
}
