package com.ignis.igrobotics.integration.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;

import java.io.File;

public abstract class BaseConfig implements IConfig {

    private ForgeConfigSpec spec;

    public BaseConfig() {
        load(new ForgeConfigSpec.Builder());
    }

    public abstract ForgeConfigSpec define(ForgeConfigSpec.Builder builder);

    @Override
    public void load(ForgeConfigSpec.Builder builder) {
        spec = define(builder);
    }

    @Override
    public ForgeConfigSpec getConfigSpec() {
        return spec;
    }
}
