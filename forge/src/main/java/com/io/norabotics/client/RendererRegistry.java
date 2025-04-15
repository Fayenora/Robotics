package com.io.norabotics.client;

import com.io.norabotics.client.rendering.MachineArmRenderer;
import com.io.norabotics.definitions.ModMachines;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "norabotics", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class RendererRegistry {
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModMachines.MACHINE_ARM.get(), MachineArmRenderer::new);
    }
}
