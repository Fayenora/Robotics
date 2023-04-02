package com.ignis.igrobotics.client.screen.selectors;

import com.ignis.igrobotics.Reference;
import com.ignis.igrobotics.client.screen.base.GuiElement;
import com.ignis.igrobotics.client.screen.base.IElement;
import com.ignis.igrobotics.core.robot.Selection;
import com.ignis.igrobotics.core.util.Lang;
import com.ignis.igrobotics.core.util.RenderUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

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
	public void renderSelection(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
		RenderUtil.drawItemStack(selection.get(), getX() + 1, getY() + 1);
	}
	
	class GuiSelectItem extends GuiElement {

		public GuiSelectItem() {
			super(0, 0, 94, 31);
			initTextureLocation(TEXTURE, 162, 113);
		}

		@Override
		public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
			/* TODO
			if(keyCode == Keyboard.KEY_RETURN) {
				IJeiRuntime jeiRuntime = RoboticsJEIPlugin.jeiRuntime;
				if(jeiRuntime == null) return;
				Object obj = jeiRuntime.getIngredientListOverlay().getIngredientUnderMouse();
				if(obj == null || !(obj instanceof ItemStack)) return;

				selector.target = (ItemStack) obj;
				getBaseGui().removeSubGui();
			}
			 */
			return super.keyPressed(pKeyCode, pScanCode, pModifiers);
		}

		@Override
		public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
			super.render(poseStack, mouseX, mouseY, delta);
			RenderUtil.drawString(poseStack, Lang.localise("pick_with_jei"), getX() + 5, getY() + height / 2 - 2, Reference.FONT_COLOR, 0.55f);
		}
	}

}
