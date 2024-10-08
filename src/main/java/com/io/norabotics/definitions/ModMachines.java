package com.io.norabotics.definitions;

import com.io.norabotics.Reference;
import com.io.norabotics.Robotics;
import com.io.norabotics.common.content.blockentity.*;
import com.io.norabotics.common.content.recipes.AssemblerRecipes;
import com.io.norabotics.common.content.recipes.CommanderCopyRecipe;
import com.io.norabotics.common.content.recipes.IRecipeSerializer;
import com.io.norabotics.common.content.recipes.WireCutterRecipes;
import com.io.norabotics.common.helpers.types.Machine;
import com.io.norabotics.integration.cc.RedstoneIntegrator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.awt.*;
import java.util.function.Supplier;

public class ModMachines {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Robotics.MODID);
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, Robotics.MODID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, Robotics.MODID);

    public static final Machine ASSEMBLER = registerMachine("assembler", Reference.GUI_ASSEMBLER_DIMENSIONS, AssemblerBlockEntity::new, ModBlocks.ASSEMBLER, AssemblerRecipes::new);
    public static final Machine WIRE_CUTTER = registerMachine("wire_cutter", Reference.GUI_DEFAULT_DIMENSIONS, WireCutterBlockEntity::new, ModBlocks.WIRE_CUTTER, WireCutterRecipes::new);
    public static final Machine ROBOT_FACTORY = registerMachine("robot_factory", Reference.GUI_ROBOT_FACTORY_DIMENSIONS, FactoryBlockEntity::new, ModBlocks.ROBOT_FACTORY, null);
    public static final Machine ROBOT_STORAGE = registerMachine("robot_storage", Reference.GUI_DEFAULT_DIMENSIONS, StorageBlockEntity::new, ModBlocks.ROBOT_STORAGE, null);

    public static final RegistryObject<BlockEntityType<ChargerBlockEntity>> CHARGER = registerBlockEntity("charger", ChargerBlockEntity::new, ModBlocks.CHARGER);
    public static final RegistryObject<BlockEntityType<RedstoneIntegrator>> REDSTONE_INTEGRATOR = registerBlockEntity("redstone_integrator", RedstoneIntegrator::new, ModBlocks.REDSTONE_INTEGRATOR);
    public static final RegistryObject<BlockEntityType<MachineArmBlockEntity>> MACHINE_ARM = registerBlockEntity("machine_arm", MachineArmBlockEntity::new, ModBlocks.MACHINE_ARM);

    public static final RegistryObject<RecipeSerializer> COMMANDERCOPY_RECIPE = RECIPE_SERIALIZERS.register("crafting_special_commandercloning", () -> new SimpleCraftingRecipeSerializer<>(CommanderCopyRecipe::new));

    private static <R extends Recipe<?>> Machine<?> registerMachine(String name, Dimension guiDimension, BlockEntityType.BlockEntitySupplier<?> supplier, RegistryObject<Block> block, Supplier<? extends IRecipeSerializer<R>> recipeSerializer) {
        RegistryObject<BlockEntityType<?>> blockEntityType = BLOCK_ENTITIES.register(name, () -> BlockEntityType.Builder.of(supplier, block.get()).build(null));
        Machine.Builder<R> builder = new Machine.Builder<R>(name).setBlock(block).setBlockEntityType(blockEntityType).setGuiDimensions(guiDimension);
        if(recipeSerializer == null) return builder.build();
        RegistryObject<RecipeType<R>> recipeType = RECIPE_TYPES.register(name, () -> RecipeType.simple(new ResourceLocation(Robotics.MODID, name)));
        RegistryObject<IRecipeSerializer<R>> serializer = RECIPE_SERIALIZERS.register(name, recipeSerializer);
        return builder.setRecipeType(recipeType).setRecipeSerializer(serializer).build();
    }

    private static <T extends BlockEntity> RegistryObject<BlockEntityType<T>> registerBlockEntity(String name, BlockEntityType.BlockEntitySupplier<T> supplier, RegistryObject<Block> block) {
        return BLOCK_ENTITIES.register(name, () -> BlockEntityType.Builder.of(supplier, block.get()).build(null));
    }

}
