package com.ignis.igrobotics.core;

import com.google.gson.JsonObject;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import net.minecraftforge.common.crafting.VanillaIngredientSerializer;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;

@MethodsReturnNonnullByDefault
public class CountedIngredient extends Ingredient {

    public static final CountedIngredientSerializer SERIALIZER = new CountedIngredientSerializer();

    protected CountedIngredient(Ingredient ingredient) {
        super(Arrays.stream(ingredient.getItems()).filter((stack) -> {
            return !stack.isEmpty();
        }).map(Ingredient.ItemValue::new));
    }

    @Override
    @SuppressWarnings("DataFlowIssue")
    public boolean test(@Nullable ItemStack stack) {
        return super.test(stack) && stack.getCount() >= getCount(this);
    }

    @Override
    public IIngredientSerializer<? extends Ingredient> getSerializer() {
        return SERIALIZER;
    }

    public static int getCount(Ingredient ingredient) {
        if(ingredient.isEmpty()) return 0;
        return ingredient.getItems()[0].getCount();
    }

    @ParametersAreNonnullByDefault
    private static class CountedIngredientSerializer implements IIngredientSerializer<CountedIngredient> {
        @Override
        public CountedIngredient parse(FriendlyByteBuf buffer) {
            return (CountedIngredient) VanillaIngredientSerializer.INSTANCE.parse(buffer);
        }

        @Override
        public CountedIngredient parse(JsonObject json) {
            if(!json.isJsonObject()) return new CountedIngredient(Ingredient.EMPTY);
            JsonObject jsonObject = json.getAsJsonObject();
            CountedIngredient ingredient = new CountedIngredient(Ingredient.fromJson(jsonObject.get("items")));
            if(jsonObject.has("amount")) {
                int amount = json.getAsJsonObject().get("amount").getAsInt();
                for(ItemStack stack : ingredient.getItems()) {
                    stack.setCount(amount);
                }
            }
            return ingredient;
        }

        @Override
        public void write(FriendlyByteBuf buffer, CountedIngredient ingredient) {
            VanillaIngredientSerializer.INSTANCE.write(buffer, ingredient);
        }
    }
}
