package com.io.norabotics.common.handlers;

import com.io.norabotics.Robotics;
import com.io.norabotics.common.WorldData;
import com.io.norabotics.common.capabilities.ModCapabilities;
import com.io.norabotics.common.content.blockentity.FactoryBlockEntity;
import com.io.norabotics.common.content.blockentity.MachineArmBlockEntity;
import com.io.norabotics.common.content.blockentity.StorageBlockEntity;
import com.io.norabotics.common.content.blocks.StorageBlock;
import com.io.norabotics.common.helpers.util.InventoryUtil;
import com.io.norabotics.definitions.ModBlocks;
import com.io.norabotics.definitions.robotics.ModPerks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.IItemHandlerModifiable;

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

    /**
     * Run before any loot tables from the data pack are called
     * @param event
     */
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if(event.getLevel().isClientSide()) return;
        BlockState state = event.getLevel().getBlockState(event.getPos());
        if(state.getBlock().equals(ModBlocks.ROBOT_STORAGE.get())) {
            int offset = state.getValue(StorageBlock.HALF).equals(DoubleBlockHalf.LOWER) ? 0: 1;
            if(!(event.getLevel().getBlockEntity(event.getPos().below(offset)) instanceof StorageBlockEntity storage)) return;
            storage.exitStorage(null);
        }
        if(state.getBlock().equals(ModBlocks.MACHINE_ARM.get())) {
            if(!(event.getLevel().getBlockEntity(event.getPos()) instanceof MachineArmBlockEntity machineArm)) return;
            machineArm.dropGrabbedItem();
        }
        if(state.getBlock().equals(ModBlocks.ROBOT_FACTORY.get())) {
            int offset = state.getValue(StorageBlock.HALF).equals(DoubleBlockHalf.LOWER) ? 0: 1;
            BlockPos blockPos = event.getPos().below(offset);
            Vec3 pos = Vec3.atBottomCenterOf(blockPos);
            if(!(event.getLevel().getBlockEntity(blockPos) instanceof FactoryBlockEntity factory)) return;
            factory.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(inv -> {
                if(inv instanceof IItemHandlerModifiable inventory) {
                    for(int i = 0; i < inventory.getSlots(); i++) {
                        InventoryUtil.dropItem(factory.getLevel(), pos.x, pos.y, pos.z, inventory.getStackInSlot(i));
                        inventory.setStackInSlot(i, ItemStack.EMPTY);
                    }
                }

            });
        }
    }
}
