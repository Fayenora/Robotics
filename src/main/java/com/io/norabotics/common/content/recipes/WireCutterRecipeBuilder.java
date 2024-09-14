package com.io.norabotics.common.content.recipes;

import com.google.gson.JsonObject;
import com.io.norabotics.definitions.ModMachines;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class WireCutterRecipeBuilder implements RecipeBuilder {

    private final Item result;
    private final int count;
    private Ingredient input;
    private int energy, processingTime;

    public WireCutterRecipeBuilder(ItemLike result) {
        this(result, 1);
    }

    public WireCutterRecipeBuilder(ItemLike result, int count) {
        this.result = result.asItem();
        this.count = count;
    }

    public WireCutterRecipeBuilder energyRequirement(int energy) {
        this.energy = energy;
        return this;
    }

    public WireCutterRecipeBuilder processingTime(int processingTime) {
        this.processingTime = processingTime;
        return this;
    }

    public WireCutterRecipeBuilder input(TagKey<Item> pTag) {
        return input(Ingredient.of(pTag));
    }

    public WireCutterRecipeBuilder input(ItemLike pItem) {
        return input(Ingredient.of(pItem));
    }

    public WireCutterRecipeBuilder input(Ingredient pIngredient) {
        this.input = pIngredient;
        return this;
    }

    @Override
    public Item getResult() {
        return result;
    }

    @Override
    public void save(Consumer<FinishedRecipe> consumer, ResourceLocation id) {
        consumer.accept(new WireCutterRecipeBuilder.Result(id, result, count, energy, processingTime, input));
    }

    @Override
    public WireCutterRecipeBuilder unlockedBy(String pCriterionName, CriterionTriggerInstance pCriterionTrigger) {
        return this;
    }

    @Override
    public WireCutterRecipeBuilder group(@Nullable String pGroupName) {
        return this;
    }

    public static class Result implements FinishedRecipe {

        private final ResourceLocation id;
        private final Item result;
        private final int count, energy, processing_time;
        private final Ingredient input;

        public Result(ResourceLocation id, Item result, int count, int energy, int processing_time, Ingredient input) {
            this.id = id;
            this.result = result;
            this.count = count;
            this.energy = energy;
            this.processing_time = processing_time;
            this.input = input;
        }

        @Override
        public void serializeRecipeData(JsonObject json) {
            json.addProperty("processing_time", processing_time);
            json.addProperty("energy", energy);
            json.add("ingredient", input.toJson());
            JsonObject result = new JsonObject();
            result.addProperty("item", ForgeRegistries.ITEMS.getKey(this.result).toString());
            if (this.count > 1) {
                result.addProperty("count", this.count);
            }
            json.add("result", result);
        }

        @Override
        public ResourceLocation getId() {
            return id;
        }

        @Override
        public RecipeSerializer<?> getType() {
            return ModMachines.WIRE_CUTTER.getRecipeSerializer();
        }

        @Nullable
        @Override
        public JsonObject serializeAdvancement() {
            return null;
        }

        @Nullable
        @Override
        public ResourceLocation getAdvancementId() {
            return null;
        }
    }
}
