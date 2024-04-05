package com.ignis.igrobotics.common.recipes;

import com.google.gson.JsonObject;
import com.ignis.igrobotics.core.MachineRecipe;
import com.ignis.igrobotics.core.util.InventoryUtil;
import com.ignis.igrobotics.definitions.ModItems;
import com.ignis.igrobotics.definitions.ModMachines;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.CraftingHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@ParametersAreNonnullByDefault
public class WireCutterRecipes implements IRecipeSerializer<MachineRecipe<?>> {

    public static final int INPUT_SIZE = 2;
    public static final int OUTPUT_SIZE = 1;
    public static List<MachineRecipe<?>> recipes = new ArrayList<>();

    @Override
    public List<MachineRecipe<?>> getRecipes() {
        return recipes;
    }

    @Override
    public @NotNull MachineRecipe<?> fromJson(ResourceLocation loc, JsonObject json) {
        int processing_time = json.get("processing_time").getAsInt();
        int energy = json.get("energy").getAsInt();
        Ingredient ingredient = InventoryUtil.fromJson(json.get("ingredient"));
        ItemStack result = CraftingHelper.getItemStack(json.getAsJsonObject("result"), true);

        MachineRecipe<?> recipe = new MachineRecipe.Builder(ModMachines.WIRE_CUTTER, loc)
                .setProcessingTime(processing_time)
                .setEnergyRequirement(energy)
                .setInputs(new Ingredient[] {Ingredient.of(ModItems.IRON_ROD.get()), ingredient})
                .setOutput(result).build();
        recipes.add(recipe);
        return recipe;
    }

    @Override
    public @Nullable MachineRecipe<?> fromNetwork(ResourceLocation loc, FriendlyByteBuf buf) {
        int energy = buf.readInt();
        int processing_time = buf.readInt();
        Ingredient[] inputs = new Ingredient[INPUT_SIZE];
        ItemStack[] outputs = new ItemStack[OUTPUT_SIZE];
        for(int i = 0; i < INPUT_SIZE; i++) {
            inputs[i] = Ingredient.fromNetwork(buf);
        }
        for(int i = 0; i < OUTPUT_SIZE; i++) {
            outputs[i] = buf.readItem();
        }

        return new MachineRecipe.Builder(ModMachines.WIRE_CUTTER, loc)
                .setInputs(inputs)
                .setOutputs(outputs)
                .setEnergyRequirement(energy)
                .setProcessingTime(processing_time).build();
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf, MachineRecipe<?> recipe) {
        buf.writeInt(recipe.getEnergy());
        buf.writeInt(recipe.getProcessingTime());
        for(Ingredient ingredient : recipe.getInputs()) {
            ingredient.toNetwork(buf);
        }
        for(ItemStack stack : recipe.getOutputs()) {
            buf.writeItem(stack);
        }
    }
}
