package com.ignis.igrobotics.integration.cc;

import com.ignis.igrobotics.core.capabilities.robot.IRobot;
import dan200.computercraft.shared.computer.core.ServerComputer;

public class ComputerizedBehavior {

    public static void onComputerTick(IRobot robot, ServerComputer computer) {
        if(robot.isActive() && !computer.isOn()) {
            computer.turnOn();
        }
        if(!robot.isActive()) {
            computer.shutdown();
        } else {
            computer.keepAlive();
        }
        //TODO if (computer.hasOutputChanged()) Update redstone output
    }
}
