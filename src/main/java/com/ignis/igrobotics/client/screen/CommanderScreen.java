package com.ignis.igrobotics.client.screen;

import com.ignis.igrobotics.Reference;
import com.ignis.igrobotics.Robotics;
import com.ignis.igrobotics.client.menu.CommanderMenu;
import com.ignis.igrobotics.client.screen.base.BaseContainerScreen;
import com.ignis.igrobotics.client.screen.elements.ButtonElement;
import com.ignis.igrobotics.client.screen.elements.RobotElement;
import com.ignis.igrobotics.client.screen.elements.ScrollableElement;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;

public class CommanderScreen extends BaseContainerScreen<CommanderMenu> {

    public static final ResourceLocation TEXTURE = new ResourceLocation(Robotics.MODID, "textures/gui/command_module.png");

    public ScrollableElement robotList;
    public ButtonElement smallList, largeList;

    public CommanderScreen(CommanderMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
        imageWidth = Reference.GUI_COMMANDER_DIMENSIONS.width;
        imageHeight = Reference.GUI_COMMANDER_DIMENSIONS.height;
    }

    @Override
    protected void init() {
        super.init();
        robotList = new ScrollableElement(leftPos + 15, topPos + 25, 147, 131);
        smallList = new ButtonElement(leftPos + 148, topPos + 16, 6, 5, 0, 2, ign -> largeList.nextState());
        largeList = new ButtonElement(leftPos + 156, topPos + 16, 6, 5, 1, 2, ign -> smallList.nextState());
        smallList.initTextureLocation(TEXTURE, 177, 0);
        largeList.initTextureLocation(TEXTURE, 177, 10);

        for(LivingEntity robot : menu.getRobots()) {
            robotList.addElement(new RobotElement(robot, 0, 0));
        }

        addElement(robotList);
        addElement(smallList);
        addElement(largeList);
    }

    @Override
    protected void renderBg(PoseStack poseStack, float pPartialTick, int pMouseX, int pMouseY) {
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
    protected void renderLabels(PoseStack pPoseStack, int pMouseX, int pMouseY) {
        //Just render the Title
        this.font.draw(pPoseStack, this.title, (float)this.titleLabelX, (float)this.titleLabelY, 4210752);
    }
}
