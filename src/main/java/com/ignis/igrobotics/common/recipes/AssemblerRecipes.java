package com.ignis.igrobotics.common.recipes;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.ignis.igrobotics.definitions.ModMachines;
import com.ignis.igrobotics.core.IRecipeSerializer;
import com.ignis.igrobotics.core.MachineRecipe;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.CraftingHelper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AssemblerRecipes implements IRecipeSerializer<MachineRecipe<?>> {

    public static final int INPUT_SIZE = 4;
    public static final int OUTPUT_SIZE = 1;
    public static List<MachineRecipe<?>> recipes = new ArrayList<>();

    @Override
    public MachineRecipe fromJson(ResourceLocation loc, JsonObject json) {
        int processing_time = json.get("processing_time").getAsInt();
        int energy = json.get("energy").getAsInt();
        ItemStack result = CraftingHelper.getItemStack(json.getAsJsonObject("result"), true);

        // Pattern
        String pattern = json.get("pattern").getAsString();

        if(pattern.length() == 0)
            throw new JsonSyntaxException("Invalid pattern: empty pattern not allowed");
        if(pattern.length() > INPUT_SIZE)
            new JsonSyntaxException("Invalid pattern: should not exceed " + INPUT_SIZE + " items.");

        // Put Ingredients in map
        Map<Character, Ingredient> ingMap = Maps.newHashMap();
        for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject("key").entrySet()) {
            if (entry.getKey().length() != 1)
                throw new JsonSyntaxException("Invalid key entry: '" + entry.getKey() + "' is an invalid symbol (must be 1 character only).");
            if (" ".equals(entry.getKey()))
                throw new JsonSyntaxException("Invalid key entry: ' ' is a reserved symbol.");

            ingMap.put(entry.getKey().toCharArray()[0], Ingredient.fromJson(entry.getValue()));
        }

        Set<Character> keys = Sets.newHashSet(ingMap.keySet());

        ingMap.put(' ', Ingredient.EMPTY);

        // Ingredients
        int i = 0;
        Ingredient[] ingr = new Ingredient[INPUT_SIZE];
        for (char chr : pattern.toCharArray()) {
            Ingredient ing = ingMap.get(chr);
            if (ing == null)
                throw new JsonSyntaxException("Pattern references symbol '" + chr + "' but it's not defined in the key");
            ingr[i] = ing;
            keys.remove(chr);
            i++;
        }

        if (!keys.isEmpty())
            throw new JsonSyntaxException("Key defines symbols that aren't used in pattern: " + keys);

        MachineRecipe recipe = new MachineRecipe.Builder(ModMachines.ASSEMBLER, loc)
                .setInputs(ingr)
                .setOutputs(new ItemStack[] {result})
                .setEnergyRequirement(energy)
                .setProcessingTime(processing_time).build();
        recipes.add(recipe);
        return recipe;
    }

    @Override
    public @Nullable MachineRecipe fromNetwork(ResourceLocation loc, FriendlyByteBuf buf) {
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

        return new MachineRecipe.Builder(ModMachines.ASSEMBLER, loc)
                .setInputs(inputs)
                .setOutputs(outputs)
                .setEnergyRequirement(energy)
                .setProcessingTime(processing_time).build();
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf, MachineRecipe recipe) {
        buf.writeInt(recipe.getEnergy());
        buf.writeInt(recipe.getProcessingTime());
        for(Ingredient ingr : recipe.getInputs()) {
            ingr.toNetwork(buf);
        }
        for(ItemStack stack : recipe.getOutputs()) {
            buf.writeItem(stack);
        }
    }

    @Override
    public List<MachineRecipe<?>> getRecipes() {
        return recipes;
    }
}
