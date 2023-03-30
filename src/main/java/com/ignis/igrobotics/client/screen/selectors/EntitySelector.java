package com.ignis.igrobotics.client.screen.selectors;

import com.ignis.igrobotics.Reference;
import com.ignis.igrobotics.client.screen.base.BaseScreen;
import com.ignis.igrobotics.client.screen.base.IElement;
import com.ignis.igrobotics.client.screen.elements.ButtonElement;
import com.ignis.igrobotics.common.items.CommanderItem;
import com.ignis.igrobotics.core.robot.Selection;
import com.ignis.igrobotics.core.util.ItemStackUtils;
import com.ignis.igrobotics.core.util.Lang;
import com.ignis.igrobotics.core.util.MathUtil;
import com.ignis.igrobotics.core.util.RenderUtil;
import com.ignis.igrobotics.definitions.ModItems;
import com.ignis.igrobotics.network.NetworkHandler;
import com.ignis.igrobotics.network.messages.IPacketDataReceiver;
import com.ignis.igrobotics.network.messages.server.PacketRequestEntitySearch;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.awt.*;
import java.util.UUID;

@OnlyIn(Dist.CLIENT)
public class EntitySelector extends SelectorElement<UUID> implements IPacketDataReceiver {
	
	/** If a server side search yielded no result(cachedEntity is null), the client still needs to stop asking */
	private boolean cached = false;
	private LivingEntity cachedEntity;

	public EntitySelector(Selection sel, int x, int y) {
		super(sel, x, y);
	}

	@Override
	protected IElement getMaximizedVersion() {
		return new GuiSelectSpEntity();
	}

	@Override
	public void renderSelection(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
		if(selection.get() == null) return;
		if(selection.get().equals(Reference.DEFAULT_UUID)) {
			poseStack.pushPose();
			poseStack.scale(0.4f, 0.4f, 0.4f);
			drawString(poseStack, Minecraft.getInstance().font, Component.translatable("default"), getX() + 2, getY() + height / 2 - 2, Reference.FONT_COLOR);
			poseStack.popPose();
			return;
		}
		if(!cached) {
			//TODO: Don't overkill and ask the server instantly, look on the client first
			NetworkHandler.sendToServer(new PacketRequestEntitySearch(getParentGuiPath(), selection.get()));
			return;
		}
		if(cachedEntity != null) {
			RenderUtil.beginClipping(MathUtil.downsizeRect(getShape(), 1));
			RenderUtil.drawRotatingEntity(getX() + width / 2, getY() + height / 2 + 6, (int) (8 / cachedEntity.getBoundingBox().getSize()), cachedEntity, angle);
			RenderSystem.disableScissor();
		}
	}
	
	public void setCachedEntity(LivingEntity ent) {
		cached = true;
		cachedEntity = ent;
	}
	
	public void setSelection(UUID uuid) {
		cached = false;
		selection.set(uuid);
	}

	@Override
	public void receive(LivingEntity entity) {
		selection.set(entity.getUUID());
		setCachedEntity(entity);
	}

	class GuiSelectSpEntity extends BaseScreen {
		
		EditBox textField;
		ButtonElement buttonSelect, buttonSelf, buttonConfirm;

		public GuiSelectSpEntity() {
			super(0, 0, 94, 56);
			initTextureLocation(TEXTURE, 162, 144);
			Player player = Minecraft.getInstance().player;
			textField = new EditBox(Minecraft.getInstance().font, getX() + 8, getY() + 8, 80, 15, Component.empty());
			textField.setFocus(true);
			
			buttonSelect = new ButtonElement(getX() + 8, getY() + 16 + 15, 17, 17, button -> {
				ItemStack stack = ItemStackUtils.searchPlayerForItem(player, ModItems.COMMANDER.get(), stack1 -> CommanderItem.getRememberedEntity(stack1) != null);

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
				NetworkHandler.sendToServer(new PacketRequestEntitySearch(EntitySelector.this.getParentGuiPath(), textField.getValue()));
				getBaseGui().removeSubGui();
			});
			buttonSelect.initTextureLocation(Reference.MISC, 0, 187);
			buttonSelf.initTextureLocation(Reference.MISC, 0, 0);
			buttonConfirm.initTextureLocation(Reference.MISC, 0, 170);
			
			addRenderableWidget(textField);
			addElement(buttonSelect);
			addElement(buttonSelf);
			addElement(buttonConfirm);
		}
	}

}
