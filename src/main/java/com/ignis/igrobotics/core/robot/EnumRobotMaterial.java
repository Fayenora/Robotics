package com.ignis.igrobotics.core.robot;

public enum EnumRobotMaterial {
    NONE(0, "none", 0),
    IRON(1, "iron", 1),
    GOLD(2, "gold", 1),
    COPPER(3, "copper", 1),
    TIN(4, "tin", 1),
    ALUMINIUM(5, "aluminium", 2),
    NICKEL(6, "nickel", 2),
    SILVER(7, "silver", 2),
    LEAD(8, "lead", 5),
    BRONZE(9, "bronze", 2),
    CONSTANTAN(10, "constantan", 2),
    STEEL(11, "steel", 3),
    ELECTRUM(12, "electrum", 2),
    //INVAR?

    //Thermal Series
    PLATINUM(13, "platinum", 4),
    IRIDIUM(14, "iridium", 5),
    SIGNALUM(15, "signalum", 3),
    LUMIUM(16, "lumium", 1),

    //Ender IO
    DARK_STEEL(17, "dark_steel", 4),
    END_STEEL(18, "end_steel", 5),

    //Nuclear Craft
    TOUGH_ALLOY(19, "tough_alloy", 4),

    //Tinkers Construct
    COBALT(20, "cobalt", 3),
    ARDITE(21, "ardite", 3),
    MANYULLIN(22, "manyullin", 4),

    //Mekanism
    OSMIUM(23, "osmium", 3),

    //Psi
    PSIMETAL(24, "psimetal", 3);

    private String name;
    private int id, stiffness;

    /**
     * @param id
     * @param name
     * @param stiffness used for energy processing costs in processing; range [0, 5]
     */
    EnumRobotMaterial(int id, String name, int stiffness) {
        this.id = id;
        this.name = name;
        this.stiffness = stiffness;
    }

    public String getName() {
        return this.name;
    }

    public int getID() {
        return this.id;
    }

    public int getStiffness() {
        return this.stiffness;
    }

    public String getMaterialOreName() {
        return "ingot" + name.substring(0, 1).toUpperCase() + name.substring(1);
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
