package com.ignis.igrobotics.core.util;

import com.ignis.igrobotics.Robotics;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;

public class Lang {

    public static final Style AQUA = color(TextColor.fromLegacyFormat(ChatFormatting.AQUA));

    public static MutableComponent localise(ResourceLocation loc) {
        return Component.translatable(loc.toString());
    }

    public static MutableComponent localise(String s, Object... args) {
        return Component.translatable(Robotics.MODID + "." + s, transformObjects(args));
    }

    public static MutableComponent localiseExisting(String s, Object... args) {
        return Component.translatable(s, transformObjects(args));
    }

    public static MutableComponent literal(String s, TextColor color) {
        return Component.literal(s).setStyle(Style.EMPTY.withColor(color));
    }

    public static Object[] transformObjects(Object... args) {
        Object[] objs = new Object[args.length];
        for(int i = 0; i < objs.length; i++) {
            if(args[i] instanceof MobEffectInstance effectInstance) {
                objs[i] = effectToString(effectInstance);
                continue;
            }
            objs[i] = args[i];
        }
        return objs;
    }

    public static String effectToString(MobEffectInstance effect) {
        if(effect.getAmplifier() > 0) {
            return Lang.localiseExisting(effect.getDescriptionId()).getString() + " x " + (effect.getAmplifier() + 1) + ", Duration: " + describeDuration(effect);
        }
        return Lang.localiseExisting(effect.getDescriptionId()).getString() + ", Duration: " + describeDuration(effect);
    }

    public static String describeDuration(MobEffectInstance effect) {
        return effect.isInfiniteDuration() ? "infinite" : Integer.toString(effect.getDuration());
    }

    public static Style color(TextColor color) {
        return Component.empty().getStyle().withColor(color);
    }
}
