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
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

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
        addRenderableWidget(new EnergyBarElement(leftPos + 8, topPos + 8, 203, () -> menu.data.get(3), () -> menu.data.get(4)));

        List<MenuType<?>> possibleMenus = List.of(ModMenuTypes.FACTORY.get(), ModMenuTypes.FACTORY_MODULES.get());
        sideBar = new SideBarSwitchElement(ModMenuTypes.FACTORY_MODULES.get(), possibleMenus, leftPos + imageWidth - 1, topPos + 3, 18, 17, factory.getBlockPos());
        sideBar.initTextureLocation(FactoryScreen.TEXTURE, 0, 219);
        addElement(sideBar);
    }

    private void renderModuleSlots(PoseStack poseStack, EnumModuleSlot slotType, int x, int y, boolean reverse) {
        for(int i = 0; i < moduleSlots.getOrDefault(slotType, 0); i++) {
            blit(poseStack, leftPos + x + (reverse ? -22 : 22) * (i % 4), topPos + y + (i > 3 ? 22 : 0), 229, 0, 18, 18);
        }
    }

    @Override
    protected void renderBg(PoseStack poseStack, float p_97788_, int p_97789_, int p_97790_) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        blit(poseStack, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        renderModuleSlots(poseStack, EnumModuleSlot.SENSOR, 26, 16, false);
        renderModuleSlots(poseStack, EnumModuleSlot.REACTOR, 26, 62, false);
        renderModuleSlots(poseStack, EnumModuleSlot.FEET, 26, 108, false);

        renderModuleSlots(poseStack, EnumModuleSlot.SKIN, 185, 16, true);
        renderModuleSlots(poseStack, EnumModuleSlot.FIST, 185, 62, true);
        renderModuleSlots(poseStack, EnumModuleSlot.DEFAULT, 185, 108, true);
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
