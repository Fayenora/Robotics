package com.io.norabotics.common.helpers.util;

import au.edu.federation.utils.Vec3f;
import com.io.norabotics.common.robot.EnumRobotMaterial;
import com.io.norabotics.common.robot.EnumRobotPart;
import com.io.norabotics.common.robot.RobotPart;
import net.minecraft.core.Registry;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;

public class NBTUtil {

    public static ListTag serializeVec(Vec3f vec) {
        ListTag vecTag = new ListTag();
        vecTag.add(FloatTag.valueOf(vec.x));
        vecTag.add(FloatTag.valueOf(vec.y));
        vecTag.add(FloatTag.valueOf(vec.z));
        return vecTag;
    }

    public static Vec3f deserializeVec(ListTag tag) {
        return new Vec3f(tag.getFloat(0), tag.getFloat(1), tag.getFloat(2));
    }

    public static <T> Tag serializeEntry(IForgeRegistry<T> registry, T value) {
        ResourceLocation key = registry.getKey(value);
        if(key == null) return StringTag.valueOf("");
        return StringTag.valueOf(key.toString());
    }

    public static <T> T deserializeEntry(IForgeRegistry<T> registry, Tag tag) {
        if(!(tag instanceof StringTag stringTag)) return null;
        return registry.getValue(ResourceLocation.tryParse(stringTag.getAsString()));
    }

    public static Tag serializeKey(ResourceKey<?> key) {
        return StringTag.valueOf(key.location().toString());
    }

    public static <T> ResourceKey<T> deserializeKey(ResourceKey<? extends Registry<T>> registry, Tag tag) {
        if(!(tag instanceof StringTag stringTag)) return null;
        ResourceLocation loc = ResourceLocation.tryParse(stringTag.getAsString());
        if(loc == null) return null;
        return ResourceKey.create(registry, loc);
    }
}
