package com.ignis.igrobotics.client;

import com.ignis.igrobotics.client.screen.*;
import com.ignis.igrobotics.definitions.ModMachines;
import com.ignis.igrobotics.Robotics;
import com.ignis.igrobotics.client.rendering.RobotRenderer;
import com.ignis.igrobotics.definitions.ModMenuTypes;
import com.ignis.igrobotics.client.rendering.RobotStorageRenderer;
import com.ignis.igrobotics.definitions.ModEntityTypes;
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
            MenuScreens.register(ModMenuTypes.WIRE_CUTTER.get(), WireCutterScreen::new);
            MenuScreens.register(ModMenuTypes.ASSEMBLER.get(), AssemblerScreen::new);
            MenuScreens.register(ModMenuTypes.FACTORY.get(), FactoryScreen::new);
            MenuScreens.register(ModMenuTypes.ROBOT.get(), RobotScreen::new);
            MenuScreens.register(ModMenuTypes.ROBOT_INFO.get(), RobotInfoScreen::new);
        });
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        EntityRenderers.register(ModEntityTypes.ROBOT.get(), RobotRenderer::new);
        event.registerBlockEntityRenderer(ModMachines.ROBOT_STORAGE.getBlockEntityType(), RobotStorageRenderer::new);
    }
}
