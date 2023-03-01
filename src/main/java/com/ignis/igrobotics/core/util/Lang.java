package com.ignis.igrobotics.core.util;

import net.minecraft.network.chat.Component;

public class Lang {

    public static String localise(String s, Object... args) {
        return Component.translatable(s, args).getString();
    }
}
