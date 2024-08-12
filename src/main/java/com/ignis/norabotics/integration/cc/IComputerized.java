package com.ignis.norabotics.integration.cc;

import dan200.computercraft.shared.computer.core.ServerComputer;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;

@AutoRegisterCapability
public interface IComputerized {

    boolean hasComputer();

    ServerComputer getComputer();
}
