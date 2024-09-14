package com.ignis.norabotics.common.robot;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public enum EnumModuleSlot implements StringRepresentable {
    HEAD("head", true),
    BODY("body", true),
    LEFT_ARM("left_arm", true),
    RIGHT_ARM("right_arm", true),
    LEFT_LEG("left_leg", true),
    RIGHT_LEG("right_leg", true),
    CORE("core", false),
    FIST("fist", false),
    FEET("feet", false),
    SENSOR("sensor", false),
    SKIN("skin", false),
    REACTOR("reactor", false);

    private final String name;
    private final boolean primary;

    EnumModuleSlot(String name, boolean primary) {
        this.name = name;
        this.primary = primary;
    }

    public static EnumModuleSlot byId(int id) {
        return values()[id];
    }

    public int getId() {
        return ordinal();
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public @NotNull String getSerializedName() {
        return name;
    }

    public static EnumModuleSlot[] primaries() {
        return Arrays.stream(values()).filter(EnumModuleSlot::isPrimary).toList().toArray(new EnumModuleSlot[6]);
    }

    public static EnumModuleSlot[] nonPrimaries() {
        return Arrays.stream(values()).filter(e -> !e.isPrimary()).toList().toArray(new EnumModuleSlot[6]);
    }

    public boolean isPrimary() {
        return primary;
    }
}
