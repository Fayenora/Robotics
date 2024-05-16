package com.ignis.igrobotics.core.robot;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum EnumModuleSlot implements StringRepresentable {
    HEAD("head"),
    BODY("body"),
    LEFT_ARM("left_arm"),
    RIGHT_ARM("right_arm"),
    LEFT_LEG("left_leg"),
    RIGHT_LEG("right_leg"),
    CORE("core"),
    FIST("fist"),
    FEET("feet"),
    SENSOR("sensor"),
    SKIN("skin"),
    REACTOR("reactor");

    private final String name;

    EnumModuleSlot(String name) {
        this.name = name;
    }

    public int getID() {
        return this.ordinal();
    }

    public static EnumModuleSlot byId(int id) {
        return values()[id];
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public @NotNull String getSerializedName() {
        return name;
    }
}
