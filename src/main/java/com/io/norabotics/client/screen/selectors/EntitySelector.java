package com.io.norabotics.client.screen.selectors;

import com.io.norabotics.Reference;
import com.io.norabotics.client.screen.base.GuiElement;
import com.io.norabotics.client.screen.base.IElement;
import com.io.norabotics.client.screen.elements.ButtonElement;
import com.io.norabotics.common.content.items.CommanderItem;
import com.io.norabotics.common.helpers.types.EntitySearch;
import com.io.norabotics.common.helpers.types.Selection;
import com.io.norabotics.common.helpers.util.InventoryUtil;
import com.io.norabotics.common.helpers.util.Lang;
import com.io.norabotics.common.helpers.util.MathUtil;
import com.io.norabotics.common.helpers.util.RenderUtil;
import com.io.norabotics.definitions.ModItems;
import com.io.norabotics.network.NetworkHandler;
import com.io.norabotics.network.messages.IPacketDataReceiver;
import com.io.norabotics.network.messages.server.PacketRequestEntitySearch;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@OnlyIn(Dist.CLIENT)
@MethodsReturnNonnullByDefault
public class EntitySelector extends SelectorElement<UUID> implements IPacketDataReceiver {

	/** If a server side search yielded no result(cachedEntity is null), the client still needs to stop asking */
	private boolean cached = false;
	@Nullable protected LivingEntity cachedEntity;

	public EntitySelector(Selection<UUID> sel, int x, int y) {
		super(sel, x, y);
	}

	@Override
	protected IElement getMaximizedVersion() {
		return new GuiSelectSpEntity();
	}

	@Override
	public void renderSelection(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		if(selection.get() == null) return;
		if(selection.get().equals(Reference.DEFAULT_UUID)) {
			RenderUtil.drawCenteredString(graphics, Component.translatable("none"), getX() + width / 2, getY() + height / 2 - 2, Reference.FONT_COLOR, 0.5f, getWidth() - 2);
			return;
		}
		if(!cached) {
			//TODO: Don't overkill and ask the server instantly, look on the client first
			NetworkHandler.sendToServer(new PacketRequestEntitySearch(getParentGuiPath(), new EntitySearch(selection.get())));
			return;
		}
		if(cachedEntity != null && currentGuiActive()) {
			RenderUtil.enableScissor(graphics, MathUtil.downsizeRect(getShape(), 1));
			RenderUtil.drawRotatingEntity(graphics.pose(), getX() + width / 2, getY() + height / 2 + 6, (int) (8 / cachedEntity.getBoundingBox().getSize()), cachedEntity, angle);
			RenderUtil.disableScissor(graphics);
		}
	}

	private void setCachedEntity(LivingEntity ent) {
		cached = true;
		cachedEntity = ent;
	}

	protected void setSelection(UUID uuid) {
		cached = false;
		selection.set(uuid);
	}

	@Override
	public void receive(LivingEntity entity) {
		selection.set(entity.getUUID());
		setCachedEntity(entity);
	}

	class GuiSelectSpEntity extends GuiElement {

		EditBox textField;
		ButtonElement buttonSelect, buttonSelf, buttonConfirm;

		public GuiSelectSpEntity() {
			super(0, 0, 94, 56);
			initTextureLocation(TEXTURE, 162, 144);
			Player player = Minecraft.getInstance().player;
			if(player == null) return;
			textField = new EditBox(Minecraft.getInstance().font, getX() + 8, getY() + 8, 80, 15, Component.empty());
			buttonSelect = new ButtonElement(getX() + 8, getY() + 16 + 15, 17, 17, button -> {
				ItemStack stack = InventoryUtil.searchPlayerForItem(player, ModItems.COMMANDER.get(), stack1 -> CommanderItem.getRememberedEntity(stack1) != null);

				if(stack != null) {
					setSelection(CommanderItem.getRememberedEntity(stack));
				} else {
					player.sendSystemMessage(Lang.localise("found_no_selection").withStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
				}

				getBaseGui().removeSubGui();
			});
			buttonSelect.setTooltip(Lang.localise("selector.spEntity.useCommander"));
			buttonSelf = new ButtonElement(getX() + 2 * 8 + 17, getY() + 16 + 15, 17, 17, button -> {
				setSelection(player.getUUID());
				getBaseGui().removeSubGui();
			});
			buttonSelf.setTooltip(Lang.localise("selector.spEntity.useSelf"));
			buttonConfirm = new ButtonElement(getX() + width - 8 - 17, getY() + 16 + 15, 17, 17, button -> {
				if(textField.getValue().isEmpty()) {
					getBaseGui().removeSubGui();
					return;
				}
				NetworkHandler.sendToServer(new PacketRequestEntitySearch(EntitySelector.this.getParentGuiPath(), new EntitySearch(textField.getValue())));
				getBaseGui().removeSubGui();
			});
			buttonConfirm.setTooltip(Lang.localise("selector.spEntity.confirm"));
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
