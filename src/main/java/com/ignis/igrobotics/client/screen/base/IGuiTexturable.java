package com.ignis.igrobotics.client.screen.base;

import net.minecraft.resources.ResourceLocation;

import java.awt.*;

public interface IGuiTexturable {

    default void initTextureLocation(ResourceLocation texture) {
        initTextureLocation(texture, 0, 0);
    }

    void initTextureLocation(ResourceLocation texture, int x, int y);
}
