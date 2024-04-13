package com.ignis.igrobotics.integration.jei;

import com.ignis.igrobotics.Reference;
import com.ignis.igrobotics.client.menu.AssemblerMenu;
import com.ignis.igrobotics.client.screen.AssemblerScreen;
import com.ignis.igrobotics.core.MachineRecipe;
import com.ignis.igrobotics.definitions.ModMachines;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AssemblerRecipeCategory extends MachineRecipeCategory {
	
	private static final int animation_time = 50;

	IDrawableAnimated energy_bar;
	IDrawableAnimated arrow_down;
	IDrawableAnimated arrow_up;
	IDrawableAnimated arrow_right;
	IDrawableAnimated arrow_left;
	
	public AssemblerRecipeCategory(IGuiHelper guiHandler) {
		super(guiHandler, ModMachines.ASSEMBLER);
		IDrawableStatic static_energy_bar = guiHandler.createDrawable(Reference.ENERGY_BAR, 0, 0, 13, 109);
		IDrawableStatic static_arrow_down = guiHandler.createDrawable(Reference.MISC, 233, 211, AssemblerScreen.arr_down.width, AssemblerScreen.arr_down.height);
		IDrawableStatic static_arrow_up = guiHandler.createDrawable(Reference.MISC, 233, 233, AssemblerScreen.arr_up.width, AssemblerScreen.arr_up.height);
		IDrawableStatic static_arrow_left = guiHandler.createDrawable(Reference.MISC, 233, 196, AssemblerScreen.arr_left.width, AssemblerScreen.arr_left.height);
		IDrawableStatic static_arrow_right = guiHandler.createDrawable(Reference.MISC, 233, 180, AssemblerScreen.arr_right.width, AssemblerScreen.arr_right.height);
		
		energy_bar = guiHandler.createAnimatedDrawable(static_energy_bar, animation_time, IDrawableAnimated.StartDirection.TOP, true);
		arrow_down = guiHandler.createAnimatedDrawable(static_arrow_down, animation_time, IDrawableAnimated.StartDirection.TOP, false);
		arrow_up = guiHandler.createAnimatedDrawable(static_arrow_up, animation_time, IDrawableAnimated.StartDirection.BOTTOM, false);
		arrow_right = guiHandler.createAnimatedDrawable(static_arrow_right, animation_time, IDrawableAnimated.StartDirection.RIGHT, false);
		arrow_left = guiHandler.createAnimatedDrawable(static_arrow_left, animation_time, IDrawableAnimated.StartDirection.LEFT, false);
	}

	@Override
	public void draw(MachineRecipe<?> recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
		energy_bar.draw(graphics, AssemblerScreen.energy_bar.x - 7, AssemblerScreen.energy_bar.y - 4);
		arrow_down.draw(graphics, AssemblerScreen.arr_down.x - 7, AssemblerScreen.arr_down.y - 4);
		arrow_up.draw(graphics, AssemblerScreen.arr_up.x - 7, AssemblerScreen.arr_up.y - 4);
		arrow_right.draw(graphics, AssemblerScreen.arr_right.x - 7, AssemblerScreen.arr_right.y - 4);
		arrow_left.draw(graphics, AssemblerScreen.arr_left.x - 7, AssemblerScreen.arr_left.y - 4);
	}

	@Override
	public List<Component> getTooltipStrings(MachineRecipe<?> recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
		if (AssemblerScreen.arr_down.contains(mouseX + 7, mouseY + 4) ||
				AssemblerScreen.arr_up.contains(mouseX + 7, mouseY + 4) ||
				AssemblerScreen.arr_right.contains(mouseX + 7, mouseY + 4) ||
				AssemblerScreen.arr_left.contains(mouseX + 7, mouseY + 4)) {
			return recipe.getRuntimeTooltip();
		}
		if(AssemblerScreen.energy_bar.contains(mouseX + 7, mouseY + 4)) {
			return recipe.getEnergyTooltip();
		}
		return List.of();
	}

	@Override
	public IDrawable getBackground() {
		return guiHelper.createDrawable(texture, 7, 4, 162, 127);
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, MachineRecipe<?> recipe, IFocusGroup focuses) {
		builder.addSlot(RecipeIngredientRole.INPUT, AssemblerMenu.slot_top.x - 7, AssemblerMenu.slot_top.y - 4)
				.addIngredients(recipe.getIngredients().get(0));
		builder.addSlot(RecipeIngredientRole.INPUT, AssemblerMenu.slot_right.x - 7, AssemblerMenu.slot_right.y - 4)
				.addIngredients(recipe.getIngredients().get(1));
		builder.addSlot(RecipeIngredientRole.INPUT, AssemblerMenu.slot_bot.x - 7, AssemblerMenu.slot_bot.y - 4)
				.addIngredients(recipe.getIngredients().get(2));
		builder.addSlot(RecipeIngredientRole.INPUT, AssemblerMenu.slot_left.x - 7, AssemblerMenu.slot_left.y - 4)
				.addIngredients(recipe.getIngredients().get(3));

		builder.addSlot(RecipeIngredientRole.OUTPUT, AssemblerMenu.slot_out.x - 7, AssemblerMenu.slot_out.y - 4)
				.addIngredient(VanillaTypes.ITEM_STACK, recipe.getOutputs()[0]);
	}

}
