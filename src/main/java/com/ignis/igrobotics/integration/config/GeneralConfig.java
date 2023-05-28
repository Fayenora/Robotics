package com.ignis.igrobotics.integration.config;

import net.minecraft.world.item.DyeColor;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;

import java.util.function.Supplier;

public class GeneralConfig extends BaseConfig {

    public Supplier<Boolean> limbDestruction;
    public Supplier<Double> limbDropChance;
    public Supplier<Boolean> poisonImmunity;
    public Supplier<DyeColor> startColor;
    public Supplier<Integer> moduleAmount;
    public Supplier<Long> robotEnergyCapacity, robotBaseConsumption;
    public Supplier<Boolean> configShutdown, pickUpShutdown, chunkLoadShutdown;

    public Supplier<Integer> robotAmountPerPlayerOnServer;

    @Override
    public ForgeConfigSpec define(ForgeConfigSpec.Builder builder) {

        builder.push("General");
        robotAmountPerPlayerOnServer = builder.comment("Maximum amount of robots per player on a server. This can be used to manage server resources")
                .defineInRange("Max Robots Per Player", Integer.MAX_VALUE, 0, Integer.MAX_VALUE);
        builder.pop();

        builder.push("Robots");
        limbDestruction = builder.comment("Whether limbs can fall off").define("Limb Destruction", true);
        limbDropChance = builder.comment("Limb drop chance when a robot is destroyed").defineInRange("Drop Chance", 0.4, 0, 1);

        poisonImmunity = builder.define("Poison Immunity", true);

        startColor = builder.comment("The color a robot has when spawned from a spawn egg").defineEnum("Start Color", DyeColor.RED);

        moduleAmount = builder.comment("Base amount of modules in a robot. Note that module slots cannot exceed the maximum of 8, even with other modifiers. This will be extended in the future").
                defineInRange("Max Modules", 2, 0, 8);

        robotEnergyCapacity = builder.defineInRange("Energy Capacity", 1000000, 1, Long.MAX_VALUE);
        robotBaseConsumption = builder.defineInRange("Base Energy Consumption Per Tick", 100, Long.MIN_VALUE, Long.MAX_VALUE);

        configShutdown = builder.comment("Whether deactivated robots cease to be configurable").define("Config Shutdown", false);
        pickUpShutdown = builder.comment("Whether deactivated robots are unable to pick up items").define("Pickup Shutdown", true);
        chunkLoadShutdown = builder.comment("Whether deactivated robots cease chunk loading capabilities").define("Chunk Loading Shutdown", true);
        builder.pop();

        return builder.build();
    }

    @Override
    public void reload() {

    }

    @Override
    public String getFileName() {
        return "robotics";
    }

    @Override
    public ModConfig.Type getConfigType() {
        return ModConfig.Type.SERVER;
    }
}
