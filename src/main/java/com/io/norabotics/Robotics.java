package com.io.norabotics;

import com.io.norabotics.common.capabilities.impl.perk.Perk;
import com.io.norabotics.common.misc.CountedIngredient;
import com.io.norabotics.common.robot.RobotModule;
import com.io.norabotics.definitions.*;
import com.io.norabotics.definitions.robotics.*;
import com.io.norabotics.integration.config.RoboticsConfig;
import com.io.norabotics.network.NetworkHandler;
import com.io.norabotics.network.proxy.ClientProxy;
import com.io.norabotics.network.proxy.IProxy;
import com.io.norabotics.network.proxy.ServerProxy;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DataPackRegistryEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import org.slf4j.Logger;
import software.bernie.geckolib.GeckoLib;

import java.util.Random;

@Mod(Robotics.MODID)
public class Robotics {

    public static final String MODID = "norabotics";
    public static final String MODNAME = "Robotics";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final Random RANDOM = new Random();

    public static IProxy proxy = DistExecutor.safeRunForDist(() -> ClientProxy::new, () -> ServerProxy::new);
    public static IEventBus EVENT_BUS;

    static {
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> ModSelectionTypes::loadGuis);
    }

    public Robotics() {
        EVENT_BUS = FMLJavaModLoadingContext.get().getModEventBus();

        ModItems.ITEMS.register(EVENT_BUS);
        ModBlocks.BLOCKS.register(EVENT_BUS);
        ModMachines.BLOCK_ENTITIES.register(EVENT_BUS);
        ModMachines.RECIPE_TYPES.register(EVENT_BUS);
        ModMachines.RECIPE_SERIALIZERS.register(EVENT_BUS);
        ModMenuTypes.MENU_TYPES.register(EVENT_BUS);
        ModEntityTypes.ENTITY_TYPES.register(EVENT_BUS);
        ModSounds.SOUNDS.register(EVENT_BUS);
        ModParticles.PARTICLES.register(EVENT_BUS);
        ModMobEffects.EFFECTS.register(EVENT_BUS);
        ModCreativeTabs.CREATIVE_TABS.register(EVENT_BUS);
        ModActions.ACTION_SERIALIZERS.register(EVENT_BUS);
        ModPerks.PERKS.register(EVENT_BUS);
        ModCommands.COMMANDS.register(EVENT_BUS);
        ModLootItemFunctions.LOOT_FUNCTIONS.register(EVENT_BUS);

        RoboticsConfig.registerConfigs(ModLoadingContext.get());

        GeckoLib.initialize();

        EVENT_BUS.addListener(this::commonSetup);
        EVENT_BUS.addListener(this::registerDataPack);
        EVENT_BUS.addListener(this::registerRecipeSerializers);
    }

    @SubscribeEvent
    public void commonSetup(final FMLCommonSetupEvent event) {
        NetworkHandler.registerMessages();
    }

    @SubscribeEvent
    public void registerDataPack(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(ModPerks.KEY, Perk.CODEC, Perk.CODEC);
        event.dataPackRegistry(ModModules.KEY, RobotModule.CODEC, RobotModule.NETWORK_CODEC);
    }

    @SubscribeEvent
    public void registerRecipeSerializers(RegisterEvent event) {
        if (event.getRegistryKey().equals(ForgeRegistries.Keys.RECIPE_SERIALIZERS)) {
            CraftingHelper.register(Robotics.rl("counted"), CountedIngredient.SERIALIZER);
        }
    }

    public static ResourceLocation rl(String path) {
        return new ResourceLocation(MODID, path);
    }

}
