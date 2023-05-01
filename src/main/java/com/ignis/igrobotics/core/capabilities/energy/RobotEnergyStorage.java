package com.ignis.igrobotics.core.capabilities.energy;

import com.ignis.igrobotics.definitions.ModAttributes;

public class RobotEnergyStorage extends EnergyStorage {

    public RobotEnergyStorage() {
        super((int) ModAttributes.ENERGY_CAPACITY.getDefaultValue());
    }
}
