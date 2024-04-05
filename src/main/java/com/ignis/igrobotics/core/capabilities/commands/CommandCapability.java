package com.ignis.igrobotics.core.capabilities.commands;

import com.ignis.igrobotics.common.handlers.CommandBehavior;
import com.ignis.igrobotics.core.EntitySearch;
import com.ignis.igrobotics.core.capabilities.ModCapabilities;
import com.ignis.igrobotics.core.capabilities.robot.IRobot;
import com.ignis.igrobotics.core.robot.RobotCommand;
import com.ignis.igrobotics.core.robot.Selection;
import com.ignis.igrobotics.core.robot.SelectionType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class CommandCapability implements ICommandable {

    public static final int MAX_NON_COMMAND_GOALS = 10;
    public static final int MAX_COMMANDS = 50;

    protected final Mob entity;
    /** Contains all active commands in order. Additionally, maps them to the goal they are currently contributing to the entity */
    private final LinkedHashMap<RobotCommand, Goal> commands = new LinkedHashMap<>();

    public CommandCapability(Mob entity) {
        this.entity = entity;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        RobotCommand.writeToNBT(nbt, commands.keySet());
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        setCommands(RobotCommand.readFromNBT(nbt));
    }

    /**
     * Clear all commands and goals provided by them
     */
    protected void clearCommands() {
        if(!entity.level.isClientSide()) {
            //If commands are changed in any way, the entity should always reconsider what to do next
            entity.goalSelector.getRunningGoals().forEach(WrappedGoal::stop);
            for(Map.Entry<RobotCommand, Goal> entry : commands.entrySet()) {
                if(entry.getValue() == null) continue;
                entity.goalSelector.removeGoal(entry.getValue());
                onRemoved(entry.getKey());
            }
        }
        commands.clear();
    }

    /**
     * Add goals provided by currently selected commands to the entity, making the entity perform them.
     * Does not re-evaluate the commands.
     */
    protected void applyCommands() {
        if(entity.level.isClientSide()) return;
        Optional<IRobot> robot = entity.getCapability(ModCapabilities.ROBOT).resolve();
        if(robot.isPresent() && !robot.get().isActive()) return;
        int i = 0;
        for(Goal goal : commands.values()) {
            if(goal == null) continue;
            int priority = MAX_NON_COMMAND_GOALS + i++;
            entity.goalSelector.addGoal(priority, goal);
        }
    }

    /**
     * Removes the goal currently provided by given command. Reevaluates the command and applies the goal it provides
     * @param command the command to reapply
     */
    @Override
    public void reapplyCommand(RobotCommand command) {
        if(entity.level.isClientSide()) return;
        if(!commands.containsKey(command)) return;
        //Stop
        Goal goal = commands.get(command);
        if(goal != null) {
            entity.goalSelector.getRunningGoals().filter(wrappedGoal -> goal.equals(wrappedGoal.getGoal())).forEach(WrappedGoal::stop);
            entity.goalSelector.removeAllGoals(goal::equals);
        }

        //Reapply
        Goal reEvaluatedGoal = command.getGoal(entity);
        int priority = MAX_NON_COMMAND_GOALS + commands.keySet().stream().toList().indexOf(command);
        if(reEvaluatedGoal == null) return;
        entity.goalSelector.addGoal(priority, reEvaluatedGoal);
    }

    /**
     * Sets and evaluates the commands
     * @param commands to set
     */
    @Override
    public void setCommands(Collection<RobotCommand> commands) {
        clearCommands();
        for(RobotCommand command : commands) {
            this.commands.put(command, command.getGoal(entity));
            onApplied(command);
        }
        applyCommands();
    }

    @Override
    public void removeCommand(RobotCommand command) {
        Goal goal = commands.get(command);
        if(goal == null) return;
        //If this goal is currently running, stop it
        entity.goalSelector.getRunningGoals().filter(wrappedGoal -> goal.equals(wrappedGoal.getGoal())).forEach(WrappedGoal::stop);
        entity.goalSelector.removeAllGoals(goal::equals);
        commands.remove(command);
        onRemoved(command);
    }

    @Override
    public Collection<RobotCommand> getCommands() {
        return commands.keySet();
    }

    @Override
    public void removeAllTasks() {
        entity.goalSelector.getRunningGoals().forEach(WrappedGoal::stop);
        entity.targetSelector.getRunningGoals().forEach(WrappedGoal::stop);
        for(Goal.Flag flag : Goal.Flag.values()) {
            entity.goalSelector.disableControlFlag(flag);
            entity.targetSelector.disableControlFlag(flag);
        }
    }

    @Override
    public void reapplyAllTasks() {
        for(Goal.Flag flag : Goal.Flag.values()) {
            entity.goalSelector.enableControlFlag(flag);
            entity.targetSelector.enableControlFlag(flag);
        }
    }

    private void onApplied(RobotCommand command) {
        if(entity.level.isClientSide) return;
        // Register the Entity Predicates of this command to the CommandBehavior
        for(Selection<?> selector : command.getSelectors()) {
            if(selector.getType().equals(SelectionType.ENTITY_PREDICATE)) {
                EntitySearch search = (EntitySearch) selector.get();
                search.addListener(newResult -> {
                    search.setCache(newResult);
                    reapplyCommand(command);
                });
                CommandBehavior.SEARCHES.put(entity.getLevel(), search);
            }
        }
    }

    private void onRemoved(RobotCommand command) {
        if(entity.level.isClientSide) return;
        for(Selection<?> selector : command.getSelectors()) {
            if(selector.getType().equals(SelectionType.ENTITY_PREDICATE)) {
                EntitySearch search = (EntitySearch) selector.get();
                CommandBehavior.SEARCHES.remove(entity.getLevel(), search);
            }
        }
    }
}
