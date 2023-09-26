package com.ignis.igrobotics.common;

import com.ignis.igrobotics.Robotics;
import com.ignis.igrobotics.common.blockentity.StorageBlockEntity;
import com.ignis.igrobotics.common.blocks.StorageBlock;
import com.ignis.igrobotics.core.capabilities.ModCapabilities;
import com.ignis.igrobotics.definitions.ModBlocks;
import com.ignis.igrobotics.integration.config.RoboticsConfig;
import com.ignis.igrobotics.network.NetworkHandler;
import com.ignis.igrobotics.network.messages.client.PacketSyncConfigs;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Robotics.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EventHandler {

    @SubscribeEvent
    public static void onEntitySpawn(EntityJoinLevelEvent event) {
        if(event.getLevel().isClientSide()) return;
        if(!(event.getEntity() instanceof PathfinderMob mob)) return;
        if(mob.getMobType() == MobType.UNDEAD) {
            mob.goalSelector.addGoal(3, new AvoidEntityGoal<>(mob, Mob.class, 32, 0, 0.1, entity ->
                        entity.getCapability(ModCapabilities.PERKS).orElse(ModCapabilities.NO_PERKS).contains(RoboticsConfig.current().perks.PERK_LUMINOUS)
            ));
        }
    }

    @SubscribeEvent
    public static void onEntityDeath(LivingDeathEvent event) {
        WorldData data = WorldData.get();
        data.releaseRobotFromCommandGroup(event.getEntity());
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if(event.getLevel().isClientSide()) return;
        BlockState state = event.getLevel().getBlockState(event.getPos());
        if(state.getValue(StorageBlock.HALF).equals(DoubleBlockHalf.LOWER)) return;
        if(state.getBlock().equals(ModBlocks.ROBOT_STORAGE.get())) return;
        if(!(event.getLevel().getBlockEntity(event.getPos()) instanceof StorageBlockEntity storage)) return;
        storage.exitStorage();
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if(!(event.getEntity() instanceof ServerPlayer serverPlayer)) return;
        if(!Minecraft.getInstance().isLocalServer()) {
            NetworkHandler.sendToPlayer(new PacketSyncConfigs(RoboticsConfig.current()), serverPlayer);
        }
    }
}
