package com.ignis.norabotics.common.content.recipes;

import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import com.ignis.norabotics.definitions.ModMachines;
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
import java.util.Map;
import java.util.function.Consumer;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class AssemblerRecipeBuilder implements RecipeBuilder {

    private final Item result;
    private final int count;
    private String pattern;
    private int energy, processingTime;
    private final Map<Character, Ingredient> keys = Maps.newLinkedHashMap();

    public AssemblerRecipeBuilder(ItemLike result) {
        this(result, 1);
    }

    public AssemblerRecipeBuilder(ItemLike result, int count) {
        this.result = result.asItem();
        this.count = count;
    }

    public AssemblerRecipeBuilder energyRequirement(int energy) {
        this.energy = energy;
        return this;
    }

    public AssemblerRecipeBuilder processingTime(int processingTime) {
        this.processingTime = processingTime;
        return this;
    }

    public AssemblerRecipeBuilder pattern(String pattern) {
        this.pattern = pattern;
        return this;
    }

    public AssemblerRecipeBuilder define(Character pSymbol, TagKey<Item> pTag) {
        return this.define(pSymbol, Ingredient.of(pTag));
    }

    public AssemblerRecipeBuilder define(Character pSymbol, ItemLike pItem) {
        return this.define(pSymbol, Ingredient.of(pItem));
    }

    public AssemblerRecipeBuilder define(Character pSymbol, Ingredient pIngredient) {
        if (this.keys.containsKey(pSymbol)) {
            throw new IllegalArgumentException("Symbol '" + pSymbol + "' is already defined!");
        } else if (pSymbol == ' ') {
            throw new IllegalArgumentException("Symbol ' ' (whitespace) is reserved and cannot be defined");
        } else {
            this.keys.put(pSymbol, pIngredient);
            return this;
        }
    }

    @Override
    public Item getResult() {
        return result;
    }

    @Override
    public void save(Consumer<FinishedRecipe> consumer, ResourceLocation id) {
        consumer.accept(new Result(id, result, count, energy, processingTime, pattern, keys));
    }

    @Override
    public AssemblerRecipeBuilder unlockedBy(String pCriterionName, CriterionTriggerInstance pCriterionTrigger) {
        return this;
    }

    @Override
    public AssemblerRecipeBuilder group(@Nullable String pGroupName) {
        return this;
    }

    public static class Result implements FinishedRecipe {

        private final ResourceLocation id;
        private final Item result;
        private final int count, energy, processing_time;
        private final String pattern;
        private final Map<Character, Ingredient> keys;

        public Result(ResourceLocation id, Item result, int count, int energy, int processing_time, String pattern, Map<Character, Ingredient> keys) {
            this.id = id;
            this.result = result;
            this.count = count;
            this.energy = energy;
            this.processing_time = processing_time;
            this.pattern = pattern;
            this.keys = keys;
        }

        @Override
        public void serializeRecipeData(JsonObject json) {
            json.addProperty("processing_time", processing_time);
            json.addProperty("energy", energy);
            json.addProperty("pattern", pattern);
            JsonObject jsonKeys = new JsonObject();
            for(Map.Entry<Character, Ingredient> entry : this.keys.entrySet()) {
                jsonKeys.add(String.valueOf(entry.getKey()), entry.getValue().toJson());
            }
            json.add("key", jsonKeys);
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
            return ModMachines.ASSEMBLER.getRecipeSerializer();
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
