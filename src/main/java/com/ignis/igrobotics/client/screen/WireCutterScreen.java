package com.ignis.igrobotics.client.screen;

import com.ignis.igrobotics.Reference;
import com.ignis.igrobotics.Robotics;
import com.ignis.igrobotics.client.screen.base.BaseContainerScreen;
import com.ignis.igrobotics.client.screen.elements.ArrowElement;
import com.ignis.igrobotics.client.screen.elements.ButtonElement;
import com.ignis.igrobotics.client.screen.elements.EnergyBarElement;
import com.ignis.igrobotics.common.content.menu.WireCutterMenu;
import com.ignis.igrobotics.common.helpers.util.Lang;
import com.ignis.igrobotics.network.messages.NetworkInfo;
import com.ignis.igrobotics.network.messages.server.PacketComponentAction;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

import javax.annotation.ParametersAreNonnullByDefault;
import java.awt.*;

@ParametersAreNonnullByDefault
public class WireCutterScreen extends BaseContainerScreen<WireCutterMenu> {

    public static final ResourceLocation TEXTURE = new ResourceLocation(Robotics.MODID, "textures/gui/wire_cutter.png");
    public static final Rectangle energy_bar = new Rectangle(14, 12, 13, 61);
    public static final Rectangle arrow = new Rectangle(87, 36, 22, 15);

    public WireCutterScreen(WireCutterMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
    }

    @Override
    protected void init() {
        super.init();
        menu.blockEntity.getCapability(ForgeCapabilities.ENERGY).ifPresent(energyStorage -> {
            addElement(new EnergyBarElement(energyStorage, leftPos + energy_bar.x, topPos + energy_bar.y, energy_bar.height));
        });
        ButtonElement muteButton = new ButtonElement(leftPos + imageWidth - 30, topPos + 9, 17, 17, menu.blockEntity.isMuffled() ? 1 : 0, 2) {
            @Override
            public void onPress() {
                menu.blockEntity.nextMuffleState();
                super.onPress();
            }
        };
        muteButton.initTextureLocation(Reference.MISC, 51, 119);
        muteButton.setTooltip(0, Lang.localise("button.mute"));
        muteButton.setTooltip(1, Lang.localise("button.unmute"));
        muteButton.setNetworkAction(() -> new PacketComponentAction(PacketComponentAction.ACTION_MUTE_STATE, new NetworkInfo(menu.blockEntity.getBlockPos())));
        addElement(muteButton);
        addRenderableOnly(new ArrowElement(leftPos + arrow.x, topPos + arrow.y, Direction.EAST, menu.blockEntity::getMachineProgress));
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float p_97788_, int p_97789_, int p_97790_) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        graphics.setColor(1, 1, 1, 1);
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, delta);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int p_97809_, int p_97810_) {
        //Don't
    }
}
