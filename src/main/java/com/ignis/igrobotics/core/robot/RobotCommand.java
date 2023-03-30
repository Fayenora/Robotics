package com.ignis.igrobotics.core.robot;

import com.ignis.igrobotics.common.entity.RobotEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class RobotCommand {

    /** number of free priority values lower(=higher priority) than the ones selected by the player */
    public static final int COMMANDS_BELOW_CUSTOM = 10;

    private CommandType type;
    protected List<Selection> selectors = new ArrayList<>();

    public RobotCommand(CommandType commandType) {
        this.type = commandType;
        for(SelectionType type : commandType.getSelectionTypes()) {
            selectors.add(new Selection(type));
        }
    }

    private RobotCommand(CommandType command_type, List<Selection> selections) {
        this(command_type);
        for(int i = 0; i < selections.size(); i++) {
            selectors.set(i, selections.get(i).clone());
        }
    }

    public void applyToEntity(RobotEntity robot, int priority) {
        type.applyToEntity(selectors, robot, priority + COMMANDS_BELOW_CUSTOM);
    }

    public int getId() {
        return type.getId();
    }

    public List<Component> getDescription() {
        return type.getDescription();
    }

    public List<Selection> getSelectors() {
        return selectors;
    }

    @Override
    public RobotCommand clone() {
        return new RobotCommand(type, selectors);
    }

    /*
     * Serialization
     */

    public static void writeToNBT(CompoundTag comp, RobotCommand[] commands) {
        ListTag list = new ListTag();

        for(RobotCommand command : commands) {
            CompoundTag commandTag = new CompoundTag();

            //Type of command
            commandTag.putInt("id", command.getId());

            //Individual selections of the command
            ListTag selectorList = new ListTag();
            for(Selection sel : command.getSelectors()) {
                selectorList.add(sel.serializeNBT());
            }
            commandTag.put("selectors", selectorList);

            list.add(commandTag);
        }

        comp.put("commands", list);
    }

    public static RobotCommand[] readFromNBT(CompoundTag comp) {
        ListTag list = comp.getList("commands", Tag.TAG_COMPOUND);

        RobotCommand[] commands = new RobotCommand[list.size()];

        for(int i = 0; i < list.size(); i++) {
            CompoundTag com = (CompoundTag) list.get(i);

            //Type of command
            int id = com.getInt("id");

            //Individual selections of the command
            ListTag selectorList = com.getList("selectors", Tag.TAG_COMPOUND);
            ArrayList<Selection> selections = new ArrayList<Selection>();
            for(int j = 0; j < selectorList.size(); j++) {
                selections.add(new Selection((CompoundTag) selectorList.get(j)));
            }

            //Instantiate command
            commands[i] = new RobotCommand(CommandType.byId(id), selections);
        }

        return commands;
    }
}
