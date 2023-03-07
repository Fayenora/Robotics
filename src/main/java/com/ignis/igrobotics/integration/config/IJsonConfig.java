package com.ignis.igrobotics.integration.config;

import net.minecraft.network.FriendlyByteBuf;

import java.io.File;

public interface IJsonConfig {

    void load(File file);

    void toNetwork(FriendlyByteBuf buffer);

    void fromNetwork(FriendlyByteBuf buffer);
}
