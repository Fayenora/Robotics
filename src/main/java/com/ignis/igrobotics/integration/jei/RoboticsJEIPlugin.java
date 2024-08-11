package com.ignis.igrobotics.integration.jei;

import com.ignis.igrobotics.Robotics;
import com.ignis.igrobotics.client.screen.AssemblerScreen;
import com.ignis.igrobotics.client.screen.WireCutterScreen;
import com.ignis.igrobotics.client.screen.base.BaseContainerScreen;
import com.ignis.igrobotics.common.capabilities.impl.perk.Perk;
import com.ignis.igrobotics.common.content.menu.AssemblerMenu;
import com.ignis.igrobotics.common.content.menu.WireCutterMenu;
import com.ignis.igrobotics.common.content.recipes.AssemblerRecipes;
import com.ignis.igrobotics.common.content.recipes.WireCutterRecipes;
import com.ignis.igrobotics.common.helpers.types.Tuple;
import com.ignis.igrobotics.common.robot.RobotModule;
import com.ignis.igrobotics.definitions.ModBlocks;
import com.ignis.igrobotics.definitions.ModMenuTypes;
import com.ignis.igrobotics.definitions.ModPerks;
import com.ignis.igrobotics.integration.cc.ProgrammingScreen;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.*;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
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
    private static MachineRecipeCategory assemblerCategory, wireCutterCategory;
    private static PerkRecipeCategory perkCategory;

    public static final IIngredientType<Perk> INGREDIENT_PERK = () -> Perk.class;
    private static IModIngredientRegistration ingredientRegistration;
    private static IRecipeRegistration recipeRegistration;

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();

        assemblerCategory = new AssemblerRecipeCategory(guiHelper);
        wireCutterCategory = new WireCutterRecipeCategory(guiHelper);
        perkCategory = new PerkRecipeCategory(guiHelper);
        registration.addRecipeCategories(assemblerCategory);
        registration.addRecipeCategories(wireCutterCategory);
        registration.addRecipeCategories(perkCategory);
    }

    @Override
    public void registerIngredients(IModIngredientRegistration registration) {
        Registry<Perk> perkRegistry = Robotics.proxy.getRegistryAccess().registryOrThrow(ModPerks.KEY);
        List<Perk> perks = perkRegistry.stream().filter(p -> !p.getKey().equals(ModPerks.PERK_UNDEFINED.getId())).toList();
        ingredientRegistration = registration;
        ingredientRegistration.register(INGREDIENT_PERK, perks, new IngredientPerk(), new IngredientPerk());
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.ASSEMBLER.get().asItem()), assemblerCategory.getRecipeType());
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.WIRE_CUTTER.get().asItem()), wireCutterCategory.getRecipeType());
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        recipeRegistration = registration;
        registration.addRecipes(assemblerCategory.getRecipeType(), AssemblerRecipes.recipes);
        registration.addRecipes(wireCutterCategory.getRecipeType(), WireCutterRecipes.recipes);
        Registry<Perk> perkRegistry = Robotics.proxy.getRegistryAccess().registryOrThrow(ModPerks.KEY);
        for(Perk perk : perkRegistry) {
            Component descriptionText = perk.getDescriptionText();
            if(descriptionText == null) return;
            registration.addIngredientInfo(perk, INGREDIENT_PERK, descriptionText);
        }
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

    public static void registerModulePerkRelation(RobotModule module) {
        for(Tuple<Perk, Integer> perk : module.getPerks()) {
            recipeRegistration.addRecipes(perkCategory.getRecipeType(), List.of(new PerkRecipeCategory.PartPerkTuple(perk.first, module)));
        }
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
