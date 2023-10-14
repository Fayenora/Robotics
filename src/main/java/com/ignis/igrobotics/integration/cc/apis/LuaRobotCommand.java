package com.ignis.igrobotics.integration.cc.apis;

import com.ignis.igrobotics.core.robot.RobotCommand;
import com.ignis.igrobotics.core.robot.Selection;
import com.ignis.igrobotics.core.robot.SelectionType;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;

public class LuaRobotCommand {

    private final RobotCommand command;

    public LuaRobotCommand(RobotCommand command) {
        this.command = command;
    }

    @LuaFunction
    public final String getType() {
        return command.getType().getName();
    }

    @LuaFunction
    public final String[] getSelections() {
        String[] selections = new String[command.getSelectors().size()];
        for(int i = 0; i < selections.length; i++) {
            selections[i] = command.getSelectors().get(i).toString();
        }
        return selections;
    }

    @LuaFunction
    public final void setSelection(int index, String selection) throws LuaException {
        if(index <= 0 || index > command.getSelectors().size()) {
            throw new LuaException("Index " + index + " out of bounds. Command only has " + command.getSelectors().size() + (command.getSelectors().size() == 1 ? " selector. " : " selectors. "));
        }
        Selection<?> s = command.getSelectors().get(index - 1);
        try {
            setSelection(s, selection);
        } catch(Exception e) {
            throw new LuaException("Unable to set value \"" + selection + "\" for type " + s.getType().identifier() + ". ");
        }
        //Don't reapply the command. We don't necessarily know whether it is still part of the active list
    }

    private <A> void setSelection(Selection<A> sel, String newValue) {
        SelectionType<A> type = sel.getType();
        sel.set(type.parse(newValue));
    }

    public RobotCommand getCommand() {
        return command;
    }
}
