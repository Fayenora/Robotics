package com.ignis.igrobotics.core.capabilities.commands;

import com.ignis.igrobotics.core.capabilities.ModCapabilities;
import com.ignis.igrobotics.core.capabilities.robot.IRobot;
import com.ignis.igrobotics.core.robot.RobotCommand;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.WrappedGoal;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class CommandCapability implements ICommandable {

    public static final int MAX_NON_COMMAND_GOALS = 10;

    protected Mob entity;
    private List<RobotCommand> commands = new ArrayList<>();

    private List<RobotCommand> inactiveCommands;
    private Set<WrappedGoal> inactiveGoals;
    private Set<WrappedGoal> inactiveTargets;

    public CommandCapability(Mob entity) {
        this.entity = entity;
    }

    @Override
    public void writeToNBT(CompoundTag compound) {
        RobotCommand.writeToNBT(compound, commands);
    }

    @Override
    public void readFromNBT(CompoundTag compound) {
        setCommands(RobotCommand.readFromNBT(compound));
    }

    @Override
    public void applyCommands() {
        if(entity.level.isClientSide()) return;
        int i = 0;
        for(RobotCommand command : commands) {
            command.applyToEntity(entity, MAX_NON_COMMAND_GOALS + i++);
        }
    }

    @Override
    public void setCommands(List<RobotCommand> commands) {
        this.commands = commands;
        Optional<IRobot> robot = entity.getCapability(ModCapabilities.ROBOT).resolve();
        if(robot.isPresent() && !robot.get().isActive()) return;
        applyCommands();
    }

    @Override
    public List<RobotCommand> getCommands() {
        return commands;
    }

    @Override
    public void removeCommand(RobotCommand command) {
        commands.remove(command);
    }

    @Override
    public void removeAllTasks() {
        inactiveGoals = entity.goalSelector.getAvailableGoals();
        inactiveTargets = entity.targetSelector.getAvailableGoals();
        entity.goalSelector.removeAllGoals(goal -> true);
        entity.targetSelector.removeAllGoals(goal -> true);
        inactiveCommands = commands;
        commands = new ArrayList<>();
    }

    @Override
    public void reapplyAllTasks() {
        for(WrappedGoal goal : inactiveGoals) {
            entity.goalSelector.addGoal(goal.getPriority(), goal.getGoal());
        }
        for(WrappedGoal goal : inactiveTargets) {
            entity.targetSelector.addGoal(goal.getPriority(), goal.getGoal());
        }
        commands = inactiveCommands;
    }
}
