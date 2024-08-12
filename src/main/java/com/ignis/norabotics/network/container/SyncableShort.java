package com.ignis.norabotics.network.container;

import com.ignis.norabotics.network.container.properties.ShortPropertyData;

public abstract class SyncableShort implements ISyncableData {

    private short lastKnownValue;

    public abstract short get();

    public abstract void set(short value);

    @Override
    public boolean isDirty() {
        short oldValue = get();
        boolean dirty = oldValue != this.lastKnownValue;
        this.lastKnownValue = oldValue;
        return dirty;
    }

    @Override
    public ShortPropertyData getPropertyData(short property) {
        return new ShortPropertyData(property, get());
    }

    public static SyncableShort create(short[] shortArray, int idx) {
        return new SyncableShort() {
            @Override
            public short get() {
                return shortArray[idx];
            }

            @Override
            public void set(short value) {
                shortArray[idx] = value;
            }
        };
    }

    public static SyncableShort create(ShortSupplier getter, ShortConsumer setter) {
        return new SyncableShort() {

            @Override
            public short get() {
                return getter.getAsShort();
            }

            @Override
            public void set(short value) {
                setter.accept(value);
            }
        };
    }

    @FunctionalInterface
    public interface ShortSupplier {
        short getAsShort();
    }

    @FunctionalInterface
    public interface ShortConsumer {
        void accept(short value);
    }
}
