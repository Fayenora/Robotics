package com.ignis.igrobotics.client.screen.selectors;

import com.ignis.igrobotics.Reference;
import com.ignis.igrobotics.client.screen.base.BaseScreen;
import com.ignis.igrobotics.client.screen.base.IElement;
import com.ignis.igrobotics.client.screen.elements.ButtonElement;
import com.ignis.igrobotics.common.items.CommanderItem;
import com.ignis.igrobotics.core.robot.Selection;
import com.ignis.igrobotics.core.util.ItemStackUtils;
import com.ignis.igrobotics.core.util.Lang;
import com.ignis.igrobotics.definitions.ModItems;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PosSelector extends SelectorElement<BlockPos> {

	public PosSelector(Selection sel, int x, int y) {
		super(sel, x, y);
	}

	@Override
	protected IElement getMaximizedVersion() {
		return new GuiSelectPos();
	}

	@Override
	public void renderSelection(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
		Font font = Minecraft.getInstance().font;
		drawCenteredString(poseStack, font, Integer.toString(selection.get().getX()), getX() + width / 2, getY() + 2, Reference.FONT_COLOR);
		drawCenteredString(poseStack, font, Integer.toString(selection.get().getY()), getX() + width / 2, getY() + 7, Reference.FONT_COLOR);
		drawCenteredString(poseStack, font, Integer.toString(selection.get().getZ()), getX() + width / 2, getY() + 12, Reference.FONT_COLOR);
	}
	
	class GuiSelectPos extends BaseScreen {
		EditBox textFieldX, textFieldY, textFieldZ;
		ButtonElement buttonSelectPos, buttonSelfPos, buttonConfirm;

		public GuiSelectPos() {
			super(0, 0, 94, 113);
			initTextureLocation(TEXTURE, 162, 0);
			Font font = Minecraft.getInstance().font;
			textFieldX = new EditBox(font, getX() + width - 68, getY() + 8, 60, 15, Component.literal("text_box_x"));
			textFieldY = new EditBox(font, getX() + width - 68, getY() + 38, 60, 15, Component.literal("text_box_y"));
			textFieldZ = new EditBox(font, getX() + width - 68, getY() + 68, 60, 15, Component.literal("text_box_z"));
			textFieldX.setFocus(true);
			textFieldX.setValue(Integer.toString(selection.get().getX()));
			textFieldY.setValue(Integer.toString(selection.get().getY()));
			textFieldZ.setValue(Integer.toString(selection.get().getZ()));
			buttonSelectPos = new ButtonElement(getX() + 8, getY() + height - 8 - 17, 17, 17, button -> {
				ItemStack stack = ItemStackUtils.searchPlayerForItem(Minecraft.getInstance().player, ModItems.COMMANDER.get(), itemStack -> CommanderItem.getRememberedPos(itemStack) != null);
				if(stack == null) return;
				selection.set(CommanderItem.getRememberedPos(stack));
				//TODO Using an entity to set the position would require contacting the server

				textFieldX.setValue(Integer.toString(selection.get().getX()));
				textFieldY.setValue(Integer.toString(selection.get().getY()));
				textFieldZ.setValue(Integer.toString(selection.get().getZ()));
			});
			buttonSelectPos.setTooltip(Lang.localise("selector.pos.useCommander"));
			buttonSelfPos = new ButtonElement(getX() + 12 + 17, getY() + height - 8 - 17, 17, 17, button -> {
				selection.set(Minecraft.getInstance().player.getOnPos());
				textFieldX.setValue(Integer.toString(selection.get().getX()));
				textFieldY.setValue(Integer.toString(selection.get().getY()));
				textFieldZ.setValue(Integer.toString(selection.get().getZ()));
			});
			buttonSelfPos.setTooltip(Lang.localise("selector.pos.useSelf"));
			buttonConfirm = new ButtonElement(getX() + width - 8 - 17, getY() + height - 8 - 17, 17, 17, button -> {
				try {
					selection.set(new BlockPos(Integer.parseInt(textFieldX.getValue()), Integer.parseInt(textFieldY.getValue()), Integer.parseInt(textFieldZ.getValue())));
				} catch(NumberFormatException e) {
					//Parsing failed, leave position as it was
				}
				getBaseGui().removeSubGui();
			});
			buttonSelectPos.initTextureLocation(Reference.MISC, 0, 187);
			buttonSelfPos.initTextureLocation(Reference.MISC, 0, 0);
			buttonConfirm.initTextureLocation(Reference.MISC, 0, 170);

			addRenderableWidget(textFieldX);
			addRenderableWidget(textFieldY);
			addRenderableWidget(textFieldZ);
			addElement(buttonSelectPos);
			addElement(buttonSelfPos);
			addElement(buttonConfirm);
		}

		@Override
		public boolean keyPressed(int keyCode, int pScanCode, int pModifiers) {
			int position =  textFieldX.isFocused() ? textFieldX.getCursorPosition() :
					textFieldY.isFocused() ? textFieldY.getCursorPosition() :
							textFieldZ.isFocused() ? textFieldZ.getCursorPosition() : -1;

			if((position == 0 && keyCode == InputConstants.KEY_MINUS) || (keyCode >= InputConstants.KEY_0 && keyCode <= InputConstants.KEY_9) || Screen.hasControlDown() || Reference.isSpecialKey(keyCode)) {
				return super.keyPressed(keyCode, pScanCode, pModifiers);
			}
			return false;
		}

		@Override
		public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
			super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
			Font font = Minecraft.getInstance().font;
			drawString(pPoseStack, font, "X:", getX() + 11, getY() + 12, Reference.FONT_COLOR);
			drawString(pPoseStack, font, "Y:", getX() + 11, getY() + 42, Reference.FONT_COLOR);
			drawString(pPoseStack, font, "Z:", getX() + 11, getY() + 72, Reference.FONT_COLOR);
		}
		
	}

}
