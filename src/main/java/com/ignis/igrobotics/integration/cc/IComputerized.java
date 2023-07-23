package com.ignis.igrobotics.integration.cc;

import dan200.computercraft.shared.computer.core.ServerComputer;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;
import net.minecraftforge.items.IItemHandler;

@AutoRegisterCapability
public interface IComputerized {

    boolean hasComputer();

    ServerComputer getComputer();

    IItemHandler getPeripherals();
}
