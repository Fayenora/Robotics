package com.ignis.igrobotics.core;

import com.ignis.igrobotics.Reference;
import com.ignis.igrobotics.common.entity.RobotEntity;
import com.ignis.igrobotics.core.util.Lang;
import com.ignis.igrobotics.core.util.Tuple;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public class CommandType {

    private static final ArrayList<CommandType> COMMAND_TYPES = new ArrayList<>();

    public static final CommandType ATTACK = new CommandType("attack", LivingEntity.class);

    static {
        ATTACK.setAISupplier((robot, selections) -> new NearestAttackableTargetGoal(robot, selections[0].get().getClass(), true));
    }

    private static int ID;
    private final int id;
    private final String name;
    private final List<SelectionType> selectionTypes = new ArrayList<>();
    private BiFunction<Mob, Selection[], Goal> applyToEntity;

    public CommandType(String name, Class... selectionClasses) {
        this.id = ID++;
        this.name = name;
        for(Class clazz : selectionClasses) {
            this.selectionTypes.add(SelectionType.byClass(clazz));
        }
    }

    public void setAISupplier(BiFunction<Mob, Selection[], Goal> applyToEntity) {
        this.applyToEntity = applyToEntity;
    }

    public void applyToEntity(List<Selection> selectors, RobotEntity robot, int priority) {
        if(robot.level == null || robot.level.isClientSide()) return;
        Goal goal = null;
        try {
            goal = applyToEntity.apply(robot, selectors.toArray(new Selection[selectors.size()]));
        } catch(CommandApplyException e) {
            /* TODO
            if(robot.getOwner().equals(Reference.DEFAULT_UUID)) return;
            Player player = robot.level.getPlayerByUUID(robot.getOwner());
            if(player == null) return;
            MutableComponent msg = Component.literal(e.getMessage());
            msg.setStyle(msg.getStyle().withColor(TextColor.fromLegacyFormat(ChatFormatting.RED)));
            player.sendSystemMessage(msg);
            */
            return;
        }

        robot.goalSelector.addGoal(priority, goal);
    }

    public static CommandType byId(int id) {
        return COMMAND_TYPES.get(id);
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<Component> getDescription() {
        if(selectionTypes.size() <= 1) return List.of(Lang.localise(name));
        List<Component> description = new ArrayList<>();
        for(int i = 0; i < selectionTypes.size(); i++) {
            description.add(Lang.localise(getName() + "." + i));
        }
        return description;
    }
}
