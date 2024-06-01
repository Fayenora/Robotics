package com.ignis.igrobotics.core.robot;

import com.ignis.igrobotics.Robotics;
import com.ignis.igrobotics.client.screen.selectors.*;
import com.ignis.igrobotics.core.EntitySearch;
import com.ignis.igrobotics.core.util.PosUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class SelectionType<T> {

    public static final List<SelectionType<?>> TYPES = new ArrayList<>();

    // TODO: This type of abstraction may have made sense when selection types did not have that much responsibility.
    //  SelectionTypes should now probably be implemented in subclasses, as this is just ugly to look at+
    //  Move to a registry!
    public static final SelectionType<ItemStack> ITEM = register("<Item>", ItemStack.class, Items.IRON_SWORD::getDefaultInstance, ItemStack::serializeNBT, ItemStack::of, string -> new ItemStack(ForgeRegistries.ITEMS.getValue(ResourceLocation.tryParse(string))), stack -> ForgeRegistries.ITEMS.getKey(stack.getItem()).toString());
    public static final SelectionType<Block> BLOCK = register("<Block>", Block.class, () -> Blocks.COBBLESTONE, null, null, string -> ForgeRegistries.BLOCKS.getValue(ResourceLocation.tryParse(string)), block -> ForgeRegistries.BLOCKS.getKey(block).toString());
    public static final SelectionType<GlobalPos> POS = register("<Pos>", GlobalPos.class, () -> GlobalPos.of(ServerLifecycleHooks.getCurrentServer().overworld().dimension(), BlockPos.ZERO), PosUtil::writePos, PosUtil::readPos, PosUtil::parseBlockPos, GlobalPos::toString);
    public static final SelectionType<EntityType> ENTITY_TYPE = register("<Entity-Type>", EntityType.class, () -> EntityType.CREEPER, type -> {
        CompoundTag tag = new CompoundTag();
        tag.putString("value", ForgeRegistries.ENTITY_TYPES.getKey(type).toString());
        return tag;
    }, tag -> ForgeRegistries.ENTITY_TYPES.getValue(ResourceLocation.tryParse(tag.getString("value"))),
       string -> ForgeRegistries.ENTITY_TYPES.getValue(ResourceLocation.tryParse(string)), type -> ForgeRegistries.ENTITY_TYPES.getKey(type).toString());
    public static final SelectionType<Integer> INTEGER = register("<Int>", Integer.class, () -> 0, number -> {
        CompoundTag tag = new CompoundTag();
        tag.putInt("value", number);
        return tag;
    }, tag -> tag.getInt("value"), string -> Integer.valueOf(Arrays.stream(string.split("\\D")).filter(s -> s.length() > 0).findFirst().get()), Object::toString);
    public static final SelectionType<EntitySearch> ENTITY_PREDICATE = register("<Entity-Predicate>", EntitySearch.class, EntitySearch::searchForNone, EntitySearch::serializeNBT, EntitySearch::of, EntitySearch::new, EntitySearch::toString);

    private final String identifier;
    private final Class<T> type;
    private final Supplier<T> defaultsTo;
    private final Function<T, CompoundTag> writer;
    private final Function<CompoundTag, T> reader;
    private final Function<String, T> parser;
    private final Function<T, String> toString;
    @OnlyIn(Dist.CLIENT)
    private Class<?> gui;

    public static void loadGuis() {
        ITEM.gui = ItemSelector.class;
        POS.gui = PosSelector.class;
        ENTITY_TYPE.gui = EntityTypeSelector.class;
        INTEGER.gui = IntSelector.class;
        ENTITY_PREDICATE.gui = EntitySearchSelector.class;
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

    private SelectionType(String identifier, Class<T> type, Supplier<T> defaultsTo, Function<T, CompoundTag> writer, Function<CompoundTag, T> reader, Function<String, T> parser, Function<T, String> toString) {
        this.identifier = identifier;
        this.type = type;
        this.defaultsTo = defaultsTo;
        this.writer = writer;
        this.reader = reader;
        this.parser = parser;
        this.toString = toString;
    }

    public static <T> SelectionType<T> register(String identifier, Class<T> type, Supplier<T> defaultsTo, Function<T, CompoundTag> writer, Function<CompoundTag, T> reader, Function<String, T> parser, Function<T, String> toString) {
        SelectionType<T> selectionType = new SelectionType<>(identifier, type, defaultsTo, writer, reader, parser, toString);
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

    public Function<T, String> stringifier() {
        return toString;
    }

    public T parse(String string) {
        return parser.apply(string);
    }

    public String toString(T selection) {
        return toString.apply(selection);
    }

    @Override
    public String toString() {
        return identifier.substring(1, identifier.length() - 1);
    }
}
