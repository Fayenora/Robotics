package com.ignis.norabotics.network.container;

public interface ISyncableData {

    boolean isDirty();

    PropertyData getPropertyData(short property);
}
