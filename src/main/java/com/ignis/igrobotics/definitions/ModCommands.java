package com.ignis.igrobotics.definitions;

import com.ignis.igrobotics.common.CommonSetup;
import com.ignis.igrobotics.common.entity.ai.*;
import com.ignis.igrobotics.core.EntitySearch;
import com.ignis.igrobotics.core.robot.CommandType;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ModCommands {

    public static final List<CommandType> COMMAND_TYPES = new ArrayList<>();

    public static final CommandType STAY = register("stay", GlobalPos.class);
    public static final CommandType GO = register("go", GlobalPos.class);
    public static final CommandType ATTACK = register("attack", EntityType.class);
    public static final CommandType ATTACK_SPECIFIC = register("attack_specific", EntitySearch.class);
    public static final CommandType DEFEND = register("defend", EntitySearch.class);
    public static final CommandType FOLLOW = register("follow", EntitySearch.class, Integer.class);
    public static final CommandType RETRIEVE = register("retrieve", ItemStack.class, GlobalPos.class);
    public static final CommandType STORE = register("store", ItemStack.class, GlobalPos.class);
    public static final CommandType BREAK = register("break", GlobalPos.class, GlobalPos.class);
    public static final CommandType PLACE = register("place", GlobalPos.class, GlobalPos.class);

    static {
        STAY.setAISupplier((robot, selections) -> new MoveToBlockGoal(robot, (GlobalPos) selections[0].get()));
        GO.setAISupplier((robot, selections) -> new QuickMoveToBlock(robot, (GlobalPos) selections[0].get()));
        ATTACK.setAISupplier((robot, selections) -> new NearestAttackableTargetGoal<>(robot, CommonSetup.allLivingEntities.get(selections[0].get()).getClass()));
        ATTACK_SPECIFIC.setAISupplier((robot, selections) -> {
            if(!(robot.level() instanceof ServerLevel server)) return null;
            EntitySearch search = (EntitySearch) selections[0].get();
            Entity entity = search.commence(server, robot.position());
            if(!(entity instanceof LivingEntity)) return null;
            return new SpecificTargetGoal(robot, (LivingEntity) entity);
        });
        DEFEND.setAISupplier((robot, selections) -> {
            if(!(robot.level() instanceof ServerLevel server)) return null;
            EntitySearch search = (EntitySearch) selections[0].get();
            Entity entity = search.commence(server, robot.position());
            if(!(entity instanceof LivingEntity)) return null;
            return new DefendGoal(robot, (LivingEntity) entity, false);
        });
        FOLLOW.setAISupplier((robot, selections) -> {
            if(!(robot.level() instanceof ServerLevel server)) return null;
            EntitySearch search = (EntitySearch) selections[0].get();
            int range = (int) selections[1].get();
            Entity entity = search.commence(server, robot.position());
            if(!(entity instanceof LivingEntity)) return null;
            double followRange = robot.getAttributeValue(Attributes.FOLLOW_RANGE);
            return new FollowGoal(robot, entity, range, (float) followRange);
        });
        RETRIEVE.setAISupplier((robot, s) -> new RetrieveGoal(robot, (GlobalPos) s[1].get(), (ItemStack) s[0].get(), 20, 400, 200));
        STORE.setAISupplier((robot, s) -> new StoreGoal(robot, (GlobalPos) s[1].get(), (ItemStack) s[0].get(), 20, 400, 200));
        BREAK.setAISupplier((robot, s) -> new BreakBlocksGoal(robot, (GlobalPos) s[0].get(), (GlobalPos) s[1].get()));
        PLACE.setAISupplier((robot, s) -> new PlaceBlocksGoal(robot, (GlobalPos) s[0].get(), (GlobalPos) s[1].get()));
    }

    public static CommandType register(String name, Class<?>... selectionClasses) {
        CommandType commandType = new CommandType(name, selectionClasses);
        COMMAND_TYPES.add(commandType);
        return commandType;
    }

    public static CommandType byId(int id) {
        return COMMAND_TYPES.get(id);
    }

    public static int getId(CommandType type) {
        return COMMAND_TYPES.indexOf(type);
    }

    public static CommandType byName(String name) {
        for(CommandType type : COMMAND_TYPES) {
            if(name.equalsIgnoreCase(type.getName())) {
                return type;
            }
        }
        return null;
    }
}
