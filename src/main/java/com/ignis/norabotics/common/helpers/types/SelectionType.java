package com.ignis.norabotics.common.helpers.types;

import com.ignis.norabotics.Robotics;
import com.ignis.norabotics.client.screen.selectors.SelectorElement;
import com.ignis.norabotics.definitions.robotics.ModSelectionTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class SelectionType<T> {

    private final Class<T> type;
    private final Supplier<T> defaultsTo;
    private final Function<T, CompoundTag> writer;
    private final Function<CompoundTag, T> reader;
    private final Function<String, T> parser;
    private final Function<T, String> toString;
    @OnlyIn(Dist.CLIENT)
    private Class<?> gui;

    @Nullable
    public static SelectionType byClass(Class<?> clazz) {
        for(SelectionType<?> type : ModSelectionTypes.TYPES) {
            if(type.type.isAssignableFrom(clazz)) {
                return type;
            }
        }
        return null;
    }

    public int getId() {
        return ModSelectionTypes.TYPES.indexOf(this);
    }

    @Nullable
    public static SelectionType<?> byId(int id) {
        return ModSelectionTypes.TYPES.get(id);
    }

    public SelectionType(Class<T> type, Supplier<T> defaultsTo, Function<T, CompoundTag> writer, Function<CompoundTag, T> reader, Function<String, T> parser, Function<T, String> toString) {
        this.type = type;
        this.defaultsTo = defaultsTo;
        this.writer = writer;
        this.reader = reader;
        this.parser = parser;
        this.toString = toString;
    }

    @OnlyIn(Dist.CLIENT)
    public static Optional<SelectorElement<?>> createSelectionGui(Selection<?> selection, int x, int y) {
        try {
            Constructor<?> constructor = selection.getType().gui.getConstructor(Selection.class, int.class, int.class);
            return Optional.of((SelectorElement<?>) constructor.newInstance(selection, x, y));
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | IllegalArgumentException |
                 InvocationTargetException | ClassCastException e) {
            Robotics.LOGGER.error("Something went wrong when adding selector of type " + selection.getType() + ". This shouldn't happen,  please report it to the mod author!");
            e.printStackTrace();
            return Optional.empty();
        }
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
        return type.getTypeName();
    }

    @OnlyIn(Dist.CLIENT)
    public void setGui(Class<?> gui) {
        this.gui = gui;
    }
}
