package com.ignis.igrobotics.integration.jei;

import com.ignis.igrobotics.Robotics;
import com.ignis.igrobotics.core.capabilities.perks.Perk;
import com.ignis.igrobotics.core.robot.RobotPart;
import com.ignis.igrobotics.core.util.Lang;
import com.ignis.igrobotics.core.util.Tuple;
import com.ignis.igrobotics.integration.config.RoboticsConfig;
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
        return guiHelper.createDrawableIngredient(RoboticsJEIPlugin.INGREDIENT_PERK, RoboticsConfig.current().perks.PERK_CHARGE);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, PartPerkTuple recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.CATALYST, 18, 18).addIngredient(RoboticsJEIPlugin.INGREDIENT_PERK, recipe.first);

        builder.addSlot(RecipeIngredientRole.OUTPUT, 18, 0).addItemStack(recipe.second.getItemStack(1));
    }

    static class PartPerkTuple extends Tuple<Perk, RobotPart> {
        public PartPerkTuple(Perk first, RobotPart second) {
            super(first, second);
        }
    }
}
