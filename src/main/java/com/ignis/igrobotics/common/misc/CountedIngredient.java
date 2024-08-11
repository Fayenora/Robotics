package com.ignis.igrobotics.common.misc;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ignis.igrobotics.Robotics;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import net.minecraftforge.common.crafting.VanillaIngredientSerializer;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.stream.Stream;

@MethodsReturnNonnullByDefault
public class CountedIngredient extends Ingredient {

    public static final CountedIngredientSerializer SERIALIZER = new CountedIngredientSerializer();
    private final int amount;

    protected CountedIngredient(Ingredient ingredient, int amount) {
        super(Arrays.stream(ingredient.getItems()).filter(stack -> !stack.isEmpty()).map(Ingredient.ItemValue::new));
        this.amount = amount;
    }

    public CountedIngredient(Value ingredient, int amount) {
        super(Stream.of(ingredient));
        this.amount = amount;
    }

    @Override
    @SuppressWarnings("DataFlowIssue")
    public boolean test(@Nullable ItemStack stack) {
        return super.test(stack) && stack.getCount() >= amount;
    }

    @Override
    public ItemStack[] getItems() {
        ItemStack[] stacks = super.getItems();
        for(ItemStack stack : stacks) {
            stack.setCount(amount);
        }
        return stacks;
    }

    @Override
    public JsonElement toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("type", Robotics.rl("counted").toString());
        json.add("items", super.toJson());
        json.addProperty("amount", this.amount);
        return json;
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
            return new CountedIngredient(VanillaIngredientSerializer.INSTANCE.parse(buffer), buffer.readInt());
        }

        @Override
        public CountedIngredient parse(JsonObject json) {
            if(!json.isJsonObject()) return new CountedIngredient(new ItemValue(new ItemStack(Items.BARRIER)), 1);
            JsonObject jsonObject = json.getAsJsonObject();
            Value ingredient = Ingredient.valueFromJson(jsonObject.get("items").getAsJsonObject());
            int amount = 1;
            if(jsonObject.has("amount")) {
                amount = jsonObject.get("amount").getAsInt();
            }
            return new CountedIngredient(ingredient, amount);
        }

        @Override
        public void write(FriendlyByteBuf buffer, CountedIngredient ingredient) {
            VanillaIngredientSerializer.INSTANCE.write(buffer, ingredient);
            buffer.writeInt(ingredient.amount);
        }
    }
}
