package com.io.norabotics.datagen;

import com.io.norabotics.Robotics;
import com.io.norabotics.common.content.recipes.AssemblerRecipeBuilder;
import com.io.norabotics.common.content.recipes.WireCutterRecipeBuilder;
import com.io.norabotics.common.misc.CountedIngredient;
import com.io.norabotics.common.robot.EnumRobotMaterial;
import com.io.norabotics.common.robot.EnumRobotPart;
import com.io.norabotics.definitions.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.crafting.ConditionalRecipe;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.NotCondition;
import net.minecraftforge.common.crafting.conditions.TagEmptyCondition;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Function;

public class RecipeGenerator extends RecipeProvider {

    public RecipeGenerator(PackOutput pOutput) {
        super(pOutput);
    }

    @Override
    protected void buildRecipes(@NotNull Consumer<FinishedRecipe> writer) {
        for(EnumRobotMaterial material : ModItems.PLATES.keySet()) {
            ItemLike plate = ModItems.PLATES.get(material).get();
            ItemLike head = ModItems.MATERIALS.get(material).get(EnumRobotPart.HEAD).get();
            ItemLike body = ModItems.MATERIALS.get(material).get(EnumRobotPart.BODY).get();

            ICondition condition = new NotCondition(new TagEmptyCondition(material.getMetal().location()));
            Function<ResourceLocation, Consumer<FinishedRecipe>> requiresMaterial = loc -> recipe -> ConditionalRecipe.builder().addCondition(condition).addRecipe(recipe).build(writer, loc);
            Function<ItemLike, Consumer<FinishedRecipe>> requiresMaterial2 = item -> requiresMaterial.apply(RecipeBuilder.getDefaultRecipeId(item));
            Function<EnumRobotPart, Consumer<FinishedRecipe>> requiresMaterial3 = part -> requiresMaterial2.apply(ModItems.MATERIALS.get(material).get(part).get());

            //Plate
            new AssemblerRecipeBuilder(plate)
                    .pattern("iiii")
                    .define('i', material.getMetal())
                    .energyRequirement(material.getStiffness() * 10000)
                    .processingTime(material.getStiffness() * 100)
                    .save(requiresMaterial2.apply(plate));

            //Head
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, head)
                    .unlockedBy(getHasName(plate), has(plate))
                    .define('n', ModItems.NEURAL_PROCESSING_UNIT.get())
                    .define('p', plate)
                    .define('c', ModItems.CAMERA_UNIT.get())
                    .pattern("ppp")
                    .pattern("pnp")
                    .pattern(" c ")
                    .save(requiresMaterial2.apply(head));

            //Body
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, body)
                    .unlockedBy(getHasName(plate), has(plate))
                    .define('p', plate)
                    .define('b', ModItems.MODULE_BATTERY.get())
                    .define('c', ModItems.ADVANCED_CIRCUIT.get())
                    .define('w', ModItems.ADVANCED_WIRING.get())
                    .define('g', Tags.Items.GLASS_PANES)
                    .pattern("pgp")
                    .pattern("bwb")
                    .pattern("pcp")
                    .save(requiresMaterial2.apply(body));

            defineLimb(material, EnumRobotPart.RIGHT_ARM)
                    .pattern("mp ")
                    .pattern("pwp")
                    .pattern(" pm")
                    .save(requiresMaterial3.apply(EnumRobotPart.RIGHT_ARM));

            defineLimb(material, EnumRobotPart.LEFT_ARM)
                    .pattern(" pm")
                    .pattern("pwp")
                    .pattern("mp ")
                    .save(requiresMaterial3.apply(EnumRobotPart.LEFT_ARM));

            defineLimb(material, EnumRobotPart.RIGHT_LEG)
                    .pattern(" mp")
                    .pattern("wmp")
                    .pattern("pp ")
                    .save(requiresMaterial3.apply(EnumRobotPart.RIGHT_LEG));

            defineLimb(material, EnumRobotPart.LEFT_LEG)
                    .pattern("pm ")
                    .pattern("pmw")
                    .pattern(" pp")
                    .save(requiresMaterial3.apply(EnumRobotPart.LEFT_LEG));

            // Conversion recipes
            ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.MATERIALS.get(material).get(EnumRobotPart.RIGHT_ARM).get())
                    .unlockedBy(getHasName(plate), has(plate))
                    .requires(ModItems.MATERIALS.get(material).get(EnumRobotPart.LEFT_ARM).get())
                    .save(requiresMaterial.apply(Robotics.rl(material.getName() + "_left_to_right_arm")));
            ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.MATERIALS.get(material).get(EnumRobotPart.LEFT_ARM).get())
                    .unlockedBy(getHasName(plate), has(plate))
                    .requires(ModItems.MATERIALS.get(material).get(EnumRobotPart.RIGHT_ARM).get())
                    .save(requiresMaterial.apply(Robotics.rl(material.getName() + "_right_to_left_arm")));
            ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.MATERIALS.get(material).get(EnumRobotPart.RIGHT_LEG).get())
                    .unlockedBy(getHasName(plate), has(plate))
                    .requires(ModItems.MATERIALS.get(material).get(EnumRobotPart.LEFT_LEG).get())
                    .save(requiresMaterial.apply(Robotics.rl(material.getName() + "_left_to_right_leg")));
            ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.MATERIALS.get(material).get(EnumRobotPart.LEFT_LEG).get())
                    .unlockedBy(getHasName(plate), has(plate))
                    .requires(ModItems.MATERIALS.get(material).get(EnumRobotPart.RIGHT_LEG).get())
                    .save(requiresMaterial.apply(Robotics.rl(material.getName() + "_right_to_left_leg")));
        }

        for(EnumRobotMaterial material : ModItems.WIRES.keySet()) {
            new WireCutterRecipeBuilder(ModItems.WIRES.get(material).get(), 3)
                    .input(new CountedIngredient(new Ingredient.TagValue(material.getMetal()), 2))
                    .energyRequirement(10000 * material.getStiffness())
                    .processingTime(100)
                    .save(writer);
        }
    }

    private ShapedRecipeBuilder defineLimb(EnumRobotMaterial material, EnumRobotPart part) {
        ItemLike plate = ModItems.PLATES.get(material).get();
        return ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.MATERIALS.get(material).get(part).get())
                .unlockedBy(getHasName(plate), has(plate))
                .showNotification(false)
                .define('m', ModItems.SERVO_MOTOR.get())
                .define('p', plate)
                .define('w', ModItems.WIRING.get());
    }
}
