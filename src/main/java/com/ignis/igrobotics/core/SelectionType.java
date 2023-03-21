package com.ignis.igrobotics.core;

import com.ignis.igrobotics.Reference;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;

public record SelectionType<T>(String identifier, Class<T> type, T defaultsTo, Function<T, CompoundTag> writer, Function<CompoundTag, T> reader, Class gui) {

    public static final List<SelectionType> TYPES = new ArrayList<>();

    public static final SelectionType<LivingEntity> ENTITY = new SelectionType<>("<Entity>", LivingEntity.class, null, null, null, null);
    public static final SelectionType<ItemStack> ITEM = new SelectionType<>("<Item>", ItemStack.class, Items.IRON_SWORD.getDefaultInstance(), ItemStack::serializeNBT, ItemStack::of, null);
    public static final SelectionType<Block> BLOCK = new SelectionType<>("<Block>", Block.class, Blocks.COBBLESTONE, null, null, null);
    public static final SelectionType<BlockPos> POS = new SelectionType<>("<Pos>", BlockPos.class, BlockPos.ZERO, NbtUtils::writeBlockPos, NbtUtils::readBlockPos, null);
    public static final SelectionType<Integer> INTEGER = new SelectionType<>("<Int>", Integer.class, 0, number -> {
        CompoundTag tag = new CompoundTag();
        tag.putInt("value", number);
        return tag;
    }, (tag) -> tag.getInt("value"), null);
    public static final SelectionType<UUID> SPECIFIC_ENTITY = new SelectionType<>("<Specific-Entity>", UUID.class, Reference.DEFAULT_UUID, uuid -> {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("value", uuid);
        return tag;
    }, (tag) -> tag.getUUID("value"), null);

    @Nullable
    public static SelectionType byClass(Class clazz) {
        for(SelectionType type : TYPES) {
            if(type.type().isAssignableFrom(clazz)) {
                return type;
            }
        }
        return null;
    }

    @Nullable
    public static SelectionType byId(int id) {
        return TYPES.get(id);
    }
}
