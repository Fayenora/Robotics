package com.ignis.igrobotics.common;

import com.ignis.igrobotics.Robotics;
import com.ignis.igrobotics.core.access.WorldAccessData;
import com.ignis.igrobotics.core.capabilities.ModCapabilities;
import com.ignis.igrobotics.core.robot.RobotView;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class WorldData extends SavedData {

    public static final String DATA_NAME = Robotics.MODID + "_data";

    public static final String KEY_COMMAND_MODULE_COUNT = "command_module_count";
    public static final String KEY_ACCESS_CONFIG = "access_config";
    public static final String KEY_GROUPS = "views";

    private int numberOfCommandGroups = 1; //commandGroup = 0 means no command group
    private final WorldAccessData accessData = new WorldAccessData();
    private final HashMap<Integer, HashMap<UUID, RobotView>> commandGroups = new HashMap<>();

    @Override
    public @NotNull CompoundTag save(CompoundTag tag) {
        CompoundTag tagCommandGroups = new CompoundTag();
        for(Integer group : commandGroups.keySet()) {
            ListTag list = new ListTag();
            for(RobotView info : commandGroups.get(group).values()) {
                list.add(info.serializeNBT());
            }
            tagCommandGroups.put(String.valueOf(group), list);
        }
        tag.putInt(KEY_COMMAND_MODULE_COUNT, numberOfCommandGroups);
        tag.put(KEY_ACCESS_CONFIG, accessData.serializeNBT());
        tag.put(KEY_GROUPS, tagCommandGroups);
        return tag;
    }

    public static WorldData load(CompoundTag tag) {
        WorldData data = new WorldData();
        if(tag.contains(KEY_COMMAND_MODULE_COUNT)) {
            data.numberOfCommandGroups = tag.getInt(KEY_COMMAND_MODULE_COUNT);
        }
        if(tag.contains(KEY_ACCESS_CONFIG)) {
            data.accessData.deserializeNBT(tag.getCompound(KEY_ACCESS_CONFIG));
        }
        if(tag.contains(KEY_GROUPS)) {
            for(String key : tag.getCompound(KEY_GROUPS).getAllKeys()) {
                ListTag tagList = tag.getCompound(KEY_GROUPS).getList(key, Tag.TAG_COMPOUND);
                for(Tag listElement : tagList) {
                    if(listElement instanceof CompoundTag compound) {
                        data.cacheRobotForCommandGroup(Integer.parseInt(key), RobotView.deserialize(compound));
                    }
                }
            }
        }
        return data;
    }

    public void cacheRobotForCommandGroup(int commandGroup, Entity robot) {
        if(robot.level.isClientSide) return;
        cacheRobotForCommandGroup(commandGroup, new RobotView(robot));
    }

    private void cacheRobotForCommandGroup(int commandGroup, RobotView view) {
        if(!commandGroups.containsKey(commandGroup)) {
            commandGroups.put(commandGroup, new HashMap<>());
        }
        if(commandGroups.get(commandGroup).containsKey(view.getUUID())) return;
        commandGroups.get(commandGroup).put(view.getUUID(), view);
        setDirty();
    }

    public void releaseRobotFromCommandGroup(Entity robot) {
        robot.getCapability(ModCapabilities.ROBOT).ifPresent(robotCap -> {
            if(commandGroups.containsKey(robotCap.getCommandGroup())) {
                commandGroups.get(robotCap.getCommandGroup()).remove(robot.getUUID());
            }
        });
        setDirty();
    }

    public void rememberRobotStorage(BlockPos pos, Entity ent) {
        ent.getCapability(ModCapabilities.ROBOT).ifPresent(robot -> {
            if(robot.getCommandGroup() == 0) return;
            RobotView view = commandGroups.get(robot.getCommandGroup()).get(ent.getUUID());
            view.updatePosition(RobotView.RobotState.IN_STORAGE, ent.level, pos);
        });
    }

    public void forgetRobotStorage(Entity ent) {
        ent.getCapability(ModCapabilities.ROBOT).ifPresent(robot -> {
            if(robot.getCommandGroup() == 0) return;
            RobotView view = commandGroups.get(robot.getCommandGroup()).get(ent.getUUID());
            view.updatePosition(RobotView.RobotState.IN_WORLD, ent.level, ent.blockPosition());
        });
    }

    public Collection<RobotView> getRobotsOfCommandGroup(int commandGroup) {
        if(!commandGroups.containsKey(commandGroup)) {
            return List.of();
        }
        return commandGroups.get(commandGroup).values();
    }

    public WorldAccessData getAccessConfigData() {
        return accessData;
    }

    public int nextCommandGroupId() {
        setDirty();
        return numberOfCommandGroups++;
    }

    public static WorldData get() {
        if(ServerLifecycleHooks.getCurrentServer() == null) return new WorldData();
        return ServerLifecycleHooks.getCurrentServer().overworld().getDataStorage().computeIfAbsent(WorldData::load, WorldData::new, DATA_NAME);
    }
}
