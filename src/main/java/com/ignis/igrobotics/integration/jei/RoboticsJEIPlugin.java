package com.ignis.igrobotics.integration.jei;

import com.ignis.igrobotics.Robotics;
import com.ignis.igrobotics.client.menu.AssemblerMenu;
import com.ignis.igrobotics.client.menu.WireCutterMenu;
import com.ignis.igrobotics.client.screen.AssemblerScreen;
import com.ignis.igrobotics.client.screen.WireCutterScreen;
import com.ignis.igrobotics.client.screen.base.BaseContainerScreen;
import com.ignis.igrobotics.common.recipes.AssemblerRecipes;
import com.ignis.igrobotics.common.recipes.WireCutterRecipes;
import com.ignis.igrobotics.definitions.ModBlocks;
import com.ignis.igrobotics.definitions.ModMenuTypes;
import com.ignis.igrobotics.integration.cc.ProgrammingScreen;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.*;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;
import java.awt.*;
import java.util.List;

@JeiPlugin
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class RoboticsJEIPlugin implements IModPlugin {

    public static final ResourceLocation LOCATION = new ResourceLocation(Robotics.MODID, "jei");

    public static IJeiRuntime JEI_RUNTIME;
    private MachineRecipeCategory assemblerCategory, wireCutterCategory;

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();

        assemblerCategory = new AssemblerRecipeCategory(guiHelper);
        wireCutterCategory = new WireCutterRecipeCategory(guiHelper);
        registration.addRecipeCategories(assemblerCategory);
        registration.addRecipeCategories(wireCutterCategory);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.ASSEMBLER.get().asItem()), assemblerCategory.getRecipeType());
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.WIRE_CUTTER.get().asItem()), wireCutterCategory.getRecipeType());
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(assemblerCategory.getRecipeType(), AssemblerRecipes.recipes);
        registration.addRecipes(wireCutterCategory.getRecipeType(), WireCutterRecipes.recipes);
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(AssemblerMenu.class, ModMenuTypes.ASSEMBLER.get(), assemblerCategory.getRecipeType(), 36, 4, 0, 36);
        registration.addRecipeTransferHandler(WireCutterMenu.class, ModMenuTypes.WIRE_CUTTER.get(), wireCutterCategory.getRecipeType(), 36, 2, 0, 36);
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        addRecipeClickArea(registration, AssemblerScreen.class, AssemblerScreen.arr_up, assemblerCategory.getRecipeType());
        addRecipeClickArea(registration, AssemblerScreen.class, AssemblerScreen.arr_down, assemblerCategory.getRecipeType());
        addRecipeClickArea(registration, AssemblerScreen.class, AssemblerScreen.arr_left, assemblerCategory.getRecipeType());
        addRecipeClickArea(registration, AssemblerScreen.class, AssemblerScreen.arr_right, assemblerCategory.getRecipeType());
        addRecipeClickArea(registration, WireCutterScreen.class, WireCutterScreen.arrow, wireCutterCategory.getRecipeType());
        registration.addGuiContainerHandler(BaseContainerScreen.class, new IGuiContainerHandler<>() {
            @Override
            public List<Rect2i> getGuiExtraAreas(BaseContainerScreen containerScreen) {
                return containerScreen.getBlockingAreas();
            }
        });
        registration.addGuiContainerHandler(ProgrammingScreen.class, new IGuiContainerHandler<>() {
            @Override
            public List<Rect2i> getGuiExtraAreas(ProgrammingScreen programmingScreen) {
                return programmingScreen.getBlockingAreas();
            }
        });
    }

    private void addRecipeClickArea(IGuiHandlerRegistration registration, Class<? extends AbstractContainerScreen<?>> screenClass, Rectangle clickArea, RecipeType<?> recipeType) {
        registration.addRecipeClickArea(screenClass, clickArea.x, clickArea.y, clickArea.width, clickArea.height, recipeType);
    }

    @Override
    public ResourceLocation getPluginUid() {
        return LOCATION;
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        JEI_RUNTIME = jeiRuntime;
    }
}
