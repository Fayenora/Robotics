package com.io.norabotics.network.container;

public interface ISyncableData {

    boolean isDirty();

    PropertyData getPropertyData(short property);
}
