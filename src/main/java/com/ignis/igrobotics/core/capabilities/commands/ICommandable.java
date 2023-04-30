package com.ignis.igrobotics.core.capabilities.commands;

import com.ignis.igrobotics.core.robot.RobotCommand;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.List;

@AutoRegisterCapability
public interface ICommandable extends INBTSerializable<CompoundTag> {

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
