package com.ignis.igrobotics.core;

import com.ignis.igrobotics.core.util.Lang;
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
import java.util.Arrays;
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
        return 1000000; //TODO Load from config
    }

    public int getEnergyTransfer() { return 100000; }

    public float getEnergyConsumption() {
        return 1; //TODO Load from config
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
            boolean allValuesFilled = Arrays.stream(Machine.class.getDeclaredFields()).allMatch(field -> {
                try {
                    Object value = field.get(machine);
                    return true;//field.isAnnotationPresent(Nullable.class) || value != null;
                } catch (IllegalAccessException ignored) {}
                return true;
            });
            if(!allValuesFilled) {
                throw new IllegalStateException("All nonnull fields must be instantiated via the builder for the machine " + machine.getName());
            }
            return machine;
        }
    }
}
