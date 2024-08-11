package com.ignis.igrobotics.client.screen.selectors;

import com.ignis.igrobotics.Reference;
import com.ignis.igrobotics.client.screen.base.GuiElement;
import com.ignis.igrobotics.client.screen.base.IElement;
import com.ignis.igrobotics.client.screen.elements.ButtonElement;
import com.ignis.igrobotics.client.screen.elements.EditBoxInt;
import com.ignis.igrobotics.client.screen.elements.TickBox;
import com.ignis.igrobotics.common.content.items.CommanderItem;
import com.ignis.igrobotics.common.helpers.types.EntitySearch;
import com.ignis.igrobotics.common.helpers.types.Selection;
import com.ignis.igrobotics.common.helpers.util.InventoryUtil;
import com.ignis.igrobotics.common.helpers.util.Lang;
import com.ignis.igrobotics.common.helpers.util.MathUtil;
import com.ignis.igrobotics.common.helpers.util.RenderUtil;
import com.ignis.igrobotics.definitions.ModItems;
import com.ignis.igrobotics.network.NetworkHandler;
import com.ignis.igrobotics.network.messages.IPacketDataReceiver;
import com.ignis.igrobotics.network.messages.server.PacketRequestEntitySearch;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.UUID;

@MethodsReturnNonnullByDefault
public class EntitySearchSelector extends SelectorElement<EntitySearch> implements IPacketDataReceiver {

    private boolean cached, serverAsked;
    private LivingEntity cachedEntity;
    private int dotTimer;

    public EntitySearchSelector(Selection<EntitySearch> selection, int x, int y) {
        super(selection, x, y);
    }

    @Override
    protected IElement getMaximizedVersion() {
        return new GuiSelectEntitySearch(getSelection().get());
    }

