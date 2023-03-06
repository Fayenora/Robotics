package com.ignis.igrobotics.core.util;

import com.ignis.igrobotics.Robotics;
import net.minecraft.network.chat.Component;

public class Lang {

    public static Component localise(String s, Object... args) {
        return Component.translatable(Robotics.MODID + "." + s, args);
    }

    public static Component localiseExisting(String s, Object... args) {
        return Component.translatable(s, args);
    }
}
