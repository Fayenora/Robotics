package com.io.norabotics.common.helpers.util;

import com.io.norabotics.Robotics;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;

import java.util.Collection;
import java.util.List;

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
        Object[] objects = new Object[args.length];
        for(int i = 0; i < objects.length; i++) {
            if(args[i] instanceof MobEffectInstance effectInstance) {
                objects[i] = effectToString(effectInstance);
                continue;
            }
            objects[i] = args[i];
        }
        return objects;
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

    public static Component localiseAll(Collection<?> toLocalise) {
        Component listing = ComponentUtils.formatList(toLocalise.stream()
                        .map(Object::toString).map(Lang::localise).toList(),
                Component.literal(", "));
        return ComponentUtils.formatList(List.of(
                        Component.literal("["),
                        listing,
                        Component.literal("]")
                ), Component.empty());
    }
}
