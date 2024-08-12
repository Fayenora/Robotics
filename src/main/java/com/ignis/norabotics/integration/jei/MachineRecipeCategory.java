package com.ignis.norabotics.integration.jei;

import com.ignis.norabotics.Robotics;
import com.ignis.norabotics.common.content.recipes.MachineRecipe;
import com.ignis.norabotics.common.helpers.types.Machine;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

@MethodsReturnNonnullByDefault
public abstract class MachineRecipeCategory implements IRecipeCategory<MachineRecipe<?>> {

	IGuiHelper guiHelper;
	protected final Machine<?> machine;
	protected final ResourceLocation texture;
	private final RecipeType<?> recipeType;
	
	public MachineRecipeCategory(IGuiHelper guiHelper, Machine<?> machine) {
		this.guiHelper = guiHelper;
		this.machine = machine;
		texture = new ResourceLocation(Robotics.MODID, "textures/gui/" + machine.getName() + ".png");
		recipeType = RecipeType.create(Robotics.MODID, machine.getName(), MachineRecipe.class);
	}

	@Override
	public RecipeType getRecipeType() {
		return recipeType;
	}

	@Override
	public Component getTitle() {
		return machine.getTitle();
	}

	@Override
	public IDrawable getIcon() {
		return guiHelper.createDrawableItemStack(new ItemStack(machine.getItem()));
	}

}
