package com.ignis.igrobotics.core;

import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.RegistryObject;

public class Machine<T extends BlockEntity> {

    private RegistryObject<RecipeType<?>> recipeType;
    private RegistryObject<BlockEntityType<T>> blockEntityType;

    public Machine(RegistryObject<RecipeType<?>> recipeType, RegistryObject<BlockEntityType<T>> blockEntityType) {
        this.recipeType = recipeType;
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

    public RecipeType<?> getRecipeType() {
        return recipeType.get();
    }

    public BlockEntityType<T> getBlockEntityType() {
        return blockEntityType.get();
    }

    public MachineRecipe[] getRecipes() {
        return new MachineRecipe[] {};
    }
}
