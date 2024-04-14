package com.ignis.igrobotics.integration.config;

import com.ignis.igrobotics.core.robot.CommandType;
import com.ignis.igrobotics.core.util.StringUtil;
import com.ignis.igrobotics.definitions.ModCommands;
import com.ignis.igrobotics.definitions.ModMachines;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;

public class GeneralConfig extends BaseConfig {

    public Supplier<Boolean> limbDestruction;
    public Supplier<Double> limbDropChance;
    public Supplier<Boolean> poisonImmunity;
    public Supplier<DyeColor> startColor;
    public Supplier<Long> robotEnergyCapacity, robotBaseConsumption;
    public Supplier<Boolean> configShutdown, pickUpShutdown, chunkLoadShutdown;

    public Supplier<Integer> shieldBaseHealth, shieldActivationCost, shieldPerHealthCost, shieldUpkeep;
    public Supplier<Double> shieldRechargeRate;

    public Supplier<Integer> robotAmountPerPlayerOnServer;

    public Supplier<List<? extends String>> availableCommands;

    public HashMap<String, Supplier<Integer>> energyCapacities;
    public HashMap<String, Supplier<Double>> energyConsumption;
    public HashMap<String, Supplier<Double>> processingSpeed;

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

        robotEnergyCapacity = builder.defineInRange("Energy Capacity", 1000000, 1, Long.MAX_VALUE);
        robotBaseConsumption = builder.defineInRange("Base Energy Consumption Per Tick", 100, Long.MIN_VALUE, Long.MAX_VALUE);

        configShutdown = builder.comment("Whether deactivated robots cease to be configurable").define("Config Shutdown", false);
        pickUpShutdown = builder.comment("Whether deactivated robots are unable to pick up items").define("Pickup Shutdown", true);
        chunkLoadShutdown = builder.comment("Whether deactivated robots cease chunk loading capabilities").define("Chunk Loading Shutdown", true);
        builder.pop();

        builder.push("Modules");

        shieldBaseHealth = builder.comment("Base health when a shield is activated").defineInRange("Shield Base Health", 4, 1, 1000);
        shieldActivationCost = builder.comment("Energy activation cost of shields").defineInRange("Shield Activation Cost", 500000, 0, Integer.MAX_VALUE);
        shieldPerHealthCost = builder.comment("Energy cost of regenerating a half a heart of shield health").defineInRange("Shield Health Cost", 10000, 0, Integer.MAX_VALUE);
        shieldUpkeep = builder.comment("Passive energy cost of maintaining a shield").defineInRange("Shield Upkeep", 10, 0, Integer.MAX_VALUE);
        shieldRechargeRate = builder.comment("How much shield health is recharged every 4 in-game ticks (~200ms)").defineInRange("Shield Recharge", 0.1, 0, 1);

        builder.pop();

        builder.push("Commands");
        availableCommands = builder.comment("Available commands. Note that limiting these does not remove already applied commands!")
                .defineList("Available Commands", ModCommands.COMMAND_TYPES.stream().map(CommandType::toString).toList(), typeString -> {
                    if(!(typeString instanceof String string)) return false;
                    return ModCommands.byName(string) != null;
        });

        builder.pop();

        builder.push("Machines");

        energyCapacities = new HashMap<>();
        energyConsumption = new HashMap<>();
        processingSpeed = new HashMap<>();

        for(RegistryObject<RecipeSerializer<?>> recipe : ModMachines.RECIPE_SERIALIZERS.getEntries()) {
            String name = recipe.getId().getPath();
            energyCapacities.put(name, builder.defineInRange(StringUtil.titleCase(name) + " Energy Capacity", 1000000, 0, Integer.MAX_VALUE));
            energyConsumption.put(name, builder.defineInRange(StringUtil.titleCase(name) + " Energy Consumption", 1d, 0, 1000));
            processingSpeed.put(name, builder.defineInRange(StringUtil.titleCase(name) + " Processing Speed", 1d, 0, 1000));
        }

        builder.pop();

        return builder.build();
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
