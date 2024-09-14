package com.io.norabotics.client.screen.base;

import net.minecraft.resources.ResourceLocation;

public interface IGuiTexturable {

    default void initTextureLocation(ResourceLocation texture) {
        initTextureLocation(texture, 0, 0);
    }

    void initTextureLocation(ResourceLocation texture, int x, int y);
}
