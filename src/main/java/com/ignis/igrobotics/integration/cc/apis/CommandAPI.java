package com.ignis.igrobotics.integration.cc.apis;

import com.ignis.igrobotics.core.capabilities.commands.ICommandable;
import com.ignis.igrobotics.core.robot.CommandType;
import com.ignis.igrobotics.core.robot.RobotCommand;
import com.ignis.igrobotics.core.robot.Selection;
import com.ignis.igrobotics.core.robot.SelectionType;
import com.ignis.igrobotics.definitions.ModCommands;
import dan200.computercraft.api.lua.ILuaAPI;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.core.apis.IAPIEnvironment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class CommandAPI implements ILuaAPI {

    private final IAPIEnvironment environment;
    private final ICommandable commands;

    public CommandAPI(IAPIEnvironment environment, ICommandable commands) {
        this.environment = environment;
        this.commands = commands;
    }

    @LuaFunction
    public final LuaRobotCommand[] get() {
        return commands.getCommands().stream().map(LuaRobotCommand::new).toList().toArray(new LuaRobotCommand[0]);
    }

    @LuaFunction
    public final LuaRobotCommand remove(int index) {
        RobotCommand command = commands.getCommands().toArray(new RobotCommand[0])[index + 1];
        commands.removeCommand(command);
        return new LuaRobotCommand(command);
    }

    @LuaFunction
    public final void add(String type, Optional<String> sel1, Optional<String> sel2, Optional<String> sel3, Optional<String> sel4) throws LuaException {
        List<String> selections = new ArrayList<>();
        sel1.ifPresent(selections::add);
        sel2.ifPresent(selections::add);
        sel3.ifPresent(selections::add);
        sel4.ifPresent(selections::add);
        CommandType commandType = ModCommands.byName(type);
        if(commandType == null) {
            throw new LuaException(type + " is not a valid command type. See getAvailableCommands for a list of viable commands");
        }

        RobotCommand command = new RobotCommand(commandType);
        List<SelectionType<?>> selectionTypes = commandType.getSelectionTypes();
        for(int i = 0; i < selectionTypes.size(); i++) {
            Selection<?> selection = new Selection<>(selectionTypes.get(i));
            if(i < selections.size()) {
                Object obj = selectionTypes.get(i).parse(selections.get(i));
                if(obj == null) {
                    throw new LuaException("Could not parse " + selections.get(i) + " as " + selectionTypes.get(i).toString());
                }
                selection = new Selection<>(obj);
            }
            command.getSelectors().set(i, selection);
        }

        Collection<RobotCommand> currentCommands = new ArrayList<>(commands.getCommands());
        currentCommands.add(command);
        commands.setCommands(currentCommands);
    }

    @Override
    public String[] getNames() {
        return new String[] {"robot_commands"};
    }
}
