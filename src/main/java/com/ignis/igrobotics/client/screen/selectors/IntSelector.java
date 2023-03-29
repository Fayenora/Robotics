package com.ignis.igrobotics.client.screen.selectors;

import com.ignis.igrobotics.Reference;
import com.ignis.igrobotics.client.screen.base.BaseScreen;
import com.ignis.igrobotics.client.screen.base.IElement;
import com.ignis.igrobotics.core.robot.Selection;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class IntSelector extends SelectorElement<Integer> {
	
	int min = Integer.MIN_VALUE, max = Integer.MAX_VALUE;

	public IntSelector(Selection sel, int x, int y) {
		super(sel, x, y);
	}
	
	public void setSelectionBounds(int min, int max) {
		this.min = min;
		this.max = max;
	}

	@Override
	protected IElement getMaximizedVersion() {
		return new GuiSelectInt();
	}

	@Override
	public void renderSelection(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
		drawCenteredString(poseStack, Minecraft.getInstance().font, Integer.toString(selection.get()), getX() + width / 2, getY() + 5, Reference.FONT_COLOR);
	}
	
	class GuiSelectInt extends BaseScreen {
		
		EditBox textField;

		public GuiSelectInt() {
			super(0, 0, 94, 31);
			initTextureLocation(TEXTURE, 162, 113);
			textField = new EditBox(Minecraft.getInstance().font, getX() + width / 2 - 40, getY() + 8, 80, 15, Component.empty()) {
				@Override
				public void setFocused(boolean isFocusedIn) {
					if(!isFocusedIn) {
						keepIntToBounds();
					}
					super.setFocused(isFocusedIn);
				}

				@Override
				public void insertText(String text) {
					String cleanedText = "";
					for(char c : text.toCharArray()) {
						if((cleanedText.isEmpty() && getCursorPosition() == 0 && (c == '-' || c == '+')) || (c >= '0' && c <= '9')) {
							cleanedText += c;
						}
					}
					super.insertText(cleanedText);
				}
			};
			textField.setFocus(true);
			addRenderableWidget(textField);
		}
		
		protected void keepIntToBounds() {
			try {
				Integer in = Integer.parseInt(textField.getValue());
				in = Math.max(min, Math.min(in, max));
				textField.setValue(Integer.toString(in));
			} catch(NumberFormatException e) {
				if(textField.getValue().length() != 0) {
					if(textField.getValue().charAt(0) == '-') {
						textField.setValue(Integer.toString(min));
					} else {
						textField.setValue(Integer.toString(max));
					}
				}
			}
		}

		@Override
		public boolean keyPressed(int keyCode, int pScanCode, int pModifiers) {
			if(keyCode == InputConstants.KEY_RETURN) {
				keepIntToBounds();
				try {
					selection.set(Integer.parseInt(textField.getValue()));
				} catch(NumberFormatException e) {
					selection.set(0);
				}
				getBaseGui().removeSubGui();
				return true;
			}
			return false;
		}
		
	}

}
