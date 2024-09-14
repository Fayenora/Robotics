package com.io.norabotics.common.content.recipes;

import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.List;

public interface IRecipeSerializer<R extends Recipe<?>> extends RecipeSerializer<R> {

    List<R> getRecipes();

}
