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
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings({"unchecked", "unused"})
public class ModCommands {

    public static final ResourceKey<Registry<CommandType>> KEY = ResourceKey.createRegistryKey(Robotics.rl("commands"));
    public static final DeferredRegister<CommandType> COMMANDS = DeferredRegister.create(KEY, Robotics.MODID);
    public static final Supplier<IForgeRegistry<CommandType>> REGISTRY = COMMANDS.makeRegistry(RegistryBuilder::new);

    public static final RegistryObject<CommandType> STAY = register("stay", GlobalPos.class, MoveToBlockGoal::new);
    public static final RegistryObject<CommandType> ATTACK = register("attack", EntitySearch.class, SpecificTargetGoal::new);
    public static final RegistryObject<CommandType> DEFEND = register("defend", EntitySearch.class, DefendGoal::new);
    public static final RegistryObject<CommandType> FOLLOW = register("follow", EntitySearch.class, Integer.class, FollowGoal::new);
    public static final RegistryObject<CommandType> RETRIEVE = register("retrieve", ItemStack.class, GlobalPos.class, RetrieveGoal::new);
    public static final RegistryObject<CommandType> STORE = register("store", ItemStack.class, GlobalPos.class, StoreGoal::new);
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
