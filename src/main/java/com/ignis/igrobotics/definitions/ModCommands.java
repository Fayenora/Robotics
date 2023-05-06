package com.ignis.igrobotics.definitions;

import com.ignis.igrobotics.common.CommonSetup;
import com.ignis.igrobotics.common.entity.ai.*;
import com.ignis.igrobotics.core.robot.CommandType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ModCommands {

    public static final List<CommandType> COMMAND_TYPES = new ArrayList<>();

    public static final CommandType STAY = register("stay", BlockPos.class);
    public static final CommandType GO = register("go", BlockPos.class);
    public static final CommandType ATTACK = register("attack", EntityType.class);
    public static final CommandType ATTACK_SPECIFIC = register("attack_specific", UUID.class);
    public static final CommandType DEFEND = register("defend", UUID.class);
    public static final CommandType FOLLOW = register("follow", UUID.class, Integer.class);
    public static final CommandType RETRIEVE = register("retrieve", ItemStack.class, BlockPos.class);
    public static final CommandType STORE = register("store", ItemStack.class, BlockPos.class);
    public static final CommandType BREAK = register("break", BlockPos.class, BlockPos.class);

    static {
        STAY.setAISupplier((robot, selections) -> new MoveToBlockGoal(robot, (BlockPos) selections[0].get()));
        GO.setAISupplier((robot, selections) -> new QuickMoveToBlock(robot, (BlockPos) selections[0].get()));
        ATTACK.setAISupplier((robot, selections) -> new NearestAttackableTargetGoal(robot, CommonSetup.allLivingEntities.get(selections[0].get()).getClass()));
        ATTACK_SPECIFIC.setAISupplier((robot, selections) -> {
            if(!(robot.level instanceof ServerLevel server)) return null;
            Entity entity = server.getEntity((UUID) selections[0].get());
            if(!(entity instanceof LivingEntity)) return null;
            return new HuntGoal(robot, (LivingEntity) entity);
        });
        DEFEND.setAISupplier((robot, selections) -> {
            if(!(robot.level instanceof ServerLevel server)) return null;
            Entity entity = server.getEntity((UUID) selections[0].get());
            if(!(entity instanceof LivingEntity)) return null;
            return new DefendGoal(robot, (LivingEntity) entity, false);
        });
        FOLLOW.setAISupplier((robot, selections) -> {
            if(!(robot.level instanceof ServerLevel server)) return null;
            Entity entity = server.getEntity((UUID) selections[0].get());
            int range = (int) selections[1].get();
            if(!(entity instanceof LivingEntity)) return null;
            double followRange = robot.getAttributeValue(Attributes.FOLLOW_RANGE);
            return new FollowGoal(robot, entity, range, (float) followRange);
        });
        RETRIEVE.setAISupplier((robot, s) -> new RetrieveGoal(robot, (BlockPos) s[1].get(), (ItemStack) s[0].get(), 20, 200));
        STORE.setAISupplier((robot, s) -> new StoreGoal(robot, (BlockPos) s[1].get(), (ItemStack) s[0].get(), 20, 200));
        BREAK.setAISupplier((robot, s) -> new BreakBlocksGoal(robot, (BlockPos) s[0].get(), (BlockPos) s[1].get()));
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
}
