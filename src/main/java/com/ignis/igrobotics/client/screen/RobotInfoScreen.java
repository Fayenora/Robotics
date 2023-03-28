package com.ignis.igrobotics.client.screen;

import com.ignis.igrobotics.Reference;
import com.ignis.igrobotics.Robotics;
import com.ignis.igrobotics.client.menu.RobotInfoMenu;
import com.ignis.igrobotics.client.screen.elements.ButtonElement;
import com.ignis.igrobotics.client.screen.elements.EnergyBarElement;
import com.ignis.igrobotics.client.screen.elements.SideBarSwitchElement;
import com.ignis.igrobotics.common.RobotBehavior;
import com.ignis.igrobotics.core.capabilities.ModCapabilities;
import com.ignis.igrobotics.core.util.Lang;
import com.ignis.igrobotics.core.util.RenderUtil;
import com.ignis.igrobotics.definitions.ModMenuTypes;
import com.ignis.igrobotics.network.messages.NetworkHandler;
import com.ignis.igrobotics.network.messages.NetworkInfo;
import com.ignis.igrobotics.network.messages.server.PacketComponentAction;
import com.ignis.igrobotics.network.messages.server.PacketSetEntityName;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;

import java.awt.event.KeyEvent;
import java.util.List;

public class RobotInfoScreen extends EffectRenderingRobotScreen<RobotInfoMenu> {

    public static final ResourceLocation TEXTURE = new ResourceLocation(Robotics.MODID, "textures/gui/robot_info.png");
    private static final Component OWNER_CAPTION = ComponentUtils.formatList(List.of(Component.translatable("owner"), Component.literal(":")), CommonComponents.EMPTY);

    private final LivingEntity entity;

    public ButtonElement pickUpButton;
    public ButtonElement chunkLoadingToggle;
    public ButtonElement soundToggle;
    public ButtonElement powerButton;
    public ButtonElement permissionConfig;
    public EditBox nameBar;
    //public GuiSelectorOwner ownerSelector;
    public ButtonElement claimButton;

