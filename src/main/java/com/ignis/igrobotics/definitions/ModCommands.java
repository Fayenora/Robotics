package com.ignis.igrobotics.definitions;

import com.ignis.igrobotics.Robotics;
import com.ignis.igrobotics.common.entity.ai.*;
import com.ignis.igrobotics.core.EntitySearch;
import com.ignis.igrobotics.core.robot.CommandType;
import com.mojang.datafixers.util.Function3;
import com.mojang.datafixers.util.Function4;
import com.mojang.datafixers.util.Function5;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings({"unchecked", "unused"})
public class ModCommands {

    public static final ResourceKey<Registry<CommandType>> KEY = ResourceKey.createRegistryKey(new ResourceLocation(Robotics.MODID, "commands"));
    public static final DeferredRegister<CommandType> COMMANDS = DeferredRegister.create(KEY, Robotics.MODID);
    public static final Supplier<IForgeRegistry<CommandType>> REGISTRY = COMMANDS.makeRegistry(RegistryBuilder::new);
    public static final List<CommandType> COMMAND_TYPES = new ArrayList<>();

    public static final RegistryObject<CommandType> STAY = register("stay", GlobalPos.class, MoveToBlockGoal::new);
    public static final RegistryObject<CommandType> ATTACK = register("attack", EntityType.class, NearestAttackableTargetGoal::new);
    public static final RegistryObject<CommandType> ATTACK_SPECIFIC = register("attack_specific", EntitySearch.class, (robot, search) -> {
        if(!(robot.level() instanceof ServerLevel server)) return null;
        Entity entity = search.commence(server, robot.position());
        if(!(entity instanceof LivingEntity)) return null;
        return new SpecificTargetGoal(robot, (LivingEntity) entity);
    });
    public static final RegistryObject<CommandType> DEFEND = register("defend", EntitySearch.class, (robot, search) -> {
        if(!(robot.level() instanceof ServerLevel server)) return null;
        Entity entity = search.commence(server, robot.position());
        if(!(entity instanceof LivingEntity)) return null;
        return new DefendGoal(robot, (LivingEntity) entity, false);
    });
    public static final RegistryObject<CommandType> FOLLOW = register("follow", EntitySearch.class, Integer.class, (robot, search, range) -> {
        if(!(robot.level() instanceof ServerLevel server)) return null;
        Entity entity = search.commence(server, robot.position());
        if(!(entity instanceof LivingEntity)) return null;
        double followRange = robot.getAttributeValue(Attributes.FOLLOW_RANGE);
        return new FollowGoal(robot, entity, range, (float) followRange);
    });
    public static final RegistryObject<CommandType> RETRIEVE = register("retrieve", ItemStack.class, GlobalPos.class,
            (robot, stack, pos) -> new RetrieveGoal(robot, pos, stack, 20, 400, 200));
    public static final RegistryObject<CommandType> STORE = register("store", ItemStack.class, GlobalPos.class,
            (robot, stack, pos) -> new StoreGoal(robot, pos, stack, 20, 400, 200));
    public static final RegistryObject<CommandType> BREAK = register("break", GlobalPos.class, GlobalPos.class, BreakBlocksGoal::new);
    public static final RegistryObject<CommandType> PLACE = register("place", GlobalPos.class, GlobalPos.class, PlaceBlocksGoal::new);

    public static RegistryObject<CommandType> register(String name, Function<Mob, Goal> goal) {
        // We can just return the same instance everytime here
        CommandType command = new CommandType(name);
        command.setAISupplier((mob, sel) -> goal.apply(mob));
        return COMMANDS.register(name, () -> command);
    }

    public static <A> RegistryObject<CommandType> register(String name, Class<A> arg1, BiFunction<Mob, A, Goal> goal) {
        CommandType command = new CommandType(name, arg1);
        command.setAISupplier((mob, sel) -> goal.apply(mob, (A) sel[0].get()));
        return COMMANDS.register(name, () -> command);
    }

    public static <A, B> RegistryObject<CommandType> register(String name, Class<A> arg1, Class<B> arg2, Function3<Mob, A, B, Goal> goal) {
        CommandType command = new CommandType(name, arg1, arg2);
        command.setAISupplier((mob, sel) -> goal.apply(mob, (A) sel[0].get(), (B) sel[1].get()));
        return COMMANDS.register(name, () -> command);
    }

    public static <A, B, C> RegistryObject<CommandType> register(String name, Class<A> arg1, Class<B> arg2, Class<C> arg3, Function4<Mob, A, B, C, Goal> goal) {
        CommandType command = new CommandType(name, arg1, arg2, arg3);
        command.setAISupplier((mob, sel) -> goal.apply(mob, (A) sel[0].get(), (B) sel[1].get(), (C) sel[2].get()));
        return COMMANDS.register(name, () -> command);
    }

    public static <A, B, C, D> RegistryObject<CommandType> register(String name, Class<A> arg1, Class<B> arg2, Class<C> arg3, Class<D> arg4, Function5<Mob, A, B, C, D, Goal> goal) {
        CommandType command = new CommandType(name, arg1, arg2, arg3, arg4);
        command.setAISupplier((mob, sel) -> goal.apply(mob, (A) sel[0].get(), (B) sel[1].get(), (C) sel[2].get(), (D) sel[3].get()));
        return COMMANDS.register(name, () -> command);
    }
}
