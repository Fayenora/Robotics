package com.ignis.igrobotics.common.capabilities;

import com.ignis.igrobotics.common.helpers.util.Lang;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.contents.LiteralContents;

public class CommandApplyException extends RuntimeException {

    private final String unlocalized_message;

    public CommandApplyException(String unlocalized_msg, Object... args) {
        super(Lang.localise(unlocalized_msg, args).toString());
        this.unlocalized_message = unlocalized_msg;
    }

    public MutableComponent getErrorMessage() {
        MutableComponent msg = MutableComponent.create(new LiteralContents("CommandApplyException: "));
        msg.append(Lang.localise(unlocalized_message));
        return msg.setStyle(msg.getStyle().withColor(TextColor.fromLegacyFormat(ChatFormatting.RED)));
    }
}
