package com.ignis.igrobotics.client.screen;

import com.ignis.igrobotics.Robotics;
import com.ignis.igrobotics.client.menu.ProgrammingMenu;
import com.ignis.igrobotics.client.screen.elements.SideBarSwitchElement;
import com.ignis.igrobotics.common.RobotBehavior;
import com.ignis.igrobotics.definitions.ModMenuTypes;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dan200.computercraft.client.gui.AbstractComputerScreen;
import dan200.computercraft.client.gui.widgets.ComputerSidebar;
import dan200.computercraft.client.gui.widgets.TerminalWidget;
import dan200.computercraft.shared.computer.inventory.AbstractComputerMenu;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class ProgrammingScreen extends AbstractComputerScreen<ProgrammingMenu> {

    public static final ResourceLocation TEXTURE = new ResourceLocation(Robotics.MODID, "textures/gui/computer.png");

    private final LivingEntity entity;

    public ProgrammingScreen(ProgrammingMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title, 12);
        entity = menu.robot;
        imageWidth = 256 + AbstractComputerMenu.SIDEBAR_WIDTH;
        imageHeight = 139;
    }

    @Override
    protected void init() {
        super.init();
        SideBarSwitchElement sidebar = new SideBarSwitchElement(ModMenuTypes.COMPUTER.get(), RobotBehavior.possibleMenus(entity), leftPos + imageWidth - 1, topPos + 3, 18, 17, entity.getId());
        sidebar.initTextureLocation(SideBarSwitchElement.DEFAULT_TEXTURE);
        addRenderableWidget(sidebar);
    }

    @Override
    protected void renderBg(@NotNull PoseStack poseStack, float p_97788_, int p_97789_, int p_97790_) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        blit(poseStack, leftPos + AbstractComputerMenu.SIDEBAR_WIDTH, topPos, 0, 117, imageWidth - AbstractComputerMenu.SIDEBAR_WIDTH, imageHeight);
        ComputerSidebar.renderBackground(poseStack, leftPos, topPos + sidebarYOffset);
    }

    @Override
    protected TerminalWidget createTerminal() {
        return new TerminalWidget(terminalData, input, leftPos + 9 + AbstractComputerMenu.SIDEBAR_WIDTH, topPos + 9);
    }
}
