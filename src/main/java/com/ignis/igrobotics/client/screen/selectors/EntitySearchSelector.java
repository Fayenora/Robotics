package com.ignis.igrobotics.client.screen.selectors;

import com.ignis.igrobotics.Reference;
import com.ignis.igrobotics.client.screen.base.GuiElement;
import com.ignis.igrobotics.client.screen.base.IElement;
import com.ignis.igrobotics.client.screen.elements.ButtonElement;
import com.ignis.igrobotics.common.items.CommanderItem;
import com.ignis.igrobotics.core.EntitySearch;
import com.ignis.igrobotics.core.robot.Selection;
import com.ignis.igrobotics.core.util.InventoryUtil;
import com.ignis.igrobotics.core.util.Lang;
import com.ignis.igrobotics.core.util.MathUtil;
import com.ignis.igrobotics.core.util.RenderUtil;
import com.ignis.igrobotics.definitions.ModItems;
import com.ignis.igrobotics.network.NetworkHandler;
import com.ignis.igrobotics.network.messages.IPacketDataReceiver;
import com.ignis.igrobotics.network.messages.server.PacketRequestEntitySearch;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class EntitySearchSelector extends SelectorElement<EntitySearch> implements IPacketDataReceiver {

    private boolean cached;
    private LivingEntity cachedEntity;

    public EntitySearchSelector(Selection<EntitySearch> selection, int x, int y) {
        super(selection, x, y);
    }

    @Override
    protected IElement getMaximizedVersion() {
        return new GuiSelectEntitySearch();
    }

    @Override
    public void renderSelection(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if(selection.get() == null) return;
        if(selection.get().equals(new EntitySearch())) {
            RenderUtil.drawCenteredString(graphics, Component.translatable("none"), getX() + width / 2, getY() + height / 2 - 2, Reference.FONT_COLOR, 0.5f, getWidth() - 2);
            return;
        }
        if(!cached) {
            //TODO: Don't overkill and ask the server instantly, look on the client first
            NetworkHandler.sendToServer(new PacketRequestEntitySearch(getParentGuiPath(), selection.get()));
            return;
        }
        if(cachedEntity != null && currentGuiActive()) {
            RenderUtil.enableScissor(graphics, MathUtil.downsizeRect(getShape(), 1));
            RenderUtil.drawRotatingEntity(graphics.pose(), getX() + width / 2, getY() + height / 2 + 6, (int) (8 / cachedEntity.getBoundingBox().getSize()), cachedEntity, angle);
            RenderUtil.disableScissor(graphics);
        }
    }

    private void setCachedEntity(LivingEntity ent) {
        cachedEntity = ent;
        cached = true;
    }

    protected void setSelection(EntitySearch search) {
        selection.set(search);
        cached = false;
    }

    @Override
    public void receive(LivingEntity entity) {
        setCachedEntity(entity);
    }

    class GuiSelectEntitySearch extends GuiElement {

        EditBox textField;
        ButtonElement buttonSelect, buttonSelf, buttonConfirm;

        public GuiSelectEntitySearch() {
            super(0, 0, 94, 56);
            initTextureLocation(TEXTURE, 162, 144);
            Player player = Minecraft.getInstance().player;
            if(player == null) return;
            textField = new EditBox(Minecraft.getInstance().font, getX() + 8, getY() + 8, 80, 15, Component.empty());
            buttonSelect = new ButtonElement(getX() + 8, getY() + 16 + 15, 17, 17, button -> {
                ItemStack stack = InventoryUtil.searchPlayerForItem(player, ModItems.COMMANDER.get(), stack1 -> CommanderItem.getRememberedEntity(stack1) != null);

                if(stack != null) {
                    setSelection(new EntitySearch(CommanderItem.getRememberedEntity(stack)));
                } else {
                    player.sendSystemMessage(Lang.localise("found_no_selection").withStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
                }

                getBaseGui().removeSubGui();
            });
            buttonSelect.setTooltip(Lang.localise("selector.spEntity.useCommander"));
            buttonSelf = new ButtonElement(getX() + 2 * 8 + 17, getY() + 16 + 15, 17, 17, button -> {
                setSelection(new EntitySearch(player.getUUID()));
                getBaseGui().removeSubGui();
            });
            buttonSelf.setTooltip(Lang.localise("selector.spEntity.useSelf"));
            buttonConfirm = new ButtonElement(getX() + width - 8 - 17, getY() + 16 + 15, 17, 17, button -> {
                if(textField.getValue().isEmpty()) {
                    getBaseGui().removeSubGui();
                    return;
                }
                NetworkHandler.sendToServer(new PacketRequestEntitySearch(EntitySearchSelector.this.getParentGuiPath(), new EntitySearch(textField.getValue())));
                getBaseGui().removeSubGui();
            });
            buttonSelect.initTextureLocation(Reference.MISC, 0, 187);
            buttonSelf.initTextureLocation(Reference.MISC, 0, 0);
            buttonConfirm.initTextureLocation(Reference.MISC, 0, 170);

            addElement(textField);
            addElement(buttonSelect);
            addElement(buttonSelf);
            addElement(buttonConfirm);
            setFocused(textField);
        }
    }
}
