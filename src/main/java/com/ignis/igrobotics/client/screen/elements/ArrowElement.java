package com.ignis.igrobotics.client.screen.elements;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.core.Direction;

import java.awt.*;

public class ArrowElement extends GuiElement {

    public ArrowElement(int x, int y, Direction pointTo) {
        super(x, y, dimensionsFromDirection(pointTo).width, dimensionsFromDirection(pointTo).height);
    }

    private static Dimension dimensionsFromDirection(Direction dir) {
        switch(dir) {
            case EAST: return new Dimension(22, 15);
            case NORTH:
            case UP: return new Dimension(16, 23);
            case SOUTH:
            case DOWN: return new Dimension(15, 22);
            default: return new Dimension(23, 16); //Includes west/left-pointing case
        }
    }

    @Override
    public void render(PoseStack p_94669_, int p_94670_, int p_94671_, float p_94672_) {

    }
}
