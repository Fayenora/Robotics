package com.ignis.norabotics.integration.jei;

import com.ignis.norabotics.Robotics;
import com.ignis.norabotics.common.capabilities.impl.perk.Perk;
import com.ignis.norabotics.common.helpers.types.Tuple;
import com.ignis.norabotics.common.helpers.util.Lang;
import com.ignis.norabotics.common.robot.RobotModule;
import com.ignis.norabotics.definitions.robotics.ModPerks;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class PerkRecipeCategory implements IRecipeCategory<PerkRecipeCategory.PartPerkTuple> {

    IGuiHelper guiHelper;

    public PerkRecipeCategory(IGuiHelper guiHelper) {
        this.guiHelper = guiHelper;
    }

    @Override
    public RecipeType<PartPerkTuple> getRecipeType() {
        return RecipeType.create(Robotics.MODID, "perk", PartPerkTuple.class);
    }

    @Override
    public Component getTitle() {
        return Lang.localise("perks");
    }

    @Override
    public IDrawable getBackground() {
        return guiHelper.createBlankDrawable(50, 50);
    }

    @Override
    public IDrawable getIcon() {
        return guiHelper.createDrawableIngredient(RoboticsJEIPlugin.INGREDIENT_PERK, ModPerks.PERK_CHARGE.get());
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, PartPerkTuple recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.CATALYST, 18, 18).addIngredient(RoboticsJEIPlugin.INGREDIENT_PERK, recipe.first);

        builder.addSlot(RecipeIngredientRole.OUTPUT, 18, 0).addIngredients(recipe.second.getItems());
    }

    static class PartPerkTuple extends Tuple<Perk, RobotModule> {
        public PartPerkTuple(Perk first, RobotModule second) {
            super(first, second);
        }
    }
}
