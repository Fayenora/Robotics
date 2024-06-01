package com.ignis.igrobotics.client.screen.elements;

import com.ignis.igrobotics.Reference;
import net.minecraft.resources.ResourceLocation;

public class TickBox extends ButtonElement {

    public static final ResourceLocation TICK_ELEMENT = new ResourceLocation("minecraft", "textures/gui/checkbox.png");

    public TickBox(int pX, int pY) {
        super(pX, pY, 15, 15, 0, 2);
        initTextureLocation(Reference.MISC, 211, 53);
    }

    public boolean isTicked() {
        return getState() == 1;
    }
}
