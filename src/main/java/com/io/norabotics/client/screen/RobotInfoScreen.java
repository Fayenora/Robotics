package com.io.norabotics.client.screen;

import com.io.norabotics.Reference;
import com.io.norabotics.Robotics;
import com.io.norabotics.client.screen.base.IElement;
import com.io.norabotics.client.screen.elements.*;
import com.io.norabotics.client.screen.selectors.EntitySelector;
import com.io.norabotics.client.screen.selectors.SelectorElement;
import com.io.norabotics.common.access.AccessConfig;
import com.io.norabotics.common.access.EnumPermission;
import com.io.norabotics.common.access.WorldAccessData;
import com.io.norabotics.common.capabilities.IRobot;
import com.io.norabotics.common.capabilities.ModCapabilities;
import com.io.norabotics.common.content.menu.RobotInfoMenu;
import com.io.norabotics.common.handlers.RobotBehavior;
import com.io.norabotics.common.helpers.types.Selection;
import com.io.norabotics.common.helpers.util.Lang;
import com.io.norabotics.common.helpers.util.RenderUtil;
import com.io.norabotics.definitions.ModAttributes;
import com.io.norabotics.definitions.ModMenuTypes;
import com.io.norabotics.integration.config.RoboticsConfig;
import com.io.norabotics.network.NetworkHandler;
import com.io.norabotics.network.messages.NetworkInfo;
import com.io.norabotics.network.messages.server.PacketComponentAction;
import com.io.norabotics.network.messages.server.PacketSetEntityName;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class RobotInfoScreen extends EffectRenderingRobotScreen<RobotInfoMenu> {

    public static final ResourceLocation TEXTURE = new ResourceLocation(Robotics.MODID, "textures/gui/robot_info.png");
    private static final Component OWNER_CAPTION = ComponentUtils.formatList(List.of(Component.translatable("owner"), Component.literal(":")), CommonComponents.EMPTY);
    public static final List<Attribute> ATTRIBUTES_TO_EXCLUDE = new ArrayList<>();

    static {
        ATTRIBUTES_TO_EXCLUDE.add(ForgeMod.NAMETAG_DISTANCE.get());
        ATTRIBUTES_TO_EXCLUDE.add(Attributes.FOLLOW_RANGE);
        ATTRIBUTES_TO_EXCLUDE.addAll(ModAttributes.MODIFIER_SLOTS.values());
    }

    private final LivingEntity entity;
    private final AccessConfig access;
    private IRobot robot;

    public ButtonElement pickUpButton;
    public ButtonElement chunkLoadingToggle;
    public ButtonElement soundToggle;
    public ButtonElement powerButton;
    public ButtonElement permissionConfig;
    public EditBox nameBar;
    public GuiSelectorOwner ownerSelector;
    public ButtonElement claimButton;

    public RobotInfoScreen(RobotInfoMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, menu.robot, title);
        this.entity = menu.robot;
        entity.getCapability(ModCapabilities.ROBOT).ifPresent(robot -> this.robot = robot);
        this.access = menu.access;
        imageWidth = Reference.GUI_COMMANDER_DIMENSIONS.width;
        imageHeight = Reference.GUI_COMMANDER_DIMENSIONS.height;
    }

    @Override
    protected void init() {
        super.init();
        SideBarSwitchElement sidebar = new SideBarSwitchElement(ModMenuTypes.ROBOT_INFO.get(), RobotBehavior.possibleMenus(entity), access, leftPos + imageWidth - 1, topPos + 3, 18, 17, entity.getId());
        sidebar.initTextureLocation(SideBarSwitchElement.DEFAULT_TEXTURE);
        addElement(sidebar);
        entity.getCapability(ForgeCapabilities.ENERGY).ifPresent(energy -> {
            addElement(new EnergyBarElement(energy, leftPos + 155, topPos + 8, 165));
        });

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
            soundToggle.setTooltip(0, Lang.localise("button.mute"));
            soundToggle.setTooltip(1, Lang.localise("button.unmute"));
            soundToggle.setNetworkAction(() -> new PacketComponentAction(PacketComponentAction.ACTION_MUTE_STATE, new NetworkInfo(entity)));
            this.powerButton = new ButtonElement(leftPos + 135, topPos + 70, 17, 17, robot.isActive() ? 1 : 0, 2);
            powerButton.initTextureLocation(Reference.MISC, 0, 204);
            powerButton.setTooltip(0, Lang.localise("button.power.up"));
            powerButton.setTooltip(1, Lang.localise("button.power.down"));
            powerButton.setNetworkAction(() -> new PacketComponentAction(PacketComponentAction.ACTION_POWER_STATE, new NetworkInfo(entity)));
            this.permissionConfig = new ButtonElement(leftPos + 135, topPos + 91, 17, 17, button -> addSubGui(new PermissionsScreen(entity, access, 0, 0)));
            permissionConfig.setTooltip(Lang.localise("button.config"));
            permissionConfig.initTextureLocation(Reference.MISC, 51, 153);
            this.nameBar = new EditBox(Minecraft.getInstance().font, leftPos + 77, topPos + 12, 50, 16, Component.literal("text_box"));
            if(entity.hasCustomName()) nameBar.insertText(entity.getName().getString());
            nameBar.setMaxLength(Reference.MAX_ROBOT_NAME_LENGTH);

            if(access.hasOwner()) {
                this.ownerSelector = new GuiSelectorOwner(access.getOwner(), leftPos + 113, topPos + 150);
                addElement(ownerSelector);
            } else {
                claimButton = new ButtonElement(leftPos + 76, topPos + 150, 54, 19, Lang.localise("button.claim"), button -> {
                    access.setOwner(Robotics.proxy.getPlayer().getUUID());
                    RobotBehavior.setAccess(WorldAccessData.EnumAccessScope.ROBOT, entity, access);
                    //Reinitialize the gui to get access to buttons / remove the claim button
                    clearWidgets();
                    init();
                });
                claimButton.initTextureLocation(Reference.MISC, 94, 34);
                addElement(claimButton);
            }

            addElement(pickUpButton);
            addElement(chunkLoadingToggle);
            addElement(soundToggle);
            addElement(powerButton);
            addElement(permissionConfig);
            addElement(nameBar);
        });

        ScrollableElement attributeBar = new ScrollableElement(leftPos + 4, topPos + 5, 67, 170);
        for(Attribute attribute : menu.attributes.keySet()) {
            if(ATTRIBUTES_TO_EXCLUDE.contains(attribute)) continue;
            attributeBar.addElement(new AttributeElement(0, 0, attribute, menu.attributes.get(attribute)));
        }
        addElement(attributeBar);

        updateButtonsEnabled();
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float pPartialTick, int pMouseX, int pMouseY) {
        if(entity == null) return;
        updateButtonsEnabled();

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        graphics.setColor(1, 1, 1, 1);
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        if(access.hasOwner()) {
            //Draw a String declaring the owner
            graphics.drawString(Minecraft.getInstance().font, OWNER_CAPTION, leftPos + 78, topPos + 154, Reference.FONT_COLOR);

            //If the owner changed, ask for confirmation
            if(ownerSelector != null && ownerSelector.getOwner() != null && !hasSubGui()) {
                LivingEntity currentSelection = ownerSelector.getOwner();
                if(!access.getOwner().equals(currentSelection.getUUID())) {
                    createConfirmationDialog(currentSelection);
                }
            }
        }

        int entity_size = 55;
        if(!hasSubGui()) {
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            if(robot.isActive()) {
                RenderUtil.drawEntityOnScreen(graphics, leftPos + (imageWidth / 2) + 15, topPos + (imageHeight / 2) + entity_size, entity_size, 0, 0, entity, false);
            } else {
                RenderUtil.drawInactiveRobot(graphics, leftPos + (imageWidth / 2) - 32, topPos + 22, entity_size, entity, false);
            }
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
            switch (keyCode) {
                case InputConstants.KEY_ESCAPE -> {
                    return super.keyPressed(keyCode, pScanCode, pModifiers);
                }
                case InputConstants.KEY_RETURN -> {
                    nameBar.setFocused(false);
                    renameRobot();
                    return true;
                }
                default -> {
                    return nameBar.keyPressed(keyCode, pScanCode, pModifiers);
                }
            }

        }
        return super.keyPressed(keyCode, pScanCode, pModifiers);
    }

    @Override
    public void onClose() {
        super.onClose();
        renameRobot();
    }

    private void updateButtonsEnabled() {
        Player player = Robotics.proxy.getPlayer();
        entity.getCapability(ModCapabilities.ROBOT).ifPresent(robot -> {
            boolean configureButtonsActive = access.hasPermission(player, EnumPermission.CONFIGURATION);
            configureButtonsActive = configureButtonsActive && (!RoboticsConfig.general.configShutdown.get() || robot.isActive());
            boolean pickUpToggleAble = robot.isActive() || !RoboticsConfig.general.pickUpShutdown.get();
            boolean chunkLoadingToggleable = robot.isActive() || !RoboticsConfig.general.chunkLoadShutdown.get();
            pickUpButton.setEnabled(configureButtonsActive && pickUpToggleAble);
            chunkLoadingToggle.setEnabled(configureButtonsActive && chunkLoadingToggleable);
            soundToggle.setEnabled(configureButtonsActive);
        });
        permissionConfig.setEnabled(access.hasPermission(player, EnumPermission.ALLY));
        powerButton.setEnabled(access.hasPermission(player, EnumPermission.COMMANDS) &&
                entity.getCapability(ForgeCapabilities.ENERGY).orElse(ModCapabilities.NO_ENERGY).getEnergyStored() > 0);
        nameBar.setEditable(access.hasPermission(player, EnumPermission.COMMANDS));
        if(ownerSelector != null) ownerSelector.setEnabled(access.getOwner().equals(player.getUUID()));
    }

    private void renameRobot() {
        if(entity.isDeadOrDying() || nameBar.getValue().isEmpty()) return;
        if(!nameBar.getValue().equals(entity.getDisplayName().getString())) {
            NetworkHandler.sendToServer(new PacketSetEntityName(entity.getId(), nameBar.getValue()));
        }
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

    private void createConfirmationDialog(LivingEntity currentSelection) {
         DialogElement confirmDialog = new DialogElement(94, 56, Lang.localise("confirm_owner_change", currentSelection.getName())) {
             @Override
             public void onClose() {
                 if(ownerSelector == null || ownerSelector.getOwner() == null) return;
                 if(ownerSelector.getOwner().getUUID().equals(access.getOwner())) return;
                 ownerSelector.setOwner(access.getOwner());
                 super.onClose();
             }
        };
        confirmDialog.initTextureLocation(SelectorElement.TEXTURE, 162, 144);
        ButtonElement confirm = new ButtonElement(0, 0, 22, 22, button -> {
            if(ownerSelector == null || ownerSelector.getOwner() == null) return;
            access.setOwner(ownerSelector.getOwner().getUUID());
            RobotBehavior.setAccess(WorldAccessData.EnumAccessScope.ROBOT, entity, access);
            removeSubGui();
            clearWidgets();
            init(); //Reinitialize the gui to get access to buttons / remove the claim button
        });
        ButtonElement decline = new ButtonElement(0, 0, 22, 22, button -> removeSubGui());
        confirm.initTextureLocation(Reference.MISC, 102, 119);
        decline.initTextureLocation(Reference.MISC, 102, 141);
        confirm.setTooltip(Lang.localise("button.ownership.confirm"));
        decline.setTooltip(Lang.localise("button.ownership.decline"));
        confirmDialog.addElement(confirm);
        confirmDialog.addElement(decline);
        addSubGui(confirmDialog);
    }

    public static class GuiSelectorOwner extends EntitySelector {

        public GuiSelectorOwner(UUID owner, int x, int y) {
            super(Selection.of(owner), x, y);
        }

        protected void setOwner(LivingEntity entity) {
            setSelection(entity.getUUID());
        }

        protected void setOwner(UUID uuid) {
            setSelection(uuid);
        }

        @Nullable
        public LivingEntity getOwner() {
            return cachedEntity;
        }

        @Override
        protected IElement getMaximizedVersion() {
            return new GuiSelectEntity(getPlayers(), this::setOwner);
        }

        private static Collection<LivingEntity> getPlayers() {
            //Ensure a safe connection
            ClientPacketListener connection = Minecraft.getInstance().getConnection();
            if(connection == null || (!Minecraft.getInstance().isLocalServer() && !connection.getConnection().isEncrypted())) return List.of();

            Collection<PlayerInfo> networkInfo = connection.getOnlinePlayers();
            List<LivingEntity> players = new ArrayList<>();

            for(PlayerInfo info : networkInfo) {
                players.add(Robotics.proxy.createFakePlayer(Minecraft.getInstance().level, info.getProfile()).get());
            }

            return players;
        }

    }
}
