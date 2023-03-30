package com.ignis.igrobotics.client.screen.selectors;

import com.ignis.igrobotics.client.screen.base.GuiElement;
import com.ignis.igrobotics.client.screen.base.IElement;
import com.ignis.igrobotics.client.screen.elements.ButtonElement;
import com.ignis.igrobotics.client.screen.elements.ScrollableElement;
import com.ignis.igrobotics.common.CommonSetup;
import com.ignis.igrobotics.core.robot.Selection;
import com.ignis.igrobotics.core.util.MathUtil;
import com.ignis.igrobotics.core.util.RenderUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.Collection;

@OnlyIn(Dist.CLIENT)
public class EntityTypeSelector extends SelectorElement<EntityType<?>> {
	
	protected Collection<LivingEntity> options;
	
	public EntityTypeSelector(Selection<EntityType<?>> sel, int x, int y) {
		this(sel, x, y, CommonSetup.allLivingEntities.values());
	}
	
	public EntityTypeSelector(Selection<EntityType<?>> sel, int x, int y, Collection<LivingEntity> options) {
		super(sel, x, y);
		setTooltip(selection.get().getDescription());
		this.options = options;
		if(options == null) {
			this.options = new ArrayList<>();
		}
	}

	public void setSelection(EntityType<?> type) {
		selection.set(type);
		setTooltip(selection.get().getDescription());
	}
	
	@Override
	protected IElement getMaximizedVersion() {
		return new GuiSelectEntity(options);
	}

	@Override
	public void renderSelection(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
		if(selection == null || selection.get() == null) return;
		Entity entity = CommonSetup.allLivingEntities.get(selection.get());
		if(!(entity instanceof LivingEntity living)) return;
		RenderUtil.beginClipping(MathUtil.downsizeRect(getShape(), 1));
		RenderUtil.drawRotatingEntity(getX() + width / 2, getY() + height / 2 + 6, (int) (8 / living.getBoundingBox().getSize()), living, angle);
		RenderSystem.disableScissor();
	}
	
	class GuiSelectEntity extends GuiElement {
		
		EditBox searchBar;
		ScrollableElement entityGrid;
		
		String currentSearch = "";
		Collection<LivingEntity> allOptions;
		Collection<LivingEntity> currentOptions;
		
		public GuiSelectEntity(Collection<LivingEntity> selectableOptions) {
			super(0, 0, 162, 164);
			initTextureLocation(TEXTURE, 0, 0);
			this.allOptions = selectableOptions;
			this.currentOptions = selectableOptions;
			
			searchBar = new EditBox(Minecraft.getInstance().font, getX() + 8, getY() + 8, 146, 10, Component.empty());
			searchBar.setFocus(true);
			entityGrid = new ScrollableElement(getX() + 9, getY() + 25, 145, 131);
			setOptions(currentOptions);

			addElement(searchBar);
			addElement(entityGrid);
		}

		@Override
		public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
			super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
			//Update the search
			String searchTerm = searchBar.getValue().toLowerCase();
			if(searchTerm.equals(currentSearch)) return;
			if(currentSearch.length() < searchTerm.length()) {
				//Squeeze some efficiency here by only narrowing the search if it got longer
				currentOptions = MathUtil.subset(currentOptions, ent -> ent.getName().getString().toLowerCase().contains(searchTerm));
			} else {
				//If the search got shorter, we have no choice but to search everything again
				currentOptions = MathUtil.subset(allOptions, ent -> ent.getName().getString().toLowerCase().contains(searchTerm));
			}
			setOptions(currentOptions);
			currentSearch = searchTerm;
		}
		
		private void setOptions(Collection<LivingEntity> options) {
			entityGrid.clear();
			for(LivingEntity living : options) {
				entityGrid.addElement(new ComponentEntity(living, 0, 0, pButton -> {
					setSelection(living.getType());
					getBaseGui().removeSubGui();
				}));
			}
		}
		
	}

	public class ComponentEntity extends ButtonElement {

		LivingEntity living;

		public ComponentEntity(LivingEntity living, int x, int y, Button.OnPress onPress) {
			super(x, y, BOUNDS.width, BOUNDS.height, onPress);
			initSingleTextureLocation(TEXTURE, 0, 164);
			setTooltip(living.getName());
			this.living = living;
		}

		@Override
		public void renderButton(PoseStack poseStack, int pMouseX, int pMouseY, float pPartialTick) {
			super.renderButton(poseStack, pMouseX, pMouseY, pPartialTick);
			RenderUtil.beginClipping(MathUtil.downsizeRect(getShape(), 1));
			RenderUtil.drawRotatingEntity(getX() + BOUNDS.width / 2, getY() + 6 + BOUNDS.height / 2, (int) (8 / living.getBoundingBox().getSize()), living, angle);
			RenderSystem.disableScissor();
		}
		
	}

}
