package com.ignis.norabotics.integration.cc.apis;

import com.ignis.norabotics.common.capabilities.ICommandable;
import com.ignis.norabotics.common.helpers.types.Selection;
import com.ignis.norabotics.common.helpers.types.SelectionType;
import com.ignis.norabotics.common.robot.CommandType;
import com.ignis.norabotics.common.robot.RobotCommand;
import com.ignis.norabotics.definitions.ModCommands;
import com.ignis.norabotics.definitions.ModSelectionTypes;
import com.ignis.norabotics.integration.config.RoboticsConfig;
import dan200.computercraft.api.lua.ILuaAPI;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.core.apis.IAPIEnvironment;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
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
    public final int amount() {
        return commands.getCommands().size();
    }

    @LuaFunction
    public final LuaRobotCommand[] get() {
        return commands.getCommands().stream().map(LuaRobotCommand::new).toList().toArray(new LuaRobotCommand[0]);
    }

    @LuaFunction
    public final LuaRobotCommand remove(int index) throws LuaException {
        RobotCommand[] commands1 = commands.getCommands().toArray(new RobotCommand[0]);
        if(index <= 0 || index > commands1.length) {
            throw new LuaException("Index " + index + " out of bounds for length " + commands1.length + ". ");
        }
        RobotCommand command = commands1[index - 1];
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
        CommandType commandType = ModCommands.REGISTRY.get().getValue(new ResourceLocation(type));
        if(commandType == null) {
            throw new LuaException(type + " is not a valid command type. See getAvailableCommands for a list of viable commands");
        }

        RobotCommand command = new RobotCommand(commandType);
        List<SelectionType<?>> selectionTypes = commandType.getSelectionTypes();
        for(int i = 0; i < Math.min(selectionTypes.size(), selections.size()); i++) {
            String argument = selections.get(i);
            SelectionType<?> reqType = selectionTypes.get(i);
            if(reqType == ModSelectionTypes.POS) {
                if(argument.matches("\\[[A-z:]*/[A-z:]*]\\s-?+\\d*\\s-?+\\d*\\s-?+\\d*")) {
                    // Everything is fine
                } else if(argument.matches("\\[[A-z:]*]\\s-?+\\d*\\s-?+\\d*\\s-?+\\d*")) {
                    argument = "[minecraft:dimension/" + argument.split("\\[")[1].trim();
                } else if(argument.matches("\\s*-?+\\d*\\s-?+\\d*\\s-?+\\d*\\s*")) {
                    argument = "[minecraft:dimension/minecraft:overworld] " + argument.trim();
                } else throw new LuaException("Position argument does not match required format 'x y z' or '[dimension-id] x y z'");
            }
            Object obj = reqType.parse(argument);
            if(obj == null) {
                throw new LuaException("Could not parse " + selections.get(i) + " as " + selectionTypes.get(i).toString());
            }
            Selection<?> selection = Selection.of(obj);
            command.getSelectors().set(i, selection);
        }

        commands.addCommand(command);
    }

    @LuaFunction
    public final List<? extends String> getAvailableCommands() {
        return RoboticsConfig.general.availableCommands.get();
    }

    @Override
    public String[] getNames() {
        return new String[] {"robot_commands"};
    }
}
