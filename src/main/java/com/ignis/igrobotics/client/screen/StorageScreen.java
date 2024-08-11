package com.ignis.igrobotics.client.screen;

import com.ignis.igrobotics.Reference;
import com.ignis.igrobotics.Robotics;
import com.ignis.igrobotics.client.screen.base.BaseContainerScreen;
import com.ignis.igrobotics.client.screen.elements.ButtonElement;
import com.ignis.igrobotics.client.screen.elements.EnergyBarElement;
import com.ignis.igrobotics.common.content.blockentity.StorageBlockEntity;
import com.ignis.igrobotics.common.content.menu.StorageMenu;
import com.ignis.igrobotics.common.helpers.util.Lang;
import com.ignis.igrobotics.network.messages.NetworkInfo;
import com.ignis.igrobotics.network.messages.server.PacketComponentAction;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class StorageScreen extends BaseContainerScreen<StorageMenu> {

    public static final ResourceLocation TEXTURE = new ResourceLocation(Robotics.MODID, "textures/gui/robot_storage.png");

    private final StorageBlockEntity storage;
    public ButtonElement releaseRobot, dismantleRobot;
    public EnergyBarElement robotEnergy;

    public StorageScreen(StorageMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
        this.storage = menu.blockEntity;
    }

    @Override
    protected void init() {
        super.init();
        storage.getCapability(ForgeCapabilities.ENERGY).ifPresent(energyStorage -> {
            addElement(new EnergyBarElement(energyStorage, leftPos + 8, topPos + 7, 71));
        });
        if(storage.getEntity().isEmpty()) return;
        storage.getEntity().get().getCapability(ForgeCapabilities.ENERGY).ifPresent(energyStorage -> {
            robotEnergy = new EnergyBarElement(energyStorage, leftPos + 155, topPos + 7, 71);
        });
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
    protected void renderBg(GuiGraphics graphics, float pPartialTick, int pMouseX, int pMouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        graphics.setColor(1, 1, 1, 1);
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        if(storage.getEntity().isEmpty() || !(storage.getEntity().get() instanceof LivingEntity living)) {
            removeWidget(robotEnergy);
            removeWidget(releaseRobot);
            removeWidget(dismantleRobot);
            return;
        }

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        InventoryScreen.renderEntityInInventoryFollowsAngle(graphics, leftPos + 126, topPos + 74, 30, 0, 0, living);
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
