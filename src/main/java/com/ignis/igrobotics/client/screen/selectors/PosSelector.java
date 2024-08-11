package com.ignis.igrobotics.client.screen.selectors;

import com.ignis.igrobotics.Reference;
import com.ignis.igrobotics.client.screen.base.GuiElement;
import com.ignis.igrobotics.client.screen.base.IElement;
import com.ignis.igrobotics.client.screen.elements.ButtonElement;
import com.ignis.igrobotics.client.screen.elements.EditBoxInt;
import com.ignis.igrobotics.common.content.items.CommanderItem;
import com.ignis.igrobotics.common.helpers.types.Selection;
import com.ignis.igrobotics.common.helpers.util.InventoryUtil;
import com.ignis.igrobotics.common.helpers.util.Lang;
import com.ignis.igrobotics.common.helpers.util.RenderUtil;
import com.ignis.igrobotics.definitions.ModItems;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class PosSelector extends SelectorElement<GlobalPos> {

	public PosSelector(Selection<GlobalPos> sel, int x, int y) {
		super(sel, x, y);
	}

	@Override
	protected IElement getMaximizedVersion() {
		return new GuiSelectPos();
	}

	@Override
	public void renderSelection(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		float fontSize = 0.5f;
		RenderUtil.drawCenteredString(graphics, Integer.toString(selection.get().pos().getX()), getX() + width / 2, getY() + 2, Reference.FONT_COLOR, fontSize, 16);
		RenderUtil.drawCenteredString(graphics, Integer.toString(selection.get().pos().getY()), getX() + width / 2, getY() + 7, Reference.FONT_COLOR, fontSize, 16);
		RenderUtil.drawCenteredString(graphics, Integer.toString(selection.get().pos().getZ()), getX() + width / 2, getY() + 12, Reference.FONT_COLOR, fontSize, 16);
	}

	@Override
	public List<Component> getTooltip(int mouseX, int mouseY) {
		if(selection.get() == null) return List.of();
		GlobalPos pos = selection.get();
		Component display = ComponentUtils.formatList(List.of(
				Lang.localiseExisting(pos.dimension().location().toString()),
				Component.literal(String.valueOf(pos.pos().getX())),
				Component.literal(String.valueOf(pos.pos().getY())),
				Component.literal(String.valueOf(pos.pos().getZ()))), Component.literal(" "));
		return List.of(display);
	}

	class GuiSelectPos extends GuiElement {
		DimensionSelectElement dimensionSelection;
		EditBoxInt textFieldX, textFieldY, textFieldZ;
		ButtonElement buttonSelectPos, buttonSelfPos, buttonConfirm;

		public GuiSelectPos() {
			super(0, 0, 94, 113);
			initTextureLocation(TEXTURE, 162, 0);
			dimensionSelection = new DimensionSelectElement(getX() + 8, getY() + 6, 79, 14, selection.get().dimension());
			dimensionSelection.initTextureLocation(TEXTURE, 0, 242);
			textFieldX = new EditBoxInt(getX() + width - 68, getY() + 24, 60, 15);
			textFieldY = new EditBoxInt(getX() + width - 68, getY() + 46, 60, 15);
			textFieldZ = new EditBoxInt(getX() + width - 68, getY() + 68, 60, 15);
			updateTextFields();
			Player player = Minecraft.getInstance().player;
			if(player == null) return;
			buttonSelectPos = new ButtonElement(getX() + 8, getY() + height - 8 - 17, 17, 17, button -> {
				ItemStack stack = InventoryUtil.searchPlayerForItem(player, ModItems.COMMANDER.get(), itemStack -> CommanderItem.getRememberedPos(itemStack) != null);
				if(stack == null) return;
				selection.set(CommanderItem.getRememberedPos(stack));
				//TODO Using an entity to set the position would require contacting the server

				updateTextFields();
			});
			buttonSelectPos.setTooltip(Lang.localise("selector.pos.useCommander"));
			buttonSelfPos = new ButtonElement(getX() + 12 + 17, getY() + height - 8 - 17, 17, 17, button -> {
				selection.set(GlobalPos.of(player.level().dimension(), player.getOnPos()));
				updateTextFields();
			});
			buttonSelfPos.setTooltip(Lang.localise("selector.pos.useSelf"));
			buttonConfirm = new ButtonElement(getX() + width - 8 - 17, getY() + height - 8 - 17, 17, 17, button -> {
				try {
					int x = Integer.parseInt(textFieldX.getValue());
					int y = Integer.parseInt(textFieldY.getValue());
					int z = Integer.parseInt(textFieldZ.getValue());
					selection.set(GlobalPos.of(dimensionSelection.currentDim(), new BlockPos(x, y, z)));
				} catch(NumberFormatException e) {
					//Parsing failed, leave position as it was
				}
				getBaseGui().removeSubGui();
			});
			buttonSelectPos.initTextureLocation(Reference.MISC, 0, 187);
			buttonSelfPos.initTextureLocation(Reference.MISC, 0, 0);
			buttonConfirm.initTextureLocation(Reference.MISC, 0, 170);

			addElement(dimensionSelection);
			addElement(textFieldX);
			addElement(textFieldY);
			addElement(textFieldZ);
			addElement(buttonSelectPos);
			addElement(buttonSelfPos);
			addElement(buttonConfirm);
			setFocused(textFieldX);
		}

		private void updateTextFields() {
			Level relevantLevel = ServerLifecycleHooks.getCurrentServer().getLevel(selection.get().dimension());
			if(relevantLevel == null) ServerLifecycleHooks.getCurrentServer().overworld();
			dimensionSelection.setDim(selection.get().dimension());
			textFieldX.setValue(Integer.toString(selection.get().pos().getX()));
			textFieldY.setValue(Integer.toString(selection.get().pos().getY()));
			textFieldZ.setValue(Integer.toString(selection.get().pos().getZ()));
			textFieldX.setBounds((int) relevantLevel.getWorldBorder().getMinX(), (int) relevantLevel.getWorldBorder().getMaxX());
			textFieldY.setBounds(relevantLevel.dimensionType().minY(), relevantLevel.dimensionType().logicalHeight());
			textFieldZ.setBounds((int) relevantLevel.getWorldBorder().getMinZ(), (int) relevantLevel.getWorldBorder().getMaxZ());
		}

		@Override
		public boolean keyPressed(int keyCode, int pScanCode, int pModifiers) {
			if(keyCode == InputConstants.KEY_ESCAPE) {
				return super.keyPressed(keyCode, pScanCode, pModifiers);
			}
			int position =  textFieldX.isFocused() ? textFieldX.getCursorPosition() :
					textFieldY.isFocused() ? textFieldY.getCursorPosition() :
							textFieldZ.isFocused() ? textFieldZ.getCursorPosition() : -1;

			if((position == 0 && keyCode == InputConstants.KEY_MINUS) || (keyCode >= InputConstants.KEY_0 && keyCode <= InputConstants.KEY_9) || Screen.hasControlDown() || Reference.isSpecialKey(keyCode)) {
				return super.keyPressed(keyCode, pScanCode, pModifiers);
			}
			return false;
		}

		@Override
		public void render(GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
			super.render(graphics, pMouseX, pMouseY, pPartialTick);
			Font font = Minecraft.getInstance().font;
			graphics.drawString(font, "X:", getX() + 11, getY() + 28, Reference.FONT_COLOR);
			graphics.drawString(font, "Y:", getX() + 11, getY() + 50, Reference.FONT_COLOR);
			graphics.drawString(font, "Z:", getX() + 11, getY() + 72, Reference.FONT_COLOR);
		}

	}

	static class DimensionSelectElement extends ButtonElement {

		private final List<ResourceKey<Level>> levels;
		private int currentIndex;

		public DimensionSelectElement(int pX, int pY, int pWidth, int pHeight, ResourceKey<Level> selectedLevel) {
			super(pX, pY, pWidth, pHeight);
			levels = ServerLifecycleHooks.getCurrentServer().levelKeys().stream().toList();
			setDim(selectedLevel);
		}

		@Override
		public void onPress() {
			super.onPress();
			currentIndex = (currentIndex + 1) % levels.size();
		}

		public ResourceKey<Level> currentDim() {
			return levels.get(currentIndex);
		}

		public void setDim(ResourceKey<Level> dim) {
			currentIndex = levels.indexOf(dim);
		}

		@Override
		public void renderWidget(GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
			super.renderWidget(graphics, pMouseX, pMouseY, pPartialTick);
			RenderUtil.drawString(graphics, Lang.localiseExisting(currentDim().location().toString()), getX() + 6, getY() + 4, Reference.FONT_COLOR, 0.8f);
		}
	}

}
