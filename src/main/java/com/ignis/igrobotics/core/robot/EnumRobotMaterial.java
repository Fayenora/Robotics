package com.ignis.igrobotics.core.robot;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import javax.annotation.Nullable;

public enum EnumRobotMaterial {
    // Vanilla
    NONE("none", 0),
    IRON("iron", 1),
    GOLD("gold", 1),
    OXIDIZED_COPPER("oxidized_copper", 0),
    WEATHERED_COPPER("weathered_copper", 0, OXIDIZED_COPPER),
    COPPER("copper", 1, WEATHERED_COPPER),

    // Basic Metals & Alloys
    TIN("tin", 1),
    ALUMINIUM("aluminium", 2),
    NICKEL("nickel", 2),
    SILVER("silver", 2),
    LEAD("lead", 5),
    BRONZE("bronze", 3),
    CONSTANTAN("constantan", 2),
    STEEL("steel", 3),
    ELECTRUM("electrum", 2),
    //INVAR
    //TITANIUM

    // Thermal Series
    PLATINUM("platinum", 4),
    IRIDIUM("iridium", 5),
    SIGNALUM("signalum", 3),
    LUMIUM("lumium", 1),
    ENDERIUM("enderium", 5),

    // Ender IO
    DARK_STEEL("dark_steel", 4),
    END_STEEL("end_steel", 5),

    // Mekanism
    OSMIUM("osmium", 3),

    // Psi
    PSIMETAL("psimetal", 3),

    // Non-metals
    DIAMOND("diamond", 4),
    NETHERITE("netherite", 4);
    // AMETHYST

    private final String name;
    private final int stiffness;
    private final TagKey<Item> metalTag;
    private final EnumRobotMaterial corrodesTo;

    /**
     * @param name the name of the material
     * @param stiffness used for energy processing costs in processing; range [0, 5]
     */
    EnumRobotMaterial(String name, int stiffness) {
        this(name, stiffness, null);
    }

    EnumRobotMaterial(String name, int stiffness, @Nullable EnumRobotMaterial corrodesTo) {
        this.name = name;
        this.stiffness = stiffness;
        this.metalTag = ItemTags.create(new ResourceLocation("forge", "ingots/" + name));
        this.corrodesTo = corrodesTo;
    }

    public String getName() {
        return this.name;
    }

    public int getID() {
        return this.ordinal();
    }

    public int getStiffness() {
        return this.stiffness;
    }

    public TagKey<Item> getMetal() {
        return metalTag;
    }

    public static EnumRobotMaterial byId(int id) {
        return EnumRobotMaterial.values()[id];
    }

    public static EnumRobotMaterial[] valuesWithoutEmpty() {
        EnumRobotMaterial[] values = new EnumRobotMaterial[values().length - 1];
        int i = 0;
        for (EnumRobotMaterial mat : values()) {
            if (mat.equals(NONE)) continue;
            values[i++] = mat;
        }
        return values;
    }

    @Override
    public String toString() {
        return name;
    }

    public EnumRobotMaterial getWeatheredMaterial() {
        return corrodesTo;
    }
}
