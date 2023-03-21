package com.ignis.igrobotics.core;

import com.ignis.igrobotics.core.util.Lang;

public class CommandApplyException extends RuntimeException {

    public CommandApplyException(String unlocalized_msg, Object... args) {
        super(Lang.localise(unlocalized_msg, args).toString());
    }
}
