package com.ignis.igrobotics.datagen;

import com.ignis.igrobotics.Reference;
import com.ignis.igrobotics.common.recipes.AssemblerRecipeBuilder;
import com.ignis.igrobotics.common.recipes.WireCutterRecipeBuilder;
import com.ignis.igrobotics.core.robot.EnumRobotMaterial;
import com.ignis.igrobotics.core.robot.EnumRobotPart;
import com.ignis.igrobotics.definitions.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.Tags;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class RecipeGenerator extends RecipeProvider {

    public RecipeGenerator(PackOutput pOutput) {
        super(pOutput);
    }

    @Override
    protected void buildRecipes(@NotNull Consumer<FinishedRecipe> writer) {
        for(EnumRobotMaterial material : EnumRobotMaterial.valuesWithoutEmpty()) {
            ItemLike plate = ModItems.PLATES[material.getID() - 1].get();

            //Plate
            new AssemblerRecipeBuilder(plate)
                    .pattern("iiii")
                    .define('i', material.getMetal())
                    .energyRequirement(material.getStiffness() * 10000)
                    .processingTime(material.getStiffness() * 100)
                    .save(writer);

            //Head
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.MATERIALS[material.getID() - 1][0].get())
                    .unlockedBy(getHasName(plate), has(plate))
                    .define('n', ModItems.NEURAL_PROCESSING_UNIT.get())
                    .define('p', plate)
                    .define('c', ModItems.CAMERA_UNIT.get())
                    .pattern("ppp")
                    .pattern("pnp")
                    .pattern(" c ")
                    .save(writer);

            //Body
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.MATERIALS[material.getID() - 1][1].get())
                    .unlockedBy(getHasName(plate), has(plate))
                    .define('p', plate)
                    .define('b', ModItems.MODULE_BATTERY.get())
                    .define('c', ModItems.ADVANCED_CIRCUIT.get())
                    .define('w', ModItems.ADVANCED_WIRING.get())
                    .define('g', Tags.Items.GLASS_PANES)
                    .pattern("pcp")
                    .pattern("bwb")
                    .pattern("pgp")
                    .save(writer);

            defineLimb(material, EnumRobotPart.RIGHT_ARM)
                    .pattern("mp ")
                    .pattern("pwp")
                    .pattern(" pm")
                    .save(writer);

            defineLimb(material, EnumRobotPart.LEFT_ARM)
                    .pattern(" pm")
                    .pattern("pwp")
                    .pattern("mp ")
                    .save(writer);

            defineLimb(material, EnumRobotPart.RIGHT_LEG)
                    .pattern(" mp")
                    .pattern("wmp")
                    .pattern("pp ")
                    .save(writer);

            defineLimb(material, EnumRobotPart.LEFT_LEG)
                    .pattern("pm ")
                    .pattern("pmw")
                    .pattern(" pp")
                    .save(writer);
        }

        int i = 0;
        for(EnumRobotMaterial material : Reference.WIRE_METALS) {
            new WireCutterRecipeBuilder(ModItems.WIRES[i++].get())
                    .input(material.getMetal())
                    .energyRequirement(10000 * material.getStiffness())
                    .processingTime(100)
                    .save(writer);
        }
    }

    private ShapedRecipeBuilder defineLimb(EnumRobotMaterial material, EnumRobotPart part) {
        ItemLike plate = ModItems.PLATES[material.getID() - 1].get();
        return ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.MATERIALS[material.getID() - 1][part.getID()].get())
                .unlockedBy(getHasName(plate), has(plate))
                .define('m', ModItems.SERVO_MOTOR.get())
                .define('p', plate)
                .define('w', ModItems.WIRING.get());
    }
}
