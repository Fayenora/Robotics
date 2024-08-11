package com.ignis.igrobotics.datagen;

import com.google.gson.JsonElement;
import com.ignis.igrobotics.Robotics;
import com.ignis.igrobotics.common.capabilities.impl.perk.Perk;
import com.ignis.igrobotics.common.robot.EnumModuleSlot;
import com.ignis.igrobotics.common.robot.EnumRobotMaterial;
import com.ignis.igrobotics.common.robot.EnumRobotPart;
import com.ignis.igrobotics.common.robot.RobotModule;
import com.ignis.igrobotics.definitions.ModItems;
import com.ignis.igrobotics.definitions.ModPerks;
import com.ignis.igrobotics.definitions.ModTags;
import com.mojang.serialization.JsonOps;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.IntersectionIngredient;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class ModuleProvider implements DataProvider {

    private final PackOutput.PathProvider pathProvider;
    private final Map<ResourceLocation, RobotModule> modules = new HashMap<>() {
        @Override
        public RobotModule put(ResourceLocation key, RobotModule value) {
            return containsKey(key) ? super.put(key, value.merge(get(key))) : super.put(key, value);
        }
    };

    public ModuleProvider(PackOutput output) {
        pathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, Robotics.MODID + "/modules");
    }

    @Override
    public String getName() {
        return "Modules";
    }

    protected abstract void buildModules();

    @Override
    public CompletableFuture<?> run(CachedOutput pOutput) {
        List<CompletableFuture<?>> list = new ArrayList<>();
        modules.clear();
        buildModules();
        for(Map.Entry<ResourceLocation, RobotModule> module : modules.entrySet()) {
            JsonElement json = RobotModule.CODEC.encodeStart(JsonOps.INSTANCE, module.getValue()).getOrThrow(false, s -> {});
            list.add(DataProvider.saveStable(pOutput, json, pathProvider.json(module.getKey())));
        }
        return CompletableFuture.allOf(list.toArray(CompletableFuture[]::new));
    }

    private static ResourceLocation id(EnumRobotMaterial material, EnumRobotPart part) {
        return ModItems.MATERIALS.get(material).get(part).getId();
    }

    private static Ingredient ingredient(EnumRobotMaterial material, EnumModuleSlot part) {
        return IntersectionIngredient.of(Ingredient.of(
                        ModTags.MATERIAL_TAGS.get(material)),
                Ingredient.of(ModTags.PART_TAGS.get(part)));
    }

    private static Ingredient limbsOf(EnumRobotMaterial material) {
        return IntersectionIngredient.of(
                Ingredient.of(ModTags.MATERIAL_TAGS.get(material)),
                Ingredient.merge(List.of(
                        Ingredient.of(ModTags.PART_TAGS.get(EnumModuleSlot.LEFT_ARM)),
                        Ingredient.of(ModTags.PART_TAGS.get(EnumModuleSlot.LEFT_LEG)),
                        Ingredient.of(ModTags.PART_TAGS.get(EnumModuleSlot.RIGHT_ARM)),
                        Ingredient.of(ModTags.PART_TAGS.get(EnumModuleSlot.RIGHT_LEG))
                )));
    }

    protected void stats(EnumRobotMaterial material, int headHealth, int headArmor, int headTough, int bodyHealth, int bodyArmor, int bodyTough, int health, int armor, int tough) {
        if(headHealth > 0) addPerk(material, EnumRobotPart.HEAD, ModPerks.PERK_HEALTH.get(), headHealth);
        if(headArmor > 0) addPerk(material, EnumRobotPart.HEAD, ModPerks.PERK_ARMOR.get(), headArmor);
        if(headTough > 0) addPerk(material, EnumRobotPart.HEAD, ModPerks.PERK_TOUGHNESS.get(), headTough);
        if(bodyHealth > 0) addPerk(material, EnumRobotPart.BODY, ModPerks.PERK_HEALTH.get(), bodyHealth);
        if(bodyArmor > 0) addPerk(material, EnumRobotPart.BODY, ModPerks.PERK_ARMOR.get(), bodyArmor);
        if(bodyTough > 0) addPerk(material, EnumRobotPart.BODY, ModPerks.PERK_TOUGHNESS.get(), bodyTough);
        if(health > 0) addLimbPerk(material, ModPerks.PERK_HEALTH.get(), health);
        if(armor > 0) addLimbPerk(material, ModPerks.PERK_ARMOR.get(), armor);
        if(tough > 0) addLimbPerk(material, ModPerks.PERK_TOUGHNESS.get(), tough);
    }

    protected  void universal(EnumRobotMaterial material, Perk perk) {
        universal(material, perk, 1);
    }

    protected void universal(EnumRobotMaterial material, Perk perk, int level) {
        Ingredient ingredient = Ingredient.of(ModTags.MATERIAL_TAGS.get(material));
        RobotModule module = new RobotModule.ModuleBuilder(ingredient).addPerk(perk, level).build();
        modules.put(Robotics.rl("material_" + material), module);
    }

    protected void addPerk(EnumRobotMaterial material, EnumRobotPart part, Perk perk) {
        addPerk(material, part, perk, 1);
    }

    protected void addPerk(EnumRobotMaterial material, EnumRobotPart part, Perk perk, int level) {
        modules.put(id(material, part), new RobotModule.ModuleBuilder(ingredient(material, part.toModuleSlot())).addPerk(perk, level).build());
    }

    protected void addLimbPerk(EnumRobotMaterial material, Perk perk) {
        addLimbPerk(material, perk, 1);
    }

    protected void addLimbPerk(EnumRobotMaterial material, Perk perk, int level) {
        modules.put(Robotics.rl(material + "_limbs"), new RobotModule.ModuleBuilder(limbsOf(material)).addPerk(perk, level).build());
    }

    protected void addLegPerk(EnumRobotMaterial material, Perk perk) {
        addLegPerk(material, perk, 1);
    }

    protected void addLegPerk(EnumRobotMaterial material, Perk perk, int level) {
        Ingredient ingredient = IntersectionIngredient.of(
                Ingredient.of(ModTags.MATERIAL_TAGS.get(material)),
                Ingredient.merge(List.of(
                        Ingredient.of(ModTags.PART_TAGS.get(EnumModuleSlot.LEFT_LEG)),
                        Ingredient.of(ModTags.PART_TAGS.get(EnumModuleSlot.RIGHT_LEG))
                )));
        modules.put(Robotics.rl(material + "_legs"), new RobotModule.ModuleBuilder(ingredient).addPerk(perk, level).build());
    }

    protected void addArmPerk(EnumRobotMaterial material, Perk perk) {
        addArmPerk(material, perk, 1);
    }

    protected void addArmPerk(EnumRobotMaterial material, Perk perk, int level) {
        Ingredient ingredient = IntersectionIngredient.of(
                Ingredient.of(ModTags.MATERIAL_TAGS.get(material)),
                Ingredient.merge(List.of(
                        Ingredient.of(ModTags.PART_TAGS.get(EnumModuleSlot.LEFT_ARM)),
                        Ingredient.of(ModTags.PART_TAGS.get(EnumModuleSlot.RIGHT_ARM))
                )));
        modules.put(Robotics.rl(material + "_arms"), new RobotModule.ModuleBuilder(ingredient).addPerk(perk, level).build());
    }

    protected void addCorePerk(EnumRobotMaterial material, Perk perk) {
        addCorePerk(material, perk, 1);
    }

    protected void addCorePerk(EnumRobotMaterial material, Perk perk, int level) {
        Ingredient ingredient = IntersectionIngredient.of(
                Ingredient.of(ModTags.MATERIAL_TAGS.get(material)),
                Ingredient.merge(List.of(
                        Ingredient.of(ModTags.PART_TAGS.get(EnumModuleSlot.BODY)),
                        Ingredient.of(ModTags.PART_TAGS.get(EnumModuleSlot.HEAD))
                )));
        modules.put(Robotics.rl(material + "_core"), new RobotModule.ModuleBuilder(ingredient).addPerk(perk, level).build());
    }

}
