package com.io.norabotics.client.screen;

import com.io.norabotics.Reference;
import com.io.norabotics.Robotics;
import com.io.norabotics.client.screen.base.GuiElement;
import com.io.norabotics.client.screen.elements.ButtonElement;
import com.io.norabotics.client.screen.elements.ScrollableElement;
import com.io.norabotics.client.screen.selectors.EntitySelector;
import com.io.norabotics.common.access.AccessConfig;
import com.io.norabotics.common.access.EnumPermission;
import com.io.norabotics.common.access.WorldAccessData;
import com.io.norabotics.common.helpers.types.Selection;
import com.io.norabotics.network.NetworkHandler;
import com.io.norabotics.network.messages.server.PacketSetAccessConfig;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

import java.util.EnumSet;
import java.util.UUID;

public class PermissionsScreen extends GuiElement {

    public static final ResourceLocation TEXTURE = new ResourceLocation(Robotics.MODID, "textures/gui/permissions.png");

    protected ScrollableElement playerSelectors;
    protected ScrollableElement playerConfigs;
    protected ScrollableElement removalButtons;
    protected ButtonElement addPlayer;

    protected final LivingEntity entity;
    protected final AccessConfig config;

    public PermissionsScreen(LivingEntity entity, AccessConfig config, int x, int y) {
        super(x, y, 172, 164);
        initTextureLocation(TEXTURE, 0, 0);
        this.entity = entity;
        this.config = config;
        playerSelectors = new ScrollableElement(getX() + 14, getY() + 27, 22, 130) {
            @Override
            protected void setScrollMaxY(int scrollMaxY) {}
        };
        playerConfigs = new ScrollableElement(getX() + 47, getY() + 27, 118, 130) {
            @Override
            protected void setScrollMaxY(int scrollMaxY) {}
        };
        removalButtons = new ScrollableElement(getX() + 7, getY() + 27, 8, 130) {
            @Override
            protected void setScrollMaxY(int scrollMaxY) {}
        };

        //Add an invisible first button to the removal buttons
        removalButtons.addElement(new GuiElement(0, 0, 8, 22));

        addElement(playerSelectors);
        addElement(playerConfigs);
        addElement(removalButtons);

        int row = 0;
        for(UUID player : config.players()) {
            playerSelectors.addElement(new EntitySelectorWrapper(player, row, 22, 22));
            addPermissionButtons(row, config.getPermissions(player));
            row++;
        }

        addPlayer = new ButtonElement(0, 0, 22, 22, button -> {
            if(!(button instanceof ButtonElement buttonElement)) return;
            int newRow = playerSelectors.children().size() - 1;
            addPermissionButtons(newRow, config.getDefaultPermissions());
            playerSelectors.removeComponent(newRow);
            playerSelectors.addElement(new EntitySelectorWrapper(null, newRow, 22, 22));
            playerSelectors.addElement(buttonElement);
        });
        addPlayer.initTextureLocation(Reference.MISC, 102, 163);
        playerSelectors.addElement(addPlayer);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.render(graphics, mouseX, mouseY, delta);

        //Draw Permission Icons
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        for(int i = 0; i < EnumPermission.values().length; i++) {
            graphics.blit(TEXTURE, getX() + 49 + 22 * i, getY() + 7, 238, 18 * i, 18, 18);
        }
    }

    private void addPermissionButtons(int row, EnumSet<EnumPermission> startPermissions) {
        for(EnumPermission permission : EnumPermission.values()) {
            boolean access = startPermissions.contains(permission);

            ButtonElement button = new ButtonElement(0, 0, 22, 22, access ? 0 : 1, 2, b -> {
                if(!(b instanceof ButtonElement buttonElement)) return;
                UUID uuid = getPlayerOfRow(row);
                switch (buttonElement.getState()) {
                    case 0 -> config.addPermission(uuid, permission);
                    case 1 -> config.removePermission(uuid, permission);
                }
            });
            button.initTextureLocation(Reference.MISC, 102, 119);
            playerConfigs.addElement(button);
        }
        if(row == 0) return;
        ButtonElement removeButton = new ButtonElement(0, 0, 7, 22, button -> {
            config.removePlayerPermissions(getPlayerOfRow(row));
            playerSelectors.removeComponent(row);
            for(int i = 0; i < EnumPermission.values().length; i++) {
                playerConfigs.removeComponent(row * EnumPermission.values().length);
            }
            removalButtons.removeComponent(row);
        });
        removeButton.initSingleTextureLocation(TEXTURE, 230, 34);
        removalButtons.addElement(removeButton);
    }

    public UUID getPlayerOfRow(int row) {
        for(GuiEventListener child : playerSelectors.children()) {
            if(child instanceof EntitySelectorWrapper selector && selector.row == row) {
                return selector.getSelection().get();
            }
        }
        Robotics.LOGGER.warn("Did not find Entity Selector in Permission Gui. Did another component get added?");
        return Reference.DEFAULT_UUID;
    }

    //Mouse input is handled, regardless of whether a component is hovered or not. This way, all scroll bars are synced
    @Override
    public boolean mouseScrolled(double p_94686_, double p_94687_, double p_94688_) {
        for(GuiEventListener comp : children()) {
            comp.mouseScrolled(p_94686_, p_94687_, p_94688_);
        }
        return false;
    }

    @Override
    public void onClose() {
        super.onClose();
        if(config == null) return;
        NetworkHandler.sendToServer(new PacketSetAccessConfig(WorldAccessData.EnumAccessScope.ROBOT, entity, config));
    }

    private class EntitySelectorWrapper extends EntitySelector {

        public final int row;

        public EntitySelectorWrapper(UUID selection, int row, int width, int height) {
            super(Selection.of(selection), 0, 0);
            this.row = row;
            this.width = width;
            this.height = height;
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
            int offsetX = (width - 18) / 2;
            int offsetY = (height - 18) / 2;
            int originalWidth = width;
            int originalHeight = height;
            setX(getX() + offsetX);
            setY(getY() + offsetY);
            width = 18;
            height = 18;
            super.renderWidget(graphics, pMouseX, pMouseY, pPartialTick);
            this.width = originalWidth;
            this.height = originalHeight;
            setX(getX() - offsetX);
            setY(getY() - offsetY);
        }
        @Override
        public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
            //Once this selector selected the default UUID it cannot be changed.
            //This causes the first default Selector to be non-interactable
            //The player cannot add any selectors with the default UUID (since it is already in the list), so they shouldn't end up adding a selector they can't interact with
            if(isHovered() && selection.get() != null && selection.get().equals(Reference.DEFAULT_UUID)) return true;
            return super.mouseClicked(pMouseX, pMouseY, pButton);
        }

        @Override
        public void setSelection(UUID uuid) {
            if(isPlayerAlreadySelected(uuid)) return;
            super.setSelection(uuid);
        }

        @Override
        public void receive(LivingEntity entity) {
            if(selection.get() == null && isPlayerAlreadySelected(entity.getUUID())) return;
            super.receive(entity);
        }

        private boolean isPlayerAlreadySelected(UUID uuid) {
            if(uuid == null) return false;
            //TODO: Either navigate the player to the row where the player is selected or select the player twice, syncing entries
            for(int i = 0; i < playerSelectors.children().size() - 1; i++) {
                if(getPlayerOfRow(i) != null && getPlayerOfRow(i).equals(uuid)) {
                    return true;
                }
            }
            return false;
        }
    }
}
