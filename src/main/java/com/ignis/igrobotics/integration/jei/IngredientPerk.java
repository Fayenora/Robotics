package com.ignis.igrobotics.integration.jei;

import com.ignis.igrobotics.common.capabilities.impl.perk.Perk;
import com.mojang.blaze3d.systems.RenderSystem;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class IngredientPerk implements IIngredientHelper<Perk>, IIngredientRenderer<Perk> {
    @Override
    public IIngredientType<Perk> getIngredientType() {
        return RoboticsJEIPlugin.INGREDIENT_PERK;
    }

    @Override
    public String getDisplayName(Perk ingredient) {
        return ingredient.toString();
    }

    @Override
    public String getUniqueId(Perk ingredient, UidContext context) {
        return ingredient.getKey().toString();
    }

    @Override
    public ResourceLocation getResourceLocation(Perk ingredient) {
        return ingredient.getKey();
    }

    @Override
    public Perk copyIngredient(Perk ingredient) {
        return ingredient.clone();
    }

    @Override
    public String getErrorInfo(@Nullable Perk ingredient) {
        return ingredient.toString();
    }

    @Override
    public void render(GuiGraphics graphics, Perk ingredient) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        graphics.setColor(1, 1, 1, 1);
        graphics.pose().scale(1 / 16f, 1 / 16f, 1/ 16f);
        graphics.blit(ingredient.getIconTexture(), 0, 0, 0, 0, 256, 256);
    }

    @Override
    public List<Component> getTooltip(Perk ingredient, TooltipFlag tooltipFlag) {
        MutableComponent display = ingredient.localized();
        display.setStyle(display.getStyle().withColor(ingredient.getDisplayColor()));
        return List.of(display);
    }
}
