package com.ignis.igrobotics.common;

import com.ignis.igrobotics.Robotics;
import com.ignis.igrobotics.core.access.WorldAccessData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.server.ServerLifecycleHooks;

public class WorldData extends SavedData {

    public static final String DATA_NAME = Robotics.MODID + "_data";

    public static final String KEY_COMMAND_MODULE_COUNT = "command_module_count";
    public static final String KEY_ACCESS_CONFIG = "access_config";

    private int numberOfCommandGroups = 0;
    private WorldAccessData accessData = new WorldAccessData();

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putInt(KEY_COMMAND_MODULE_COUNT, numberOfCommandGroups);
        tag.put(KEY_ACCESS_CONFIG, accessData.serializeNBT());
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
        return data;
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
