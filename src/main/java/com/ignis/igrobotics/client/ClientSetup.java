package com.ignis.igrobotics.client;

import com.ignis.igrobotics.Reference;
import com.ignis.igrobotics.Robotics;
import com.ignis.igrobotics.client.rendering.RobotFactoryRenderer;
import com.ignis.igrobotics.client.rendering.RobotRenderer;
import com.ignis.igrobotics.client.rendering.RobotStorageRenderer;
import com.ignis.igrobotics.client.rendering.StompedBlockRenderer;
import com.ignis.igrobotics.client.screen.*;
import com.ignis.igrobotics.definitions.ModEntityTypes;
import com.ignis.igrobotics.definitions.ModMachines;
import com.ignis.igrobotics.definitions.ModMenuTypes;
import com.ignis.igrobotics.integration.cc.ProgrammingScreen;
import com.ignis.igrobotics.integration.cc.vanilla.VProgrammingScreen;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@Mod.EventBusSubscriber(modid = Robotics.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientSetup {

    public static final ShaderTracker SHADER_SHIELD = new ShaderTracker();

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(ModMenuTypes.WIRE_CUTTER.get(), WireCutterScreen::new);
            MenuScreens.register(ModMenuTypes.ASSEMBLER.get(), AssemblerScreen::new);
            MenuScreens.register(ModMenuTypes.FACTORY.get(), FactoryScreen::new);
            MenuScreens.register(ModMenuTypes.FACTORY_MODULES.get(), FactoryModulesScreen::new);
            MenuScreens.register(ModMenuTypes.STORAGE.get(), StorageScreen::new);
            MenuScreens.register(ModMenuTypes.ROBOT.get(), RobotScreen::new);
            MenuScreens.register(ModMenuTypes.ROBOT_INFO.get(), RobotInfoScreen::new);
            MenuScreens.register(ModMenuTypes.ROBOT_COMMANDS.get(), RobotCommandScreen::new);
            MenuScreens.register(ModMenuTypes.COMMANDER.get(), CommanderScreen::new);
            //This is horrendous. Please look away
            if(ModList.get().isLoaded(Reference.CC_MOD_ID)) {
                MenuScreens.register(ModMenuTypes.COMPUTER.get(), new MenuScreens.ScreenConstructor() {
                    @Override
                    public AbstractContainerScreen<?> create(AbstractContainerMenu menu, Inventory inv, Component title) {
                        return new ProgrammingScreen(menu, inv, title);
                    }
                });
            } else {
                MenuScreens.register(ModMenuTypes.COMPUTER.get(), VProgrammingScreen::new);
            }
        });
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        EntityRenderers.register(ModEntityTypes.ROBOT.get(), RobotRenderer::new);
        EntityRenderers.register(ModEntityTypes.STOMPED_BLOCK.get(), StompedBlockRenderer::new);
        event.registerBlockEntityRenderer(ModMachines.ROBOT_STORAGE.getBlockEntityType(), RobotStorageRenderer::new);
        event.registerBlockEntityRenderer(ModMachines.ROBOT_FACTORY.getBlockEntityType(), RobotFactoryRenderer::new);
    }

    @SubscribeEvent
    public static void registerShaders(RegisterShadersEvent event) throws IOException {
        event.registerShader(new ShaderInstance(event.getResourceProvider(), new ResourceLocation(Robotics.MODID, "shield"), DefaultVertexFormat.POSITION_TEX_COLOR), SHADER_SHIELD::setInstance);
    }

    public static class ShaderTracker implements Supplier<ShaderInstance> {

        private ShaderInstance instance;
        final RenderStateShard.ShaderStateShard shard = new RenderStateShard.ShaderStateShard(this);

        private ShaderTracker() {
        }

        private void setInstance(ShaderInstance instance) {
            this.instance = instance;
        }

        @Override
        public ShaderInstance get() {
            return instance;
        }
    }
}
