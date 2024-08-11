package com.ignis.igrobotics.common.helpers.types;

import com.ignis.igrobotics.common.content.recipes.IRecipeSerializer;
import com.ignis.igrobotics.common.helpers.util.Lang;
import com.ignis.igrobotics.integration.config.RoboticsConfig;
import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.List;

@FieldsAreNonnullByDefault
public class Machine<R extends Recipe<?>> {

    private final String name;
    private RegistryObject<Block> block;
    private Dimension guiSize;
    @Nullable
    private RegistryObject<RecipeType<R>> recipeType;
    @Nullable
    private RegistryObject<IRecipeSerializer<R>> recipeSerializer;
    private RegistryObject<BlockEntityType<?>> blockEntityType;

    private Machine(String name) {
        this.name = name;
    }

    public int getEnergyCapacity() {
        return RoboticsConfig.general.energyCapacities.getOrDefault(name, () -> 1000000).get();
    }

    public int getEnergyTransfer() { return 100000; }

    public double getEnergyConsumption() {
        return RoboticsConfig.general.energyConsumption.getOrDefault(name, () -> 1d).get();
    }

    public double getProcessingSpeed() {
        return RoboticsConfig.general.processingSpeed.getOrDefault(name, () -> 1d).get();
    }

    public RecipeType<R> getRecipeType() {
        return recipeType.get();
    }

    public RecipeSerializer<R> getRecipeSerializer() {
        return recipeSerializer.get();
    }

    public BlockEntityType<?> getBlockEntityType() {
        return blockEntityType.get();
    }

    public List<R> getRecipes() {
        if(recipeSerializer == null) return List.of();
        return recipeSerializer.get().getRecipes();
    }

    public String getName() {
        return name;
    }

    public Component getTitle() {
        return Lang.localise(getName());
    }

    public Item getItem() {
        return block.get().asItem();
    }

    public Dimension getGuiSize() {
        return guiSize;
    }

    public static class Builder<R extends Recipe<?>> {
        private final Machine<R> machine;
        public Builder(String identifier) {
            machine = new Machine<>(identifier);
        }
        public Builder<R> setBlock(RegistryObject<Block> block) {
            machine.block = block;
            return this;
        }
        public Builder<R> setGuiDimensions(Dimension dimensions) {
            machine.guiSize = dimensions;
            return this;
        }
        public Builder<R> setRecipeType(RegistryObject<RecipeType<R>> recipeType) {
            machine.recipeType = recipeType;
            return this;
        }
        public Builder<R> setRecipeSerializer(RegistryObject<IRecipeSerializer<R>> recipeSerializer) {
            machine.recipeSerializer = recipeSerializer;
            return this;
        }
        public Builder<R> setBlockEntityType(RegistryObject<BlockEntityType<?>> blockEntityType) {
            machine.blockEntityType = blockEntityType;
            return this;
        }
        public Machine<R> build() {
            return machine;
        }
    }
}
