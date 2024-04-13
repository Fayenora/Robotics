package com.ignis.igrobotics.core.capabilities.commands;

import com.ignis.igrobotics.core.robot.RobotCommand;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.Collection;
import java.util.HashSet;

@AutoRegisterCapability
public interface ICommandable extends INBTSerializable<CompoundTag> {

    void setCommands(Collection<RobotCommand> commands);

    default void addCommand(RobotCommand command) {
        Collection<RobotCommand> currentCommands = new HashSet<>(getCommands());
        for(RobotCommand c : currentCommands) {
            if(command.equals(c)) return;
        }
        currentCommands.add(command);
        setCommands(currentCommands);
    }

    Collection<RobotCommand> getCommands();

    void reapplyCommand(RobotCommand command);

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
