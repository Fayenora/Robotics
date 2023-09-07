package com.ignis.igrobotics.integration.jei;

import com.ignis.igrobotics.Robotics;
import com.ignis.igrobotics.core.capabilities.perks.Perk;
import com.ignis.igrobotics.core.util.Lang;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

import static net.minecraft.client.gui.GuiComponent.blit;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class IngredientPerk implements IIngredientHelper<Perk>, IIngredientRenderer<Perk> {
    @Override
    public IIngredientType<Perk> getIngredientType() {
        return RoboticsJEIPlugin.INGREDIENT_PERK;
    }

    @Override
    public String getDisplayName(Perk ingredient) {
        return Lang.localise(ingredient.getUnlocalizedName()).getString();
    }

    @Override
    public String getUniqueId(Perk ingredient, UidContext context) {
        return ingredient.getUnlocalizedName();
    }

    @Override
    public ResourceLocation getResourceLocation(Perk ingredient) {
        return new ResourceLocation(Robotics.MODID, ingredient.getUnlocalizedName().toLowerCase());
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
    public void render(PoseStack stack, Perk ingredient) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, ingredient.getIconTexture());
        stack.scale(1 / 16f, 1 / 16f, 1/ 16f);
        blit(stack, 0, 0, 0, 0, 256, 256);
    }

    @Override
    public List<Component> getTooltip(Perk ingredient, TooltipFlag tooltipFlag) {
        MutableComponent display = Lang.localise(ingredient.getUnlocalizedName());
        display.setStyle(display.getStyle().withColor(ingredient.getDisplayColor()));
        return List.of(display);
    }
}
