package com.ignis.igrobotics.client.screen;

import com.ignis.igrobotics.Reference;
import com.ignis.igrobotics.Robotics;
import com.ignis.igrobotics.client.menu.FactoryModulesMenu;
import com.ignis.igrobotics.client.screen.base.BaseContainerScreen;
import com.ignis.igrobotics.client.screen.elements.EnergyBarElement;
import com.ignis.igrobotics.client.screen.elements.SideBarSwitchElement;
import com.ignis.igrobotics.common.blockentity.FactoryBlockEntity;
import com.ignis.igrobotics.core.robot.EnumModuleSlot;
import com.ignis.igrobotics.definitions.ModMenuTypes;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

import java.util.List;
import java.util.Map;

public class FactoryModulesScreen extends BaseContainerScreen<FactoryModulesMenu> {

    public static final ResourceLocation TEXTURE = new ResourceLocation(Robotics.MODID, "textures/gui/robot_factory_modules.png");

    FactoryBlockEntity factory;
    public SideBarSwitchElement sideBar;

    private Map<EnumModuleSlot, Integer> moduleSlots;

    public FactoryModulesScreen(FactoryModulesMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
        imageWidth = Reference.GUI_ROBOT_FACTORY_DIMENSIONS.width;
        imageHeight = Reference.GUI_ROBOT_FACTORY_DIMENSIONS.height;
        this.factory = menu.blockEntity;
        this.moduleSlots = menu.getModuleSlots();
    }

    @Override
    protected void init() {
        super.init();
        factory.getCapability(ForgeCapabilities.ENERGY).ifPresent(energy -> {
            addElement(new EnergyBarElement(energy, leftPos + 8, topPos + 8, 203));
        });

        List<MenuType<?>> possibleMenus = List.of(ModMenuTypes.FACTORY.get(), ModMenuTypes.FACTORY_MODULES.get());
        sideBar = new SideBarSwitchElement(ModMenuTypes.FACTORY_MODULES.get(), possibleMenus, leftPos + imageWidth - 1, topPos + 3, 18, 17, factory.getBlockPos());
        sideBar.initTextureLocation(FactoryScreen.TEXTURE, 0, 219);
        addElement(sideBar);
    }

    private void renderModuleSlots(GuiGraphics graphics, EnumModuleSlot slotType, int x, int y, boolean reverse) {
        for(int i = 0; i < moduleSlots.getOrDefault(slotType, 0); i++) {
            graphics.blit(TEXTURE, leftPos + x + (reverse ? -22 : 22) * (i % 4), topPos + y + (i > 3 ? 22 : 0), 229, 0, 18, 18);
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float p_97788_, int p_97789_, int p_97790_) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        renderModuleSlots(graphics, EnumModuleSlot.SENSOR, 26, 16, false);
        renderModuleSlots(graphics, EnumModuleSlot.REACTOR, 26, 62, false);
        renderModuleSlots(graphics, EnumModuleSlot.FEET, 26, 108, false);

        renderModuleSlots(graphics, EnumModuleSlot.SKIN, 185, 16, true);
        renderModuleSlots(graphics, EnumModuleSlot.FIST, 185, 62, true);
        renderModuleSlots(graphics, EnumModuleSlot.CORE, 185, 108, true);
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
