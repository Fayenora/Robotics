package com.ignis.igrobotics.core.robot;

import com.ignis.igrobotics.Reference;
import com.ignis.igrobotics.Robotics;
import com.ignis.igrobotics.client.screen.selectors.*;
import com.ignis.igrobotics.network.proxy.ServerProxy;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

public class SelectionType<T> {

    public static final List<SelectionType<?>> TYPES = new ArrayList<>();

    public static final SelectionType<ItemStack> ITEM = register("<Item>", ItemStack.class, Items.IRON_SWORD::getDefaultInstance, ItemStack::serializeNBT, ItemStack::of);
    public static final SelectionType<Block> BLOCK = register("<Block>", Block.class, () -> Blocks.COBBLESTONE, null, null);
    public static final SelectionType<BlockPos> POS = register("<Pos>", BlockPos.class, () -> BlockPos.ZERO, NbtUtils::writeBlockPos, NbtUtils::readBlockPos);
    public static final SelectionType<EntityType> ENTITY_TYPE = register("<Entity-Type>", EntityType.class, () -> EntityType.CREEPER, type -> {
        CompoundTag tag = new CompoundTag();
        tag.putString("value", ForgeRegistries.ENTITY_TYPES.getKey(type).toString());
        return tag;
    }, tag -> ForgeRegistries.ENTITY_TYPES.getValue(ResourceLocation.tryParse(tag.getString("value"))));
    public static final SelectionType<Integer> INTEGER = register("<Int>", Integer.class, () -> 0, number -> {
        CompoundTag tag = new CompoundTag();
        tag.putInt("value", number);
        return tag;
    }, tag -> tag.getInt("value"));
    public static final SelectionType<UUID> ENTITY = register("<Entity>", UUID.class, () -> Reference.DEFAULT_UUID, uuid -> {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("value", uuid);
        return tag;
    }, tag -> tag.getUUID("value"));

    private final String identifier;
    private final Class<T> type;
    private final Supplier<T> defaultsTo;
    private final Function<T, CompoundTag> writer;
    private final Function<CompoundTag, T> reader;
    @OnlyIn(Dist.CLIENT)
    private Class<?> gui;

    public static void loadGuis() {
        ITEM.gui = ItemSelector.class;
        POS.gui = PosSelector.class;
        ENTITY_TYPE.gui = EntityTypeSelector.class;
        INTEGER.gui = IntSelector.class;
        ENTITY.gui = EntitySelector.class;
    }

    @Nullable
    public static SelectionType byClass(Class<?> clazz) {
        for(SelectionType<?> type : TYPES) {
            if(type.type.isAssignableFrom(clazz)) {
                return type;
            }
        }
        return null;
    }

    public int getId() {
        return TYPES.indexOf(this);
    }

    @Nullable
    public static SelectionType<?> byId(int id) {
        return TYPES.get(id);
    }

    private SelectionType(String identifier, Class<T> type, Supplier<T> defaultsTo, Function<T, CompoundTag> writer, Function<CompoundTag, T> reader) {
        this.identifier = identifier;
        this.type = type;
        this.defaultsTo = defaultsTo;
        this.writer = writer;
        this.reader = reader;
    }

    public static <T> SelectionType<T> register(String identifier, Class<T> type, Supplier<T> defaultsTo, Function<T, CompoundTag> writer, Function<CompoundTag, T> reader) {
        SelectionType<T> selectionType = new SelectionType<>(identifier, type, defaultsTo, writer, reader);
        TYPES.add(selectionType);
        return selectionType;
    }

    @OnlyIn(Dist.CLIENT)
    public static Optional<SelectorElement<?>> createSelectionGui(Selection<?> selection, int x, int y) {
        try {
            Constructor<?> constructor = selection.getType().gui.getConstructor(Selection.class, int.class, int.class);
            return Optional.of((SelectorElement<?>) constructor.newInstance(selection, x, y));
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | IllegalArgumentException |
                 InvocationTargetException | ClassCastException e) {
            Robotics.LOGGER.error("Something went wrong when adding selector of type " + selection.getType().identifier + ". This shouldn't happen,  please report it to the mod author!");
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public String identifier() {
        return identifier;
    }

    public Class<T> type() {
        return type;
    }

    public Supplier<T> defaultsTo() {
        return defaultsTo;
    }

    public Function<T, CompoundTag> writer() {
        return writer;
    }

    public Function<CompoundTag, T> reader() {
        return reader;
    }

}
