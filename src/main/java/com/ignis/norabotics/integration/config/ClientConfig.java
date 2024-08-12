package com.ignis.norabotics.integration.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ClientConfig extends BaseConfig {

    public Supplier<Boolean> machineSoundsEnabled;

    public Supplier<List<? extends Integer>> heartColors;
    public Supplier<List<? extends Integer>> armorColors;

    private static final List<Integer> defaultHeartColors = new ArrayList<>();
    private static final List<Integer> defaultArmorColors = new ArrayList<>();

    static {
        defaultHeartColors.add(0xff1313);
        defaultHeartColors.add(0xf2d00a);
        defaultHeartColors.add(0x49d00a);
        defaultArmorColors.add(0xFFFFFF);
        defaultArmorColors.add(0x567def);
    }

    @Override
    public ForgeConfigSpec define(ForgeConfigSpec.Builder builder) {

        machineSoundsEnabled = builder.define("Machine Sounds Enabled", true);

        heartColors = builder.comment("Colors for drawing the hearts in the robot gui for values higher than 10. Loops back around. You can choose colors easily here https://www.mathsisfun.com/hexadecimal-decimal-colors.html (use the decimal form)").
                defineList("Heart Colors", defaultHeartColors, color -> color instanceof Integer integer && integer >= 0 && integer <= 0xFFFFFF);
        armorColors = builder.comment("Colors for drawing the armor in the robot gui for values higher than 10. Loops back around. You can choose colors easily here https://www.mathsisfun.com/hexadecimal-decimal-colors.html (use the decimal form)")
                .defineList("Armor Colors", defaultArmorColors, color -> color instanceof Integer integer && integer >= 0 && integer <= 0xFFFFFF);

        return builder.build();
    }

    @Override
    public String getFileName() {
        return "robotics_client";
    }

    @Override
    public ModConfig.Type getConfigType() {
        return ModConfig.Type.CLIENT;
    }
}
