package com.ignis.igrobotics.integration.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.ignis.igrobotics.Robotics;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.config.ConfigFileTypeHandler;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;
import java.util.function.Function;

/**
 * Custom {@link ModConfig} implementation that allows for rerouting the server config from being in the world's folder to being in the normal config folder. This allows
 * for us to use the builtin sync support, without the extra hassle of having to explain to people where the config file is, or require people in single player to edit
 * the config each time they make a new world.
 * @see <a href="https://github.com/mekanism/Mekanism/blob/ee5707f96155c0306f99b768212cb893d43b60e8/src/main/java/mekanism/common/config/MekanismModConfig.java#L17">Original Class in Mekanism</a>
 */
public class RoboticsModConfig extends ModConfig {

    private static final RoboticsConfigFileTypeHandler ROBOTICS_TOML_HANDLER = new RoboticsConfigFileTypeHandler();

    public RoboticsModConfig(ModContainer container, IConfig config) {
        super(config.getConfigType(), config.getConfigSpec(), container, Robotics.MODNAME + "/" + config.getFileName() + ".toml");
    }

    @Override
    public ConfigFileTypeHandler getHandler() {
        return ROBOTICS_TOML_HANDLER;
    }

    private static class RoboticsConfigFileTypeHandler extends ConfigFileTypeHandler {

        private static Path getPath(Path configBasePath) {
            //Intercept server config path reading for configs and reroute it to the normal config directory
            if (configBasePath.endsWith("serverconfig")) {
                return FMLPaths.CONFIGDIR.get();
            }
            return configBasePath;
        }

        @Override
        public Function<ModConfig, CommentedFileConfig> reader(Path configBasePath) {
            return super.reader(getPath(configBasePath));
        }

        @Override
        public void unload(Path configBasePath, ModConfig config) {
            super.unload(getPath(configBasePath), config);
        }
    }
}
