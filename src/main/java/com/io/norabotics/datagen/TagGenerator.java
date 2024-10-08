package com.io.norabotics.datagen;

import com.io.norabotics.Robotics;
import com.io.norabotics.common.robot.EnumRobotMaterial;
import com.io.norabotics.common.robot.EnumRobotPart;
import com.io.norabotics.definitions.ModItems;
import com.io.norabotics.definitions.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.concurrent.CompletableFuture;

@ParametersAreNonnullByDefault
public class TagGenerator extends IntrinsicHolderTagsProvider<Item> {

    public TagGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> pLookupProvider, ExistingFileHelper fileHelper) {
        super(output, Registries.ITEM, pLookupProvider, (p_255790_) -> {
            return p_255790_.builtInRegistryHolder().key();
        }, Robotics.MODID, fileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        for(EnumRobotMaterial material : ModItems.MATERIALS.keySet()) {
            for(EnumRobotPart part : ModItems.MATERIALS.get(material).keySet()) {
                TagKey<Item> materialTag = ModTags.MATERIAL_TAGS.get(material);
                TagKey<Item> partTag = ModTags.PART_TAGS.get(part.toModuleSlot());
                ResourceKey<Item> itemKey = ModItems.MATERIALS.get(material).get(part).getKey();
                if(itemKey == null) continue;
                tag(materialTag).add(itemKey);
                tag(partTag).add(itemKey);
            }
        }
    }
}
