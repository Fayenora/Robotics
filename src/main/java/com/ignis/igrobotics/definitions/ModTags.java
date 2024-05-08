package com.ignis.igrobotics.definitions;

import com.ignis.igrobotics.Robotics;
import com.ignis.igrobotics.core.robot.EnumModuleSlot;
import com.ignis.igrobotics.core.robot.EnumRobotMaterial;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;

public class ModTags {

    public static final List<TagKey<Item>> PART_TAGS = new ArrayList<>();
    public static final List<TagKey<Item>> MATERIAL_TAGS = new ArrayList<>();

    static {
        for(EnumRobotMaterial material : EnumRobotMaterial.valuesWithoutEmpty()) {
            MATERIAL_TAGS.add(tag("material/" +material.getName()));
        }
        for(EnumModuleSlot part : EnumModuleSlot.values()) {
            PART_TAGS.add(tag("part/" + part.getName()));
        }
    }

    private static TagKey<Item> tag(String name) {
        return ItemTags.create(new ResourceLocation(Robotics.MODID, name));
    }
}
