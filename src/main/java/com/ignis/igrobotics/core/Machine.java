package com.ignis.igrobotics.core;

import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

public class Machine<T extends BlockEntity, R extends Recipe<?>> {

    private RegistryObject<RecipeType<R>> recipeType;
    private RegistryObject<IRecipeSerializer<R>> recipeSerializer;
    private RegistryObject<BlockEntityType<T>> blockEntityType;

    public Machine(RegistryObject<RecipeType<R>> recipeType, RegistryObject<IRecipeSerializer<R>> serializer, RegistryObject<BlockEntityType<T>> blockEntityType) {
        this.recipeType = recipeType;
        this.recipeSerializer = serializer;
        this.blockEntityType = blockEntityType;
    }

    public int getEnergyCapacity() {
        return 1000000; //TODO Load from config
    }

    public int getEnergyTransfer() { return 10000; }

    public float getEnergyConsumption() {
        return 10; //TODO Load from config
    }

    public float getProcessingSpeed() {
        return 1; //TODO Load from config
    }

    public RecipeType<R> getRecipeType() {
        return recipeType.get();
    }

    public RecipeSerializer<R> getRecipeSerializer() {
        return recipeSerializer.get();
    }

    public BlockEntityType<T> getBlockEntityType() {
        return blockEntityType.get();
    }

    public List<R> getRecipes() {
        return recipeSerializer.get().getRecipes();
    }
}
