package com.ignis.igrobotics.core.robot;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class RobotCommand {

    private final CommandType type;
    protected List<Selection<?>> selectors = new ArrayList<>();

    public RobotCommand(CommandType commandType) {
        this.type = commandType;
        for(SelectionType<?> type : commandType.getSelectionTypes()) {
            selectors.add(new Selection<>(type));
        }
    }

    private RobotCommand(CommandType command_type, List<Selection<?>> selections) {
        this(command_type);
        for(int i = 0; i < selections.size(); i++) {
            selectors.set(i, selections.get(i).clone());
        }
    }

    @Nullable
    public Goal getGoal(Mob robot) {
        return type.getGoal(selectors, robot);
    }

    public CommandType getType() {
        return type;
    }

    public List<Component> getDescription() {
        return type.getDescription();
    }

    public List<Selection<?>> getSelectors() {
        return selectors;
    }

    @Override
    public RobotCommand clone() {
        return new RobotCommand(type, selectors);
    }

    /*
     * Serialization
     */

    public static void writeToNBT(CompoundTag comp, List<RobotCommand> commands) {
        ListTag list = new ListTag();

        for(RobotCommand command : commands) {
            CompoundTag commandTag = new CompoundTag();

            //Type of command
            commandTag.putInt("id", command.type.getId());

            //Individual selections of the command
            ListTag selectorList = new ListTag();
            for(Selection<?> sel : command.getSelectors()) {
                selectorList.add(sel.serializeNBT());
            }
            commandTag.put("selectors", selectorList);

            list.add(commandTag);
        }

        comp.put("commands", list);
    }

    public static List<RobotCommand> readFromNBT(CompoundTag comp) {
        ListTag list = comp.getList("commands", Tag.TAG_COMPOUND);

        List<RobotCommand> commands = new ArrayList<>();

        for (Tag tag : list) {
            CompoundTag com = (CompoundTag) tag;

            //Type of command
            int id = com.getInt("id");

            //Individual selections of the command
            ListTag selectorList = com.getList("selectors", Tag.TAG_COMPOUND);
            ArrayList<Selection<?>> selections = new ArrayList<>();
            for(Tag value : selectorList) {
                selections.add(new Selection<>((CompoundTag) value));
            }

            //Instantiate command
            commands.add(new RobotCommand(CommandType.byId(id), selections));
        }

        return commands;
    }
}
