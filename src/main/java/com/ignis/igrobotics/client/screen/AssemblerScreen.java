package com.ignis.igrobotics.client.screen;

import com.ignis.igrobotics.Robotics;
import com.ignis.igrobotics.client.menu.AssemblerMenu;
import com.ignis.igrobotics.client.screen.elements.BaseScreen;
import com.ignis.igrobotics.client.screen.elements.EnergyBarElement;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class AssemblerScreen extends BaseScreen<AssemblerMenu> {

    public static final ResourceLocation TEXTURE = new ResourceLocation(Robotics.MODID, "textures/gui/assembler.png");

    public AssemblerScreen(AssemblerMenu menu, Inventory inv, Component comp) {
        super(menu, inv, comp);
        imageHeight = 216;
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(new EnergyBarElement(leftPos + 14, topPos + 13, 109, () -> menu.data.get(3), () -> menu.data.get(4)));
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
