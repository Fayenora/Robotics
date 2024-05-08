package com.ignis.igrobotics.datagen;

import com.ignis.igrobotics.Robotics;
import com.ignis.igrobotics.core.robot.EnumModuleSlot;
import com.ignis.igrobotics.core.robot.EnumRobotMaterial;
import com.ignis.igrobotics.definitions.ModItems;
import com.ignis.igrobotics.definitions.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

public class TagGenerator extends IntrinsicHolderTagsProvider<Item> {

    public TagGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> pLookupProvider, ExistingFileHelper fileHelper) {
        super(output, Registries.ITEM, pLookupProvider, (p_255790_) -> {
            return p_255790_.builtInRegistryHolder().key();
        }, Robotics.MODID, fileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        int nMaterials = Math.min(ModTags.MATERIAL_TAGS.size(), ModItems.MATERIALS.length);
        int nParts = Math.min(ModTags.PART_TAGS.size(), ModItems.MATERIALS[0].length);
        for(int i = 0; i < nMaterials; i++) {
            TagKey<Item> key = ModTags.MATERIAL_TAGS.get(i);
            for(var material : ModItems.MATERIALS[i]) {
                tag(key).add(material.getKey());
            }
        }
        for(int j = 0; j < nParts; j++) {
            TagKey<Item> key = ModTags.PART_TAGS.get(j);
            for(int i = 0; i < nMaterials; i++) {
                tag(key).add(ModItems.MATERIALS[i][j].getKey());
            }
        }
    }
}
