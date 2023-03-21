package com.ignis.igrobotics.core.capabilities.commands;

import com.ignis.igrobotics.core.INBTSerializer;
import com.ignis.igrobotics.core.robot.RobotCommand;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;

import java.util.List;

@AutoRegisterCapability
public interface ICommandable extends INBTSerializer {

    void applyCommands();

    void setCommands(List<RobotCommand> commands);

    List<RobotCommand> getCommands();

    void removeCommand(RobotCommand command);

    /**
     * Clear tasks applied by commands, as well as vanilla tasks
     */
    void removeAllTasks();

    /**
     * Reapply all tasks that have been cleared by {@link #removeAllTasks()}
     */
    void reapplyAllTasks();
}
