package com.ignis.igrobotics.core.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class PosUtil {

    public static GlobalPos parseBlockPos(String string) {
        //Find and separate all consequent digits ( = numbers) in the string
        String resourceKey = string.split(" ")[0].split("\\[")[1].split("]")[0];
        ResourceLocation registry = ResourceLocation.tryParse(resourceKey.split("/")[0]);
        ResourceLocation location = ResourceLocation.tryParse(resourceKey.split("/")[1]);
        BlockPos pos = BlockPos.ZERO;
        Object[] list = Arrays.stream(string.split("\\D")).filter(Predicate.not(String::isBlank)).map(Integer::parseInt).toArray();
        if(list.length >= 3 && list[0] instanceof Integer && list[1] instanceof Integer && list[2] instanceof Integer) {
            pos = new BlockPos((Integer) list[0], (Integer) list[1], (Integer) list[2]);
        }
        Optional<ResourceKey<Level>> dim = getLevelKey(registry, location);
        return GlobalPos.of(dim.orElse(ServerLifecycleHooks.getCurrentServer().overworld().dimension()), pos);
    }

    public static Optional<ResourceKey<Level>> getLevelKey(ResourceLocation registry, ResourceLocation location) {
        return ServerLifecycleHooks.getCurrentServer().levelKeys().stream().filter(key -> key.registry().equals(registry) && key.location().equals(location)).findFirst();
    }

    public static GlobalPos readPos(CompoundTag tag) {
        ResourceKey<Level> dim = NBTUtil.deserializeKey(Registries.DIMENSION, tag.get("dim"));
        if(dim == null) {
            dim = ServerLifecycleHooks.getCurrentServer().overworld().dimension();
        }
        return GlobalPos.of(dim, NbtUtils.readBlockPos(tag));
    }

    public static CompoundTag writePos(GlobalPos pos) {
        CompoundTag tag = NbtUtils.writeBlockPos(pos.pos());
        tag.put("dim", NBTUtil.serializeKey(pos.dimension()));
        return tag;
    }

    public static Component prettyPrint(GlobalPos pos) {
        List<Component> components = List.of(prettyPrint(pos.dimension()), prettyPrint(pos.pos()));
        return ComponentUtils.formatList(components, Component.literal(" "));
    }

    public static Component prettyPrint(BlockPos pos) {
        return Component.literal(pos.getX() + " " + pos.getY() + " " + pos.getZ());
    }

    public static Component prettyPrint(ResourceKey<Level> dimension) {
        return Lang.localiseExisting(dimension.location().toString());
    }
}
