package com.ignis.igrobotics.client.screen.elements;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class EditBoxInt extends EditBox {

    private final int min, max;

    public EditBoxInt(int pX, int pY, int pWidth, int pHeight, int min, int max) {
        super(Minecraft.getInstance().font, pX, pY, pWidth, pHeight, Component.empty());
        this.min = min;
        this.max = max;
    }

    @Override
    public void setFocused(boolean isFocusedIn) {
        if(!isFocusedIn) {
            keepIntToBounds();
        }
        super.setFocused(isFocusedIn);
    }

    @Override
    public void insertText(@NotNull String text) {
        StringBuilder cleanedText = new StringBuilder();
        for(char c : text.toCharArray()) {
            if((cleanedText.isEmpty() && getCursorPosition() == 0 && (c == '-' || c == '+')) || (c >= '0' && c <= '9')) {
                cleanedText.append(c);
            }
        }
        super.insertText(cleanedText.toString());
    }

    protected void keepIntToBounds() {
        try {
            int in = Integer.parseInt(getValue());
            in = Math.max(min, Math.min(in, max));
            setValue(Integer.toString(in));
        } catch(NumberFormatException e) {
            if(!getValue().isEmpty()) {
                if(getValue().charAt(0) == '-') {
                    setValue(Integer.toString(min));
                } else {
                    setValue(Integer.toString(max));
                }
            }
        }
    }

    public int getIntValue() {
        keepIntToBounds();
        try {
            return Integer.parseInt(getValue());
        } catch(NumberFormatException e) {
            return 0;
        }

    }
}
