package com.ignis.igrobotics.client.screen.selectors;

import com.ignis.igrobotics.Reference;
import com.ignis.igrobotics.client.screen.base.GuiElement;
import com.ignis.igrobotics.client.screen.base.IElement;
import com.ignis.igrobotics.core.robot.Selection;
import com.ignis.igrobotics.core.util.Lang;
import com.ignis.igrobotics.core.util.RenderUtil;
import com.ignis.igrobotics.integration.jei.RoboticsJEIPlugin;
import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Optional;

@OnlyIn(Dist.CLIENT)
public class ItemSelector extends SelectorElement<ItemStack> {

	public ItemSelector(Selection<ItemStack> sel, int x, int y) {
		super(sel, x, y);
	}

	@Override
	protected IElement getMaximizedVersion() {
		return new GuiSelectItem();
	}

	@Override
	public void renderSelection(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		RenderUtil.drawItemStack(graphics, selection.get(), getX() + 1, getY() + 1);
	}

	class GuiSelectItem extends GuiElement {

		public GuiSelectItem() {
			super(0, 0, 94, 31);
			initTextureLocation(TEXTURE, 162, 113);
		}

		@Override
		public boolean keyPressed(int keyCode, int pScanCode, int pModifiers) {
			if(keyCode == InputConstants.KEY_RETURN) {
				IJeiRuntime jeiRuntime = RoboticsJEIPlugin.JEI_RUNTIME;
				if(jeiRuntime == null) return super.keyPressed(keyCode, pScanCode, pModifiers);
				Optional<ITypedIngredient<?>> obj = jeiRuntime.getIngredientListOverlay().getIngredientUnderMouse();
				if(obj.isPresent() && obj.get().getItemStack().isPresent()) {
					selection.set(obj.get().getItemStack().get());
					getBaseGui().removeSubGui();
				}
			}
			return super.keyPressed(keyCode, pScanCode, pModifiers);
		}

		@Override
		public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
			super.render(graphics, mouseX, mouseY, delta);
			RenderUtil.drawString(graphics, Lang.localise("pick_with_jei"), getX() + 5, getY() + height / 2 - 2, Reference.FONT_COLOR, 0.55f);
		}
	}

}
