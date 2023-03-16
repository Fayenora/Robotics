package com.ignis.igrobotics.core;

public enum EnumRobotPart {
    HEAD(0, "head"),
    BODY(1, "body"),
    LEFT_ARM(2, "left_arm"),
    RIGHT_ARM(3, "right_arm"),
    LEFT_LEG(4, "left_leg"),
    RIGHT_LEG(5, "right_leg");

    private String name;
    private int id;

    EnumRobotPart(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getID() {
        return id;
    }

    public static EnumRobotPart getByID(int id) {
        return values()[id];
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
