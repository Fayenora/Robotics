package com.ignis.igrobotics.core.robot;

import com.ignis.igrobotics.core.util.NBTUtil;
import com.ignis.igrobotics.definitions.ModCommands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RobotCommand {

    private final CommandType type;
    protected List<Selection<?>> selectors = new ArrayList<>();

    public RobotCommand(CommandType commandType) {
        this.type = commandType;
        for(SelectionType<?> type : commandType.getSelectionTypes()) {
            selectors.add(Selection.ofType(type));
        }
    }

    public RobotCommand(CommandType command_type, List<Selection<?>> selections) {
        this(command_type);
        for(int i = 0; i < selections.size(); i++) {
            if(i <= command_type.getSelectionTypes().size() && !command_type.getSelectionTypes().get(i).equals(selections.get(i).getType())) {
                throw new IllegalArgumentException("This command type does not support supplied selections");
            }
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

    public static void writeToNBT(CompoundTag comp, Collection<RobotCommand> commands) {
        ListTag list = new ListTag();

        for(RobotCommand command : commands) {
            CompoundTag commandTag = new CompoundTag();
            writeToNBT(commandTag, command);
            list.add(commandTag);
        }

        comp.put("commands", list);
    }

    public static void writeToNBT(CompoundTag comp, RobotCommand command) {
        //Type of command
        comp.put("type", NBTUtil.serializeEntry(ModCommands.REGISTRY.get(), command.getType()));

        //Individual selections of the command
        ListTag selectorList = new ListTag();
        for(Selection<?> sel : command.getSelectors()) {
            selectorList.add(sel.serializeNBT());
        }
        comp.put("selectors", selectorList);
    }

    public static List<RobotCommand> readFromNBT(CompoundTag comp) {
        ListTag list = comp.getList("commands", Tag.TAG_COMPOUND);

        List<RobotCommand> commands = new ArrayList<>();

        for (Tag tag : list) {
            commands.add(readSingleCommandFromNBT((CompoundTag) tag));
        }

        return commands;
    }

    public static RobotCommand readSingleCommandFromNBT(CompoundTag comp) {
        //Type of command
        CommandType type = NBTUtil.deserializeEntry(ModCommands.REGISTRY.get(), comp.get("type"));

        //Individual selections of the command
        ListTag selectorList = comp.getList("selectors", Tag.TAG_COMPOUND);
        ArrayList<Selection<?>> selections = new ArrayList<>();
        for(Tag value : selectorList) {
            selections.add(Selection.read((CompoundTag) value));
        }

        return new RobotCommand(type, selections);
    }
}
