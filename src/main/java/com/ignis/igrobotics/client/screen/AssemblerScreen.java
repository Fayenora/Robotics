package com.ignis.igrobotics.client.screen;

import com.ignis.igrobotics.Reference;
import com.ignis.igrobotics.Robotics;
import com.ignis.igrobotics.client.screen.base.BaseContainerScreen;
import com.ignis.igrobotics.client.screen.elements.ArrowElement;
import com.ignis.igrobotics.client.screen.elements.ButtonElement;
import com.ignis.igrobotics.client.screen.elements.EnergyBarElement;
import com.ignis.igrobotics.common.content.menu.AssemblerMenu;
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
public class AssemblerScreen extends BaseContainerScreen<AssemblerMenu> {

    public static final ResourceLocation TEXTURE = new ResourceLocation(Robotics.MODID, "textures/gui/assembler.png");
    public static final Rectangle energy_bar = new Rectangle(14, 13, 13, 109);
    public static final Rectangle arr_down = new Rectangle(81, 32, 15, 22);
    public static final Rectangle arr_up = new Rectangle(81, 80, 16, 23);
    public static final Rectangle arr_right = new Rectangle(101, 60, 23, 16);
    public static final Rectangle arr_left = new Rectangle(53, 60, 22, 15);

    public AssemblerScreen(AssemblerMenu menu, Inventory inv, Component comp) {
        super(menu, inv, comp);
        imageHeight = 216;
    }

    @Override
    protected void init() {
        super.init();
        addRenderableOnly(new ArrowElement(leftPos + arr_down.x, topPos + arr_down.y, Direction.DOWN, () -> getMachineProgress(Direction.DOWN)));
        addRenderableOnly(new ArrowElement(leftPos + arr_up.x, topPos + arr_up.y, Direction.UP, () -> getMachineProgress(Direction.UP)));
        addRenderableOnly(new ArrowElement(leftPos + arr_right.x, topPos + arr_right.y, Direction.WEST, () -> getMachineProgress(Direction.WEST)));
        addRenderableOnly(new ArrowElement(leftPos + arr_left.x, topPos + arr_left.y, Direction.EAST, () -> getMachineProgress(Direction.EAST)));
        menu.blockEntity.getCapability(ForgeCapabilities.ENERGY).ifPresent(energy -> {
            addElement(new EnergyBarElement(energy, leftPos + energy_bar.x, topPos + energy_bar.y, energy_bar.height));
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
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float p_97788_, int p_97789_, int p_97790_) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
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

    private float getMachineProgress(Direction dir) {
        return menu.blockEntity.isArrowActive(dir) ? menu.blockEntity.getMachineProgress() : 0;
    }
}
