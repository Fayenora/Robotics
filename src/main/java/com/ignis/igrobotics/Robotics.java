package com.ignis.igrobotics;

import com.ignis.igrobotics.core.CountedIngredient;
import com.ignis.igrobotics.core.capabilities.perks.Perk;
import com.ignis.igrobotics.core.robot.RobotModule;
import com.ignis.igrobotics.core.robot.SelectionType;
import com.ignis.igrobotics.definitions.*;
import com.ignis.igrobotics.integration.config.RoboticsConfig;
import com.ignis.igrobotics.network.NetworkHandler;
import com.ignis.igrobotics.network.proxy.ClientProxy;
import com.ignis.igrobotics.network.proxy.IProxy;
import com.ignis.igrobotics.network.proxy.ServerProxy;
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

    public static final String MODID = "igrobotics";
    public static final String MODNAME = "Robotics";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final Random RANDOM = new Random();

    public static IProxy proxy = DistExecutor.safeRunForDist(() -> ClientProxy::new, () -> ServerProxy::new);

    static {
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> SelectionType::loadGuis);
    }

    public Robotics() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModItems.ITEMS.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);
        ModMachines.BLOCK_ENTITIES.register(modEventBus);
        ModMachines.RECIPE_TYPES.register(modEventBus);
        ModMachines.RECIPE_SERIALIZERS.register(modEventBus);
        ModMenuTypes.MENU_TYPES.register(modEventBus);
        ModEntityTypes.ENTITY_TYPES.register(modEventBus);
        ModSounds.SOUNDS.register(modEventBus);
        ModMobEffects.EFFECTS.register(modEventBus);
        ModCreativeTabs.CREATIVE_TABS.register(modEventBus);
        ModPerks.PERKS.register(modEventBus);

        RoboticsConfig.registerConfigs(ModLoadingContext.get());

        GeckoLib.initialize();

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerDataPack);
        modEventBus.addListener(this::registerRecipeSerializers);
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
