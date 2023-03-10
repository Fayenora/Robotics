package com.ignis.igrobotics;

import com.ignis.igrobotics.common.blockentity.BlockEntityAssembler;
import com.ignis.igrobotics.common.blockentity.BlockEntityFactory;
import com.ignis.igrobotics.common.blockentity.BlockEntityStorage;
import com.ignis.igrobotics.common.blockentity.BlockEntityWireCutter;
import com.ignis.igrobotics.common.recipes.AssemblerRecipes;
import com.ignis.igrobotics.common.recipes.WireCutterRecipes;
import com.ignis.igrobotics.core.IRecipeSerializer;
import com.ignis.igrobotics.core.Machine;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class ModMachines {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Robotics.MODID);
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, Robotics.MODID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, Robotics.MODID);

    public static final Machine ASSEMBLER = registerMachine("assembler", BlockEntityAssembler::new, ModBlocks.ASSEMBLER, AssemblerRecipes::new);
    public static final Machine WIRE_CUTTER = registerMachine("wire_cutter", BlockEntityWireCutter::new, ModBlocks.WIRE_CUTTER, WireCutterRecipes::new);
    public static final Machine ROBOT_FACTORY = registerMachine("robot_factory", BlockEntityFactory::new, ModBlocks.ROBOT_FACTORY, null);
    public static final RegistryObject<BlockEntityType<?>> ROBOT_STORAGE = BLOCK_ENTITIES.register("robot_storage", () -> BlockEntityType.Builder.of(BlockEntityStorage::new, ModBlocks.ROBOT_STORAGE.get()).build(null));

    private static <T extends BlockEntity> Machine registerMachine(String name, BlockEntityType.BlockEntitySupplier<T> supplier, Supplier<Block> block, Supplier<? extends IRecipeSerializer<?>> recipeSerializer) {
        RegistryObject<BlockEntityType<T>> blockEntityType = BLOCK_ENTITIES.register(name, () -> BlockEntityType.Builder.of(supplier, block.get()).build(null));
        if(recipeSerializer == null) return new Machine(null, null, blockEntityType);
        RegistryObject<RecipeType<?>> recipeType = RECIPE_TYPES.register(name, () -> RecipeType.simple(new ResourceLocation(Robotics.MODID, name)));
        RegistryObject<IRecipeSerializer<?>> serializer = RECIPE_SERIALIZERS.register(name, recipeSerializer);
        return new Machine(recipeType, serializer, blockEntityType);
    }

}
