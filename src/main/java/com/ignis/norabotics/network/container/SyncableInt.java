package com.ignis.norabotics.network.container;

import com.ignis.norabotics.network.container.properties.IntPropertyData;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public abstract class SyncableInt implements ISyncableData {

    private int lastKnownValue;

    public abstract int get();

    public abstract void set(int value);

    @Override
    public boolean isDirty() {
        int oldValue = get();
        boolean dirty = oldValue != this.lastKnownValue;
        this.lastKnownValue = oldValue;
        return dirty;
    }

    @Override
    public IntPropertyData getPropertyData(short property) {
        return new IntPropertyData(property, get());
    }

    public static SyncableInt create(int[] intArray, int idx) {
        return new SyncableInt() {
            @Override
            public int get() {
                return intArray[idx];
            }

            @Override
            public void set(int value) {
                intArray[idx] = value;
            }
        };
    }

    public static SyncableInt create(IntSupplier getter, IntConsumer setter) {
        return new SyncableInt() {

            @Override
            public int get() {
                return getter.getAsInt();
            }

            @Override
            public void set(int value) {
                setter.accept(value);
            }
        };
    }
}
