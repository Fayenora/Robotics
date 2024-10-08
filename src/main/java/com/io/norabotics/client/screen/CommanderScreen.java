package com.io.norabotics.client.screen;

import com.io.norabotics.Reference;
import com.io.norabotics.Robotics;
import com.io.norabotics.client.screen.base.BaseContainerScreen;
import com.io.norabotics.client.screen.elements.ButtonElement;
import com.io.norabotics.client.screen.elements.RobotElement;
import com.io.norabotics.client.screen.elements.ScrollableElement;
import com.io.norabotics.common.content.menu.CommanderMenu;
import com.io.norabotics.common.robot.RobotView;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class CommanderScreen extends BaseContainerScreen<CommanderMenu> {

    public static final ResourceLocation TEXTURE = new ResourceLocation(Robotics.MODID, "textures/gui/command_module.png");

    private ButtonElement smallList, largeList;

    public CommanderScreen(CommanderMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
        imageWidth = Reference.GUI_COMMANDER_DIMENSIONS.width;
        imageHeight = Reference.GUI_COMMANDER_DIMENSIONS.height;
    }

    @Override
    protected void init() {
        super.init();
        ScrollableElement robotList = new ScrollableElement(leftPos + 15, topPos + 25, 147, 131);
        smallList = new ButtonElement(leftPos + 148, topPos + 16, 6, 5, 0, 2, ign -> largeList.nextState());
        largeList = new ButtonElement(leftPos + 156, topPos + 16, 6, 5, 1, 2, ign -> smallList.nextState());
        smallList.initTextureLocation(TEXTURE, 177, 0);
        largeList.initTextureLocation(TEXTURE, 177, 10);

        for(RobotView robot : menu.getRobots()) {
            robotList.addElement(new RobotElement(robot, 0, 0));
        }

        addElement(robotList);
        addElement(smallList);
        addElement(largeList);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float pPartialTick, int pMouseX, int pMouseY) {
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
    protected void renderLabels(GuiGraphics graphics, int pMouseX, int pMouseY) {
        //Just render the Title
        graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 4210752, false);
    }
}
