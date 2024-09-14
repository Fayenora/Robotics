package com.io.norabotics.network.container;

import com.io.norabotics.network.container.properties.BytePropertyData;
import com.io.norabotics.network.container.properties.IntPropertyData;
import com.io.norabotics.network.container.properties.ShortPropertyData;
import net.minecraft.network.FriendlyByteBuf;

import javax.annotation.Nullable;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Credits to Mekanism's author
 * @see <a href="https://github.com/mekanism/Mekanism/blob/1.20.6/src/main/java/mekanism/common/network/to_client/container/property/PropertyType.java">...</a>
 * @author Sara Freimer
 */
public enum PropertyType {

    BYTE(Byte.TYPE, (byte) 0, (getter, setter) -> SyncableByte.create(() -> (byte) getter.get(), setter::accept),
            (property, buffer) -> new BytePropertyData(property, buffer.readByte())),
    SHORT(Short.TYPE, (short) 0, (getter, setter) -> SyncableShort.create(() -> (short) getter.get(), setter::accept),
            (property, buffer) -> new ShortPropertyData(property, buffer.readShort())),
    INT(Integer.TYPE, 0, (getter, setter) -> SyncableInt.create(() -> (int) getter.get(), setter::accept),
            (property, buffer) -> new IntPropertyData(property, buffer.readVarInt()));

    private final Class<?> type;
    private final Object defaultValue;
    @Nullable
    private final BiFunction<Supplier<Object>, Consumer<Object>, ISyncableData> creatorFunction;
    private final BiFunction<Short, FriendlyByteBuf, PropertyData> dataCreatorFunction;

    private static final PropertyType[] VALUES = values();

    PropertyType(Class<?> type, Object defaultValue, @Nullable BiFunction<Supplier<Object>, Consumer<Object>, ISyncableData> creatorFunction,
                 BiFunction<Short, FriendlyByteBuf, PropertyData> dataCreatorFunction) {
        this.type = type;
        this.defaultValue = defaultValue;
        this.creatorFunction = creatorFunction;
        this.dataCreatorFunction = dataCreatorFunction;
    }

    public <T> T getDefault() {
        return (T) defaultValue;
    }

    public static PropertyType getFromType(Class<?> type) {
        for (PropertyType propertyType : VALUES) {
            if (type == propertyType.type) {
                return propertyType;
            }
        }

        return null;
    }

    public PropertyData createData(short property, FriendlyByteBuf buffer) {
        return dataCreatorFunction.apply(property, buffer);
    }

    public ISyncableData create(Supplier<Object> supplier, Consumer<Object> consumer) {
        if (creatorFunction == null) {
            throw new IllegalStateException(name() + " does not support annotation based syncing.");
        }
        return creatorFunction.apply(supplier, consumer);
    }
}
