package com.ignis.igrobotics.core.capabilities.commands;

import com.ignis.igrobotics.core.capabilities.ModCapabilities;
import com.ignis.igrobotics.core.capabilities.robot.IRobot;
import com.ignis.igrobotics.core.robot.RobotCommand;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class CommandCapability implements ICommandable {

    public static final int MAX_NON_COMMAND_GOALS = 10;
    public static final int MAX_COMMANDS = 50;

    protected final Mob entity;
    /** The goal selector to use for commands */
    private List<RobotCommand> commands = new ArrayList<>();

    private List<RobotCommand> inactiveCommands;
    private Set<WrappedGoal> inactiveGoals;
    private Set<WrappedGoal> inactiveTargets;

    public CommandCapability(Mob entity) {
        this.entity = entity;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        RobotCommand.writeToNBT(nbt, commands);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        setCommands(RobotCommand.readFromNBT(nbt));
    }

    /**
     * Clear any goals provided by commands from the goals of the entity
     */
    protected void clearCommands() {
        if(entity.level.isClientSide()) return;
        //If commands are changed in any way, the entity should always reconsider what to do next
        entity.goalSelector.getRunningGoals().forEach(WrappedGoal::stop);
        for(RobotCommand command : commands) {
            entity.goalSelector.removeAllGoals(command.getGoal(entity)::equals);
        }
    }

    /**
     * Add goals provided by currently selected commands to the entity, making the entity perform them
     */
    protected void applyCommands() {
        if(entity.level.isClientSide()) return;
        int i = 0;
        for(RobotCommand command : commands) {
            Goal goal = command.getGoal(entity);
            int priority = MAX_NON_COMMAND_GOALS + i++;
            entity.goalSelector.addGoal(priority, goal);
        }
    }

    @Override
    public void setCommands(List<RobotCommand> commands) {
        clearCommands();
        this.commands = commands;
        Optional<IRobot> robot = entity.getCapability(ModCapabilities.ROBOT).resolve();
        if(robot.isPresent() && !robot.get().isActive()) return;
        applyCommands();
    }

    @Override
    public void removeCommand(RobotCommand command) {
        //If this command is currently running, stop it
        entity.goalSelector.getRunningGoals().filter(wrappedGoal -> command.getGoal(entity).equals(wrappedGoal.getGoal())).forEach(WrappedGoal::stop);
        entity.goalSelector.removeAllGoals(command.getGoal(entity)::equals);
        commands.remove(command);
    }

    @Override
    public List<RobotCommand> getCommands() {
        return commands;
    }

    @Override
    public void removeAllTasks() {
        inactiveGoals = entity.goalSelector.getAvailableGoals();
        inactiveTargets = entity.targetSelector.getAvailableGoals();
        entity.goalSelector.removeAllGoals(goal -> true);
        entity.targetSelector.removeAllGoals(goal -> true);
        for(Goal.Flag flag : Goal.Flag.values()) {
            entity.goalSelector.disableControlFlag(flag);
            entity.targetSelector.disableControlFlag(flag);
        }
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
        for(Goal.Flag flag : Goal.Flag.values()) {
            entity.goalSelector.enableControlFlag(flag);
            entity.targetSelector.enableControlFlag(flag);
        }
        commands = inactiveCommands;
    }
}
