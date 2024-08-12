package com.ignis.igrobotics.client.screen.elements;

import com.ignis.igrobotics.Reference;

public class TickBox extends ButtonElement {

    public TickBox(int pX, int pY) {
        super(pX, pY, 15, 15, 0, 2);
        initTextureLocation(Reference.MISC, 211, 53);
    }

    public boolean isTicked() {
        return getState() == 1;
    }
}
