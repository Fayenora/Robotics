package com.ignis.igrobotics.network.container;

public interface ISyncableData {

    boolean isDirty();

    PropertyData getPropertyData(short property);
}
