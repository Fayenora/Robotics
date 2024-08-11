package com.ignis.igrobotics.client.screen.selectors;

import com.ignis.igrobotics.Reference;
import com.ignis.igrobotics.client.screen.base.GuiElement;
import com.ignis.igrobotics.client.screen.base.IElement;
import com.ignis.igrobotics.client.screen.elements.EditBoxInt;
import com.ignis.igrobotics.common.helpers.types.Selection;
import com.ignis.igrobotics.common.helpers.util.RenderUtil;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class IntSelector extends SelectorElement<Integer> {

	public IntSelector(Selection<Integer> sel, int x, int y) {
		super(sel, x, y);
	}

	@Override
	protected IElement getMaximizedVersion() {
		return new GuiSelectInt();
	}

	@Override
	public void renderSelection(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		RenderUtil.drawCenteredString(graphics, Integer.toString(selection.get()), getX() + width / 2, getY() + 5, Reference.FONT_COLOR, 1, 16);
	}
	
	class GuiSelectInt extends GuiElement {
		
		EditBoxInt textField;

		public GuiSelectInt() {
			super(0, 0, 94, 31);
			initTextureLocation(TEXTURE, 162, 113);
			textField = new EditBoxInt(getX() + width / 2 - 40, getY() + 8, 80, 15, Integer.MIN_VALUE, Integer.MAX_VALUE);
			addElement(textField);
			setFocused(textField);
		}

		@Override
		public boolean keyPressed(int keyCode, int pScanCode, int pModifiers) {
			if(keyCode == InputConstants.KEY_RETURN) {
				selection.set(textField.getIntValue());
				getBaseGui().removeSubGui();
			}
			return super.keyPressed(keyCode, pScanCode, pModifiers);
		}
		
	}

}
