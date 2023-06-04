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
        /* We currently don't have a client config
        if(config != null) {
            config.client = LOCAL.client;
        }
         */
        SERVER = config;
    }

    public static final GeneralConfig general = new GeneralConfig();
    public PerkConfig perks = new PerkConfig();
    public ModuleConfig modules = new ModuleConfig();
    public PartConfig parts = new PartConfig();

    public static void registerConfigs(ModLoadingContext cxt) {
        ModContainer container = cxt.getActiveContainer();
        registerConfig(container, general);
    }

    public void loadJsonConfigs() {
        Path configDir = getOrCreateDirectory(FMLPaths.CONFIGDIR.get().resolve(Robotics.MODNAME));
        perks.load(new File(configDir.toString(), "perks.json"));
        modules.load(new File(configDir.toString(), "robot_modules.json"));
        parts.load(new File(configDir.toString(), "robot_parts.json"));
    }

    private static void registerConfig(ModContainer container, IConfig config) {
        RoboticsModConfig modConfig = new RoboticsModConfig(container, config);
        container.addConfig(modConfig);
    }

    public static Path getOrCreateDirectory(Path dirPath) {
        if (!Files.isDirectory(dirPath)) {
            try {
                Files.createDirectories(dirPath);
            } catch (IOException e) {
                throw new RuntimeException("Problem creating directory", e);
            }
        }
        return dirPath;
    }

}
