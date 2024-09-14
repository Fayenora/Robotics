package com.io.norabotics.integration.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;

public interface IConfig {

    void load(ForgeConfigSpec.Builder builder);

    String getFileName();

    ForgeConfigSpec getConfigSpec();

    ModConfig.Type getConfigType();
}
