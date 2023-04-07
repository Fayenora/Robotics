package com.ignis.igrobotics.client.screen;

import com.ignis.igrobotics.Robotics;
import com.ignis.igrobotics.client.menu.AssemblerMenu;
import com.ignis.igrobotics.client.screen.base.BaseContainerScreen;
import com.ignis.igrobotics.client.screen.elements.EnergyBarElement;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.awt.*;

public class AssemblerScreen extends BaseContainerScreen<AssemblerMenu> {

    public static final ResourceLocation TEXTURE = new ResourceLocation(Robotics.MODID, "textures/gui/assembler.png");
    public static final Rectangle energy_bar = new Rectangle(14, 13, 13, 109);
    public static final Rectangle arr_down = new Rectangle(74, 28, 15, 22);
    public static final Rectangle arr_up = new Rectangle(74, 76, 16, 23);
    public static final Rectangle arr_right = new Rectangle(94, 56, 23, 16);
    public static final Rectangle arr_left = new Rectangle(46, 56, 22, 15);

    public AssemblerScreen(AssemblerMenu menu, Inventory inv, Component comp) {
        super(menu, inv, comp);
        imageHeight = 216;
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(new EnergyBarElement(leftPos + energy_bar.x, topPos + energy_bar.y, energy_bar.height, () -> menu.data.get(3), () -> menu.data.get(4)));
    }

    @Override
    protected void renderBg(PoseStack poseStack, float p_97788_, int p_97789_, int p_97790_) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        this.blit(poseStack, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, delta);
        renderTooltip(poseStack, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(PoseStack p_97808_, int p_97809_, int p_97810_) {
        //Don't
    }
}
