package com.ignis.igrobotics;

import com.ignis.igrobotics.client.menu.ModMenuTypes;
import com.ignis.igrobotics.client.renderer.RenderRobotStorage;
import com.ignis.igrobotics.client.screen.AssemblerScreen;
import com.ignis.igrobotics.client.screen.FactoryScreen;
import com.ignis.igrobotics.client.screen.WireCutterScreen;
import com.ignis.igrobotics.common.blockentity.BlockEntityStorage;
import com.ignis.igrobotics.common.entity.ModEntityTypes;
import com.ignis.igrobotics.integration.config.RoboticsConfig;
import com.ignis.igrobotics.network.messages.NetworkHandler;
import com.mojang.logging.LogUtils;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.client.RenderTypeGroup;
import net.minecraftforge.client.RenderTypeHelper;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.model.renderable.ITextureRenderTypeLookup;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import software.bernie.geckolib.GeckoLib;

@Mod(Robotics.MODID)
public class Robotics {

    public static final String MODID = "igrobotics";
    public static final String MODNAME = "Robotics";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final String VERSION = "1.0";
    public static final String ACCEPTED_VERSIONS = "[1.12.2]";

    public Robotics() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModItems.ITEMS.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);
        ModMachines.BLOCK_ENTITIES.register(modEventBus);
        ModMachines.RECIPE_TYPES.register(modEventBus);
        ModMachines.RECIPE_SERIALIZERS.register(modEventBus);
        ModMenuTypes.MENU_TYPES.register(modEventBus);
        ModEntityTypes.ENTITY_TYPES.register(modEventBus);

        RoboticsConfig.registerConfigs(ModLoadingContext.get());

        GeckoLib.initialize();

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(RoboticsCreativeTab::register);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void commonSetup(final FMLCommonSetupEvent event) {
        NetworkHandler.registerMessages();
    }

    @SubscribeEvent
    public void serverSetup(ServerStartingEvent event) {
        RoboticsConfig.local().loadJsonConfigs();
    }

}