    public RobotInfoScreen(RobotInfoMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, menu.robot, title);
        this.entity = menu.robot;
        imageWidth = Reference.GUI_COMMANDER_DIMENSIONS.width;
        imageHeight = Reference.GUI_COMMANDER_DIMENSIONS.height;
    }

    @Override
    protected void init() {
        super.init();
        SideBarSwitchElement sidebar = new SideBarSwitchElement(ModMenuTypes.ROBOT.get(), RobotBehavior.possibleMenus(entity), leftPos + imageWidth - 1, topPos + 3, 18, 17, entity.getId());
        sidebar.initTextureLocation(SideBarSwitchElement.DEFAULT_TEXTURE);
        addRenderableWidget(sidebar);
        addRenderableWidget(new EnergyBarElement(leftPos + 155, topPos + 8, 165, () -> menu.data.get(0), () -> menu.data.get(1)));

        entity.getCapability(ModCapabilities.ROBOT).ifPresent(robot -> {
            this.pickUpButton = new ButtonElement(leftPos + 135, topPos + 7, 17, 17, robot.getPickUpState(), 3);
            pickUpButton.initTextureLocation(Reference.MISC, 0, 0);
            pickUpButton.setTooltip(0, Lang.localise("button.pickup.default"));
            pickUpButton.setTooltip(1, Lang.localise("button.pickup.area"));
            pickUpButton.setTooltip(2, Lang.localise("button.pickup.none"));
            pickUpButton.setNetworkAction(() -> new PacketComponentAction(PacketComponentAction.ACTION_PICKUP_STATE, new NetworkInfo(entity)));
            this.chunkLoadingToggle = new ButtonElement(leftPos + 135, topPos + 28, 17, 17, robot.getChunkLoadingState(), 3);
            chunkLoadingToggle.initTextureLocation(Reference.MISC, 0, 119);
            chunkLoadingToggle.setTooltip(0, Lang.localise("button.chunks.none"));
            chunkLoadingToggle.setTooltip(1, Lang.localise("button.chunks.default"));
            chunkLoadingToggle.setTooltip(2, Lang.localise("button.chunks.area"));
            chunkLoadingToggle.setNetworkAction(() -> new PacketComponentAction(PacketComponentAction.ACTION_CHUNK_LOADING_STATE, new NetworkInfo(entity)));
            this.soundToggle = new ButtonElement(leftPos + 135, topPos + 49, 17, 17, robot.isMuffled() ? 1 : 0, 2);
            soundToggle.initTextureLocation(Reference.MISC, 51, 119);
            soundToggle.setNetworkAction(() -> new PacketComponentAction(PacketComponentAction.ACTION_MUTE_STATE, new NetworkInfo(entity)));
            this.powerButton = new ButtonElement(leftPos + 135, topPos + 70, 17, 17, robot.isActive() ? 1 : 0, 2);
            powerButton.initTextureLocation(Reference.MISC, 0, 204);
            powerButton.setTooltip(0, Lang.localise("button.power.up"));
            powerButton.setTooltip(1, Lang.localise("button.power.down"));
            powerButton.setNetworkAction(() -> new PacketComponentAction(PacketComponentAction.ACTION_POWER_STATE, new NetworkInfo(entity)));
            this.permissionConfig = new ButtonElement(leftPos + 135, topPos + 91, 17, 17);
            permissionConfig.initTextureLocation(Reference.MISC, 51, 153);
            this.nameBar = new EditBox(Minecraft.getInstance().font, leftPos + 77, topPos + 12, 50, 16, Component.literal("text_box"));
            nameBar.insertText(entity.getName().getString());
            nameBar.setMaxLength(Reference.MAX_ROBOT_NAME_LENGTH);

            if(robot.hasOwner()) {
                //We want to initialize a selector which holds the current owner
                //After a preliminary search on the client, ask the server to contact Mojang servers, as the owner may currently not be online
                //TODO
                //this.ownerSelector = new GuiSelectorOwner(robot.getOwner(), guiLeft + 113, guiTop + 150);
                //addRenderableWidget(ownerSelector);
            } else {
                claimButton = new ButtonElement(leftPos + 76, topPos + 150, 54, 19, Lang.localise("button.claim"), button -> {});
                claimButton.initTextureLocation(Reference.MISC, 94, 34);
                addRenderableWidget(claimButton);
            }

            addRenderableWidget(pickUpButton);
            addRenderableWidget(chunkLoadingToggle);
            addRenderableWidget(soundToggle);
            addRenderableWidget(powerButton);
            addRenderableWidget(permissionConfig);
            addRenderableWidget(nameBar);
        });
    }

    @Override
    protected void renderBg(PoseStack poseStack, float pPartialTick, int pMouseX, int pMouseY) {
        if(entity == null) return;
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        this.blit(poseStack, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        entity.getCapability(ModCapabilities.ROBOT).ifPresent(robot -> {
            if(menu.data.get(0) <= 0) {
                robot.setActivation(false);
            }

            if(robot.hasOwner()) {
                //Draw a String declaring the owner
                drawString(poseStack, Minecraft.getInstance().font, OWNER_CAPTION, leftPos + 78, topPos + 154, Reference.FONT_COLOR);

                //If the owner changed, ask for confirmation
                /* TODO
                if(ownerSelector != null && ownerSelector.getSelector().target != null && !hasSubGui()) {
                    EntityLivingBase currentSelection = ownerSelector.getSelector().target;
                    if(!robot.getOwner().equals(currentSelection.getUniqueID())) {
                        createConfirmationDialog(currentSelection);
                    }
                }*/
            }
        });

        int entity_size = 55;
        if(!hasSubGui()) {
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            RenderUtil.drawEntityOnScreen(leftPos + (imageWidth / 2) + 15, topPos + (imageHeight / 2) + entity_size, entity_size, 0, 0, entity);
        }
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if(!nameBar.isFocused()) {
            renameRobot();
        }
        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean keyPressed(int keyCode, int pScanCode, int pModifiers) {
        if(nameBar.isFocused()) {
            switch(keyCode) {
                case 256:
                    return super.keyPressed(keyCode, pScanCode, pModifiers);
                case 257:
                    nameBar.setFocus(false);
                    renameRobot();
                    return true;
                default:
                    return nameBar.keyPressed(keyCode, pScanCode, pModifiers);
            }

        }
        return super.keyPressed(keyCode, pScanCode, pModifiers);
    }

    @Override
    public void onClose() {
        super.onClose();
        renameRobot();
    }

    private void renameRobot() {
        if(entity.isDeadOrDying()) return;
        if(!nameBar.getValue().equals(entity.getDisplayName())) {
            NetworkHandler.sendToServer(new PacketSetEntityName(entity.getId(), nameBar.getValue()));
        }
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
