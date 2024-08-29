package com.ignis.norabotics.common.capabilities;

import net.minecraftforge.items.IItemHandlerModifiable;

public interface ModifiableInventory extends IItemHandlerModifiable {

    void setSize(int size);
}
