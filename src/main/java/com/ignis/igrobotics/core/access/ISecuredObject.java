package com.ignis.igrobotics.core.access;

import java.util.UUID;

public interface ISecuredObject {

    int getGroup();
    UUID getOwner();
    AccessConfig getConfiguration();
    void setConfiguration(AccessConfig config);

}
