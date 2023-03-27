package com.ignis.igrobotics.core.capabilities.energy;

import net.minecraftforge.energy.IEnergyStorage;

public interface ModifiableEnergyStorage extends IEnergyStorage {

    void setEnergy(int value);

    void setMaxEnergyStored(int maxEnergyStored);
}
