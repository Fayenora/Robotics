package com.ignis.igrobotics.core.robot;

import com.ignis.igrobotics.core.capabilities.ModCapabilities;
import com.ignis.igrobotics.core.capabilities.commands.CommandApplyException;
import com.ignis.igrobotics.core.util.Lang;
import com.ignis.igrobotics.definitions.ModCommands;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public class CommandType {

    private final String name;
    private final List<SelectionType<?>> selectionTypes = new ArrayList<>();
    private BiFunction<Mob, Selection<?>[], Goal> applyToEntity;

    public CommandType(String name, Class<?>... selectionClasses) {
        this.name = name;
        for(Class<?> clazz : selectionClasses) {
            SelectionType<?> type = SelectionType.byClass(clazz);
            if(type != null) {
                this.selectionTypes.add(type);
            }
        }
    }

    public void setAISupplier(BiFunction<Mob, Selection<?>[], Goal> applyToEntity) {
        this.applyToEntity = applyToEntity;
    }

    @Nullable
    public Goal getGoal(List<Selection<?>> selectors, Mob robot) {
        if(robot.level().isClientSide()) return null;
        try {
            return applyToEntity.apply(robot, selectors.toArray(new Selection[0]));
        } catch(CommandApplyException e) {
            robot.getCapability(ModCapabilities.ROBOT).ifPresent(r -> {
                Player player = robot.level().getPlayerByUUID(r.getOwner());
                if(player == null) return;
                MutableComponent msg = Component.literal(e.getMessage());
                msg.setStyle(msg.getStyle().withColor(TextColor.fromLegacyFormat(ChatFormatting.RED)));
                player.sendSystemMessage(msg);
            });
            return null;
        }
    }

    public static CommandType byId(int id) {
        return ModCommands.byId(id);
    }

    public int getId() {
        return ModCommands.getId(this);
    }

    public String getName() {
        return name;
    }

    public List<Component> getDescription() {
        if(selectionTypes.size() <= 1) return List.of(Lang.localise("command." + name));
        List<Component> description = new ArrayList<>();
        for(int i = 0; i < selectionTypes.size(); i++) {
            description.add(Lang.localise("command." + getName() + "." + i));
        }
        return description;
    }

    public List<SelectionType<?>> getSelectionTypes() {
        return selectionTypes;
    }

    @Override
    public String toString() {
        return name;
    }
}
