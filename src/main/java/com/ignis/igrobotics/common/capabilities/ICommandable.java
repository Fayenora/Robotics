package com.ignis.igrobotics.common.capabilities;

import com.ignis.igrobotics.common.robot.RobotCommand;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;
import net.minecraftforge.common.util.INBTSerializable;
import java.util.List;

@AutoRegisterCapability
public interface ICommandable extends INBTSerializable<CompoundTag> {

    void setCommands(List<RobotCommand> commands);

    default void addCommand(RobotCommand command) {
        List<RobotCommand> currentCommands = getCommands();
        for(RobotCommand c : currentCommands) {
            if(command.equals(c)) return;
        }
        currentCommands.add(command);
        setCommands(currentCommands);
    }

    List<RobotCommand> getCommands();

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
