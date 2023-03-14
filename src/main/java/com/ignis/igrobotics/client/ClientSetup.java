package com.ignis.igrobotics.client;

import com.ignis.igrobotics.ModMachines;
import com.ignis.igrobotics.Robotics;
import com.ignis.igrobotics.client.entity.RobotRenderer;
import com.ignis.igrobotics.client.menu.ModMenuTypes;
import com.ignis.igrobotics.client.renderer.RenderRobotStorage;
import com.ignis.igrobotics.client.screen.AssemblerScreen;
import com.ignis.igrobotics.client.screen.FactoryScreen;
import com.ignis.igrobotics.client.screen.WireCutterScreen;
import com.ignis.igrobotics.common.entity.ModEntityTypes;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = Robotics.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientSetup {

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(ModMenuTypes.WIRECUTTER_MENU.get(), WireCutterScreen::new);
            MenuScreens.register(ModMenuTypes.ASSEMBLER_MENU.get(), AssemblerScreen::new);
            MenuScreens.register(ModMenuTypes.FACTORY_MENU.get(), FactoryScreen::new);
        });
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        EntityRenderers.register(ModEntityTypes.ROBOT.get(), RobotRenderer::new);
        event.registerBlockEntityRenderer(ModMachines.ROBOT_STORAGE.get(), RenderRobotStorage::new);
    }
}
