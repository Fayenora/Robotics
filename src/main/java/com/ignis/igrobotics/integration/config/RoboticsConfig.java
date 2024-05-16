package com.ignis.igrobotics.integration.config;

import com.ignis.igrobotics.Robotics;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class RoboticsConfig {

    private static final RoboticsConfig LOCAL = new RoboticsConfig();
    private static RoboticsConfig SERVER = null;

    public static RoboticsConfig current() {
        return SERVER != null ? SERVER : LOCAL;
    }

    public static RoboticsConfig local() {
        return LOCAL;
    }

    public static void receiveConfig(RoboticsConfig config) {
        SERVER = config;
    }

    public static final GeneralConfig general = new GeneralConfig();
    public static final ClientConfig client = new ClientConfig();

    public static void registerConfigs(ModLoadingContext cxt) {
        ModContainer container = cxt.getActiveContainer();
        registerConfig(container, general);
        registerConfig(container, client);
    }

    private static void registerConfig(ModContainer container, IConfig config) {
        RoboticsModConfig modConfig = new RoboticsModConfig(container, config);
        container.addConfig(modConfig);
    }

}
