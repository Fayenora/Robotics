package com.ignis.norabotics.integration.cc;

import com.ignis.norabotics.Robotics;
import com.ignis.norabotics.client.screen.base.IElement;
import com.ignis.norabotics.client.screen.elements.SideBarSwitchElement;
import com.ignis.norabotics.common.handlers.RobotBehavior;
import com.ignis.norabotics.definitions.ModMenuTypes;
import com.mojang.blaze3d.systems.RenderSystem;
import dan200.computercraft.client.gui.AbstractComputerScreen;
import dan200.computercraft.client.gui.widgets.TerminalWidget;
import dan200.computercraft.shared.computer.inventory.AbstractComputerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ProgrammingScreen extends AbstractComputerScreen<ProgrammingMenu> {

    public static final ResourceLocation TEXTURE = new ResourceLocation(Robotics.MODID, "textures/gui/computer.png");

    private final LivingEntity entity;

    public ProgrammingScreen(AbstractContainerMenu menu, Inventory playerInv, Component title) {
        super((ProgrammingMenu) menu, playerInv, title, 12);
        entity = ((ProgrammingMenu) menu).robot;
        imageWidth = 256 + AbstractComputerMenu.SIDEBAR_WIDTH;
        imageHeight = 139;
    }

    @Override
    protected void init() {
        super.init();
        SideBarSwitchElement sidebar = new SideBarSwitchElement(ModMenuTypes.COMPUTER.get(), RobotBehavior.possibleMenus(entity), menu.access, leftPos + imageWidth - 1, topPos + 3, 18, 17, entity.getId());
        sidebar.initTextureLocation(SideBarSwitchElement.DEFAULT_TEXTURE);
        addRenderableWidget(sidebar);
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics graphics, float p_97788_, int p_97789_, int p_97790_) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);

        graphics.setColor(1, 1, 1, 1);
        graphics.blit(TEXTURE, leftPos, topPos + sidebarYOffset, 48, 0, 17, 40);
        graphics.blit(TEXTURE, leftPos + AbstractComputerMenu.SIDEBAR_WIDTH, topPos, 0, 117, imageWidth - AbstractComputerMenu.SIDEBAR_WIDTH, imageHeight);
    }

    @Override
    protected TerminalWidget createTerminal() {
        return new TerminalWidget(terminalData, input, leftPos + 9 + AbstractComputerMenu.SIDEBAR_WIDTH, topPos + 9);
    }

    public List<Rect2i> getBlockingAreas() {
        List<Rect2i> blockedAreas = new ArrayList<>();
        for(var comp : children()) {
            if(comp instanceof IElement element) {
                blockedAreas.addAll(element.getBlockingAreas());
            }
        }
        return blockedAreas;
    }
}
