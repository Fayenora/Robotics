package com.ignis.igrobotics.client.screen.elements;

import com.ignis.igrobotics.Reference;
import com.ignis.igrobotics.client.screen.base.GuiElement;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.awt.*;
import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public class ArrowElement extends GuiElement {

    private final Direction pointTo;
    private final Supplier<Float> progress;

    public ArrowElement(int x, int y, Direction pointTo, Supplier<Float> progress) {
        super(x, y, dimensionsFromDirection(pointTo).width, dimensionsFromDirection(pointTo).height);
        this.pointTo = pointTo;
        this.progress = progress;
    }

    private static Dimension dimensionsFromDirection(Direction dir) {
        return switch (dir) {
            case EAST -> new Dimension(22, 15);
            case NORTH, UP -> new Dimension(16, 23);
            case SOUTH, DOWN -> new Dimension(15, 22);
            default -> new Dimension(23, 16); //Includes west/left-pointing case
        };
    }

    @Override
    public void render(GuiGraphics graphics, int p_94670_, int p_94671_, float p_94672_) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        int k = (int) (progress.get() * Math.max(width, height));
        int l = (k != 0) ? 1 : 0;

        switch (pointTo) {
            case DOWN, SOUTH -> graphics.blit(Reference.MISC, getX(), getY(), 233, 211, width, k);
            case EAST -> graphics.blit(Reference.MISC, getX(), getY(), 233, 196, k, height);
            case WEST -> {
                k += l;
                graphics.blit(Reference.MISC, getX() + width - k, getY(), 233 + width - k, 180, k, height);
            }
            case UP, NORTH -> {
                k += l;
                graphics.blit(Reference.MISC, getX(), getY() + height - k, 233, 233 + height - k, width, k);
            }
        }
    }
}