    @Override
    public void renderSelection(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if(selection.get() == null) return;
        if(selection.get().isSearchForNone()) {
            RenderUtil.drawCenteredString(graphics, Component.translatable("none"), getX() + width / 2, getY() + height / 2 - 2, Reference.FONT_COLOR, 0.5f, getWidth() - 2);
            return;
        }
        if(!cached && !serverAsked) {
            //TODO: Don't overkill and ask the server instantly, look on the client first
            NetworkHandler.sendToServer(new PacketRequestEntitySearch(getParentGuiPath(), selection.get()));
            serverAsked = true;
            return;
        }
        if(cachedEntity == null) {
            Component display;
            if(!cached) {
                display = Component.literal(".".repeat(Math.floorDiv((dotTimer++ % 300 * 4), 300)));
            } else {
                display = Component.translatable("found.none");
            }
            RenderUtil.drawCenteredString(graphics, display, getX() + width / 2, getY() + height / 2 - 2, Reference.FONT_COLOR, 0.5f, getWidth() - 2);
            return;
        }
        if(currentGuiActive()) {
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
        serverAsked = false;
    }

    @Override
    public void receive(LivingEntity entity) {
        setCachedEntity(entity);
    }

    @Override
    public List<Component> getTooltip(int mouseX, int mouseY) {
        if(cachedEntity == null) return List.of();
        return List.of(cachedEntity.getDisplayName());
    }

    class GuiSelectEntitySearch extends GuiElement {

        EditBox textFieldName;
        EditBoxInt textFieldRange;
        ButtonElement buttonSelect, buttonSelf, buttonConfirm;
        TickBox tickBoxUseType, tickBoxUseRange, tickBoxUseName;
        Selection<EntityType<?>> typeSelection = Selection.of(EntityType.CREEPER);
        EntityTypeSelector typeSelector;

        public GuiSelectEntitySearch(EntitySearch parent) {
            super(0, 0, 94, 113);
            initTextureLocation(TEXTURE, 162, 0);
            Player player = Minecraft.getInstance().player;
            if(player == null) return;
            tickBoxUseType = new TickBox(getX() + 5, getY() + 8);
            tickBoxUseRange = new TickBox(getX() + 5, getY() + 31);
            tickBoxUseName = new TickBox(getX() + 5, getY() + 54);

            if(parent.searchesFor(EntitySearch.SearchFlags.TYPE)) {
                tickBoxUseType.nextState();
                parent.getType().ifPresent(val -> typeSelection = Selection.of(val));
            }

            typeSelector = new EntityTypeSelector(typeSelection, getX() + 24, getY() + 7);
            textFieldRange = new EditBoxInt(getX() + 24, getY() + 31, 60, 15, 0, 100);
            textFieldName = new EditBox(Minecraft.getInstance().font, getX() + 24, getY() + 54, 60, 15, Component.empty());

            buttonSelect = new ButtonElement(getX() + 8, getY() + 16 + 15 + 46, 17, 17, button -> {
                ItemStack stack = InventoryUtil.searchPlayerForItem(player, ModItems.COMMANDER.get(), stack1 -> CommanderItem.getRememberedEntity(stack1) != null);
                UUID rememberedEntity = stack != null ? CommanderItem.getRememberedEntity(stack) : null;

                if(rememberedEntity != null) {
                    setSelection(new EntitySearch(rememberedEntity));
                } else {
                    player.sendSystemMessage(Lang.localise("found_no_selection").withStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
                }

                getBaseGui().removeSubGui();
            });
            buttonSelf = new ButtonElement(getX() + 2 * 8 + 17, getY() + 16 + 15 + 46, 17, 17, button -> {
                setSelection(new EntitySearch(player.getUUID()));
                getBaseGui().removeSubGui();
            });
            buttonConfirm = new ButtonElement(getX() + width - 8 - 17, getY() + 16 + 15 + 46, 17, 17, button -> {
                EntitySearch search = EntitySearch.searchForNone();
                if(tickBoxUseType.isTicked()) {
                    search.setType(typeSelection.get());
                }
                if(tickBoxUseRange.isTicked() && !textFieldRange.getValue().isEmpty()) {
                    search.setRange(textFieldRange.getIntValue() * 2); // Experiments yield that this should be multiplied by 2 to get intuitive block distances
                }
                if(tickBoxUseName.isTicked() && !textFieldName.getValue().isEmpty()) {
                    search.setName(textFieldName.getValue());
                }
                setSelection(search);
                getBaseGui().removeSubGui();
            });
            buttonSelect.initTextureLocation(Reference.MISC, 0, 187);
            buttonSelf.initTextureLocation(Reference.MISC, 0, 0);
            buttonConfirm.initTextureLocation(Reference.MISC, 0, 170);

            tickBoxUseType.setTooltip(Lang.localise("selector.entity.useType"));
            tickBoxUseRange.setTooltip(Lang.localise("selector.entity.useRange"));
            tickBoxUseName.setTooltip(Lang.localise("selector.entity.useName"));
            tickBoxUseType.setTooltip(1, Lang.localise("selector.entity.useType"));
            tickBoxUseRange.setTooltip(1, Lang.localise("selector.entity.useRange"));
            tickBoxUseName.setTooltip(1, Lang.localise("selector.entity.useName"));
            buttonSelect.setTooltip(Lang.localise("selector.entity.useCommander"));
            buttonSelf.setTooltip(Lang.localise("selector.entity.useSelf"));
            buttonConfirm.setTooltip(Lang.localise("selector.entity.confirm"));
            addElement(tickBoxUseType);
            addElement(tickBoxUseRange);
            addElement(tickBoxUseName);
            addElement(typeSelector);
            addElement(textFieldRange);
            addElement(textFieldName);
            addElement(buttonSelect);
            addElement(buttonSelf);
            addElement(buttonConfirm);
            setFocused(textFieldName);

            if(parent.searchesFor(EntitySearch.SearchFlags.RANGE)) {
                tickBoxUseRange.nextState();
                textFieldRange.insertText(String.valueOf(parent.getRange()));
            }
            if(parent.searchesFor(EntitySearch.SearchFlags.NAME)) {
                tickBoxUseName.nextState();
                parent.getName().ifPresent(name -> textFieldName.insertText(name));
            }
        }
    }
}
