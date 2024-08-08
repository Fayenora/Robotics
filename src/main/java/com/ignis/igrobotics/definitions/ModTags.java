package com.ignis.igrobotics.definitions;

import com.ignis.igrobotics.Robotics;
import com.ignis.igrobotics.core.robot.EnumModuleSlot;
import com.ignis.igrobotics.core.robot.EnumRobotMaterial;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.util.HashMap;
import java.util.Map;

/**
 * The tags defined here are only used for the generator {@link com.ignis.igrobotics.datagen.TagGenerator}
 */
public class ModTags {

    public static final Map<EnumModuleSlot, TagKey<Item>> PART_TAGS = new HashMap<>();
    public static final Map<EnumRobotMaterial, TagKey<Item>> MATERIAL_TAGS = new HashMap<>();

    static {
        for(EnumRobotMaterial material : EnumRobotMaterial.valuesWithoutEmpty()) {
            MATERIAL_TAGS.put(material, tag("material/" +material.getName()));
        }
        for(EnumModuleSlot part : EnumModuleSlot.values()) {
            PART_TAGS.put(part, tag("part/" + part.getSerializedName()));
        }
    }

    private static TagKey<Item> tag(String name) {
        return ItemTags.create(new ResourceLocation(Robotics.MODID, name));
    }
}
