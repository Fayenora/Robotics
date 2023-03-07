package com.ignis.igrobotics.core.util;

import com.ignis.igrobotics.Robotics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

public class Lang {

    public static MutableComponent localise(String s, Object... args) {
        return Component.translatable(Robotics.MODID + "." + s, args);
    }

    public static MutableComponent localiseExisting(String s, Object... args) {
        return Component.translatable(s, args);
    }

    public static MutableComponent literal(String s, TextColor color) {
        return Component.literal(s).setStyle(Style.EMPTY.withColor(color));
    }
}
