package com.ignis.igrobotics.integration.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;

import java.io.File;

public interface IConfig {

    void load(ForgeConfigSpec.Builder builder);

    void reload();

    String getFileName();

    ForgeConfigSpec getConfigSpec();

    ModConfig.Type getConfigType();
}
