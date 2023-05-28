package com.ignis.igrobotics.core.robot;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public enum EnumRobotMaterial {
    NONE("none", 0),
    IRON("iron", 1),
    GOLD("gold", 1),
    COPPER("copper", 1),
    TIN("tin", 1),
    ALUMINIUM("aluminium", 2),
    NICKEL("nickel", 2),
    SILVER("silver", 2),
    LEAD("lead", 5),
    BRONZE("bronze", 2),
    CONSTANTAN("constantan", 2),
    STEEL("steel", 3),
    ELECTRUM("electrum", 2),
    //INVAR?

    //Thermal Series
    PLATINUM("platinum", 4),
    IRIDIUM("iridium", 5),
    SIGNALUM("signalum", 3),
    LUMIUM("lumium", 1),

    //Ender IO
    DARK_STEEL("dark_steel", 4),
    END_STEEL("end_steel", 5),

    //Nuclear Craft
    TOUGH_ALLOY("tough_alloy", 4),

    //Tinkers Construct
    COBALT("cobalt", 3),
    ARDITE("ardite", 3),
    MANYULLIN("manyullin", 4),

    //Mekanism
    OSMIUM("osmium", 3),

    //Psi
    PSIMETAL("psimetal", 3);

    private final String name;
    private final int stiffness;
    private final TagKey<Item> metalTag;

    /**
     * @param name the name of the material
     * @param stiffness used for energy processing costs in processing; range [0, 5]
     */
    EnumRobotMaterial(String name, int stiffness) {
        this.name = name;
        this.stiffness = stiffness;
        this.metalTag = ItemTags.create(new ResourceLocation("forge", "ingots/" + name));
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
}
