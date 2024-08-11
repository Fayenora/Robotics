package com.ignis.igrobotics.common.handlers;

import com.ignis.igrobotics.Robotics;
import com.ignis.igrobotics.common.WorldData;
import com.ignis.igrobotics.common.capabilities.ModCapabilities;
import com.ignis.igrobotics.common.content.blockentity.StorageBlockEntity;
import com.ignis.igrobotics.common.content.blocks.StorageBlock;
import com.ignis.igrobotics.definitions.ModBlocks;
import com.ignis.igrobotics.definitions.ModPerks;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
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
                        entity.getCapability(ModCapabilities.PERKS).orElse(ModCapabilities.NO_PERKS).contains(ModPerks.PERK_LUMINOUS.get())
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
        if(!state.getBlock().equals(ModBlocks.ROBOT_STORAGE.get())) return;
        int offset = state.getValue(StorageBlock.HALF).equals(DoubleBlockHalf.LOWER) ? 0: 1;
        if(!(event.getLevel().getBlockEntity(event.getPos().below(offset)) instanceof StorageBlockEntity storage)) return;
        storage.exitStorage(null);
    }
}
