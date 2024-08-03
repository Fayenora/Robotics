package com.ignis.igrobotics.common.modules;

import com.ignis.igrobotics.definitions.ModActions;
import com.mojang.serialization.Codec;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.entity.LivingEntity;

public interface IAction {

    IAction NO_ACTION = new IAction() {
        @Override
        public boolean execute(LivingEntity caster, int duration) {
            return true;
        }

        @Override
        public Codec<? extends IAction> codec() {
            return ModActions.NONE.get();
        }
    };

    boolean execute(LivingEntity caster, int duration);

    Codec<? extends IAction> codec();

    default TextColor getColor() {
        return TextColor.fromLegacyFormat(ChatFormatting.WHITE);
    }
}
