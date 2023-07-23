package com.ignis.igrobotics.integration.cc;

import com.ignis.igrobotics.Robotics;
import com.ignis.igrobotics.client.screen.base.BaseContainerScreen;
import com.ignis.igrobotics.client.screen.elements.SideBarSwitchElement;
import com.ignis.igrobotics.common.RobotBehavior;
import com.ignis.igrobotics.definitions.ModMenuTypes;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class PeripheralScreen extends BaseContainerScreen<PeripheralMenu> {

    public static final ResourceLocation TEXTURE = new ResourceLocation(Robotics.MODID, "textures/gui/peripherals.png");

    public PeripheralScreen(PeripheralMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
    }

    @Override
    protected void init() {
        super.init();
        SideBarSwitchElement sidebar = new SideBarSwitchElement(ModMenuTypes.ROBOT_COMMANDS.get(), RobotBehavior.possibleMenus(menu.entity), leftPos + imageWidth - 1, topPos + 3, 18, 17, menu.entity.getId());
        sidebar.initTextureLocation(SideBarSwitchElement.DEFAULT_TEXTURE);
        addRenderableWidget(sidebar);
    }

    @Override
    protected void renderBg(PoseStack poseStack, float p_97788_, int p_97789_, int p_97790_) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        blit(poseStack, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, delta);
        renderTooltip(poseStack, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(PoseStack pPoseStack, int pMouseX, int pMouseY) {
        //NO-OP
    }
}
