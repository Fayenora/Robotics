package com.ignis.igrobotics.common.robot;

public enum EnumRobotPart {
    HEAD("head"),
    BODY("body"),
    LEFT_ARM("left_arm"),
    RIGHT_ARM("right_arm"),
    LEFT_LEG("left_leg"),
    RIGHT_LEG("right_leg");

    private final String name;

    EnumRobotPart(String name) {
        this.name = name;
    }

    public int getID() {
        return this.ordinal();
    }

    public static EnumRobotPart byId(int id) {
        return values()[id];
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    public EnumModuleSlot toModuleSlot() {
        return switch (this) {
            case BODY -> EnumModuleSlot.BODY;
            case HEAD -> EnumModuleSlot.HEAD;
            case LEFT_ARM -> EnumModuleSlot.LEFT_ARM;
            case LEFT_LEG -> EnumModuleSlot.LEFT_LEG;
            case RIGHT_ARM -> EnumModuleSlot.RIGHT_ARM;
            case RIGHT_LEG -> EnumModuleSlot.RIGHT_LEG;
        };
    }
}
