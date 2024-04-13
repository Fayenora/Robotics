package com.ignis.igrobotics.integration.jei;

import com.ignis.igrobotics.Reference;
import com.ignis.igrobotics.client.menu.WireCutterMenu;
import com.ignis.igrobotics.client.screen.WireCutterScreen;
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
public class WireCutterRecipeCategory extends MachineRecipeCategory {
	
	private static final int animation_time = 50;

	IDrawableAnimated energy_bar;
	IDrawableAnimated arrow;
	
	public WireCutterRecipeCategory(IGuiHelper guiHelper) {
		super(guiHelper, ModMachines.WIRE_CUTTER);
		IDrawableStatic static_energy_bar = guiHelper.createDrawable(Reference.ENERGY_BAR, 0, 0, 13, WireCutterScreen.energy_bar.height);
		IDrawableStatic static_arrow = guiHelper.createDrawable(Reference.MISC, 233, 196, WireCutterScreen.arrow.width, WireCutterScreen.arrow.height);
		
		energy_bar = guiHelper.createAnimatedDrawable(static_energy_bar, animation_time, IDrawableAnimated.StartDirection.TOP, true);
		arrow = guiHelper.createAnimatedDrawable(static_arrow, animation_time, IDrawableAnimated.StartDirection.LEFT, false);
	}

	@Override
	public void draw(MachineRecipe<?> recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
		energy_bar.draw(graphics, WireCutterScreen.energy_bar.x - 7, WireCutterScreen.energy_bar.y - 4);
		arrow.draw(graphics, WireCutterScreen.arrow.x - 7, WireCutterScreen.arrow.y - 4);
	}

	@Override
	public List<Component> getTooltipStrings(MachineRecipe<?> recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
		if(WireCutterScreen.arrow.contains(mouseX + 7, mouseY + 4)) {
			return recipe.getRuntimeTooltip();
		}
		if(WireCutterScreen.energy_bar.contains(mouseX + 7, mouseY + 4)) {
			return recipe.getEnergyTooltip();
		}
		return List.of();
	}

	@Override
	public IDrawable getBackground() {
		return guiHelper.createDrawable(texture, 7, 4, 162, 77);
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, MachineRecipe<?> recipe, IFocusGroup focuses) {
		builder.addSlot(RecipeIngredientRole.INPUT, WireCutterMenu.slot_upper.x - 7, WireCutterMenu.slot_upper.y - 4)
				.addIngredients(recipe.getIngredients().get(0));
		builder.addSlot(RecipeIngredientRole.INPUT, WireCutterMenu.slot_main.x - 7, WireCutterMenu.slot_main.y - 4)
				.addIngredients(recipe.getIngredients().get(1));
		builder.addSlot(RecipeIngredientRole.OUTPUT, WireCutterMenu.slot_out.x - 7, WireCutterMenu.slot_out.y - 4)
				.addIngredient(VanillaTypes.ITEM_STACK, recipe.getOutputs()[0]);
	}

}
