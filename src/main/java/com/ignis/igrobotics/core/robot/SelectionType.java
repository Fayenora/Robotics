package com.ignis.igrobotics.core.robot;

import com.ignis.igrobotics.Reference;
import com.ignis.igrobotics.Robotics;
import com.ignis.igrobotics.client.screen.selectors.*;
import com.ignis.igrobotics.common.CommonSetup;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

public record SelectionType<T>(String identifier, Class<T> type, Supplier<T> defaultsTo, Function<T, CompoundTag> writer, Function<CompoundTag, T> reader, Class gui) {

    public static final List<SelectionType> TYPES = new ArrayList<>();

    public static final SelectionType<LivingEntity> ENTITY_TYPE = register("<Entity-Type>", LivingEntity.class, () -> CommonSetup.allLivingEntities.get(EntityType.CREEPER), LivingEntity::serializeNBT, null, EntityTypeSelector.class);
    public static final SelectionType<ItemStack> ITEM = register("<Item>", ItemStack.class, () -> Items.IRON_SWORD.getDefaultInstance(), ItemStack::serializeNBT, ItemStack::of, ItemSelector.class);
    public static final SelectionType<Block> BLOCK = register("<Block>", Block.class, () -> Blocks.COBBLESTONE, null, null, null);
    public static final SelectionType<BlockPos> POS = register("<Pos>", BlockPos.class, () -> BlockPos.ZERO, NbtUtils::writeBlockPos, NbtUtils::readBlockPos, PosSelector.class);
    public static final SelectionType<Integer> INTEGER = register("<Int>", Integer.class, () -> 0, number -> {
        CompoundTag tag = new CompoundTag();
        tag.putInt("value", number);
        return tag;
    }, (tag) -> tag.getInt("value"), IntSelector.class);
    public static final SelectionType<UUID> ENTITY = new SelectionType<>("<Entity>", UUID.class, () -> Reference.DEFAULT_UUID, uuid -> {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("value", uuid);
        return tag;
    }, (tag) -> tag.getUUID("value"), EntitySelector.class);

    public static Optional<SelectorElement<?>> createSelectionGui(Selection selection, int x, int y) {
        try {
            Constructor constructor = selection.getType().gui().getConstructor(Selection.class, int.class, int.class);
            return Optional.of((SelectorElement<?>) constructor.newInstance(selection, x, y));
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | IllegalArgumentException |
                 InvocationTargetException | ClassCastException e) {
            Robotics.LOGGER.warn("Something went wrong when adding selector of type " + selection.getType().identifier + ". This shouldn't happen,  please report it to the mod author!");
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public static <T> SelectionType<T> register(String identifier, Class<T> type, Supplier<T> defaultsTo, Function<T, CompoundTag> writer, Function<CompoundTag, T> reader, Class gui) {
        SelectionType<T> selectionType = new SelectionType<>(identifier, type, defaultsTo, writer, reader, gui);
        TYPES.add(selectionType);
        return selectionType;
    }

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
