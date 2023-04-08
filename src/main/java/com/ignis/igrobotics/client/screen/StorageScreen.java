package com.ignis.igrobotics.client.screen;

import com.ignis.igrobotics.Reference;
import com.ignis.igrobotics.Robotics;
import com.ignis.igrobotics.client.menu.StorageMenu;
import com.ignis.igrobotics.client.screen.base.BaseContainerScreen;
import com.ignis.igrobotics.client.screen.elements.ButtonElement;
import com.ignis.igrobotics.client.screen.elements.EnergyBarElement;
import com.ignis.igrobotics.common.blockentity.StorageBlockEntity;
import com.ignis.igrobotics.core.util.Lang;
import com.ignis.igrobotics.core.util.RenderUtil;
import com.ignis.igrobotics.network.messages.NetworkInfo;
import com.ignis.igrobotics.network.messages.server.PacketComponentAction;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.awt.*;

public class StorageScreen extends BaseContainerScreen<StorageMenu> {

    public static final ResourceLocation TEXTURE = new ResourceLocation(Robotics.MODID, "textures/gui/robot_storage.png");

    private StorageBlockEntity storage;
    public ButtonElement releaseRobot, dismantleRobot;
    public EnergyBarElement storageEnergy, robotEnergy;

    public StorageScreen(StorageMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
        this.storage = menu.blockEntity;
    }

    @Override
    protected void init() {
        super.init();
        storageEnergy = new EnergyBarElement(leftPos + 8, topPos + 7, 71, () -> menu.data.get(0), () -> menu.data.get(1));
        addElement(storageEnergy);
        if(!storage.containsRobot()) return;
        robotEnergy = new EnergyBarElement(leftPos + 155, topPos + 7, 71, () -> menu.data.get(2), () -> menu.data.get(3));
        releaseRobot = new ButtonElement(leftPos + 55, topPos + 7, 41, 17, Lang.localise("deploy"), button -> {});
        releaseRobot.initTextureLocation(Reference.MISC, 83, 17);
        releaseRobot.setNetworkAction(() -> new PacketComponentAction(PacketComponentAction.ACTION_FACTORY_BUTTON, new NetworkInfo(storage.getBlockPos())));
        dismantleRobot = new ButtonElement(leftPos + 55, topPos + 28, 41, 17, Lang.localise("dismantle"), button -> {});
        dismantleRobot.initTextureLocation(Reference.MISC, 83, 17);
        dismantleRobot.setNetworkAction(() -> new PacketComponentAction(PacketComponentAction.ACTION_DISMANTLE_ROBOT, new NetworkInfo(storage.getBlockPos())));

        addElement(robotEnergy);
        addElement(releaseRobot);
        addElement(dismantleRobot);
    }

    @Override
    protected void renderBg(PoseStack poseStack, float pPartialTick, int pMouseX, int pMouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        this.blit(poseStack, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        if(!storage.containsRobot()) {
            removeWidget(robotEnergy);
            removeWidget(releaseRobot);
            removeWidget(dismantleRobot);
            return;
        }

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        InventoryScreen.renderEntityInInventory(leftPos + 126, topPos + 74, 30, 0, 0, storage.getEntity());
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
