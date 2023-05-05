package com.ignis.igrobotics.client.screen.selectors;

import com.ignis.igrobotics.client.screen.base.IElement;
import com.ignis.igrobotics.client.screen.elements.GuiSelectEntity;
import com.ignis.igrobotics.common.CommonSetup;
import com.ignis.igrobotics.core.robot.Selection;
import com.ignis.igrobotics.core.util.MathUtil;
import com.ignis.igrobotics.core.util.RenderUtil;
import com.mojang.blaze3d.vertex.PoseStack;
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

	public void setSelection(LivingEntity entity) {
		selection.set(entity.getType());
		setTooltip(selection.get().getDescription());
	}

	@Override
	protected IElement getMaximizedVersion() {
		return new GuiSelectEntity(options, this::setSelection);
	}

	@Override
	public void renderSelection(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
		if(selection == null || selection.get() == null) return;
		Entity entity = CommonSetup.allLivingEntities.get(selection.get());
		if(!(entity instanceof LivingEntity living)) return;
		if(getBaseGui().hasSubGui()) return;
		RenderUtil.enableScissor(MathUtil.downsizeRect(getShape(), 1));
		RenderUtil.drawRotatingEntity(poseStack, getX() + width / 2, getY() + height / 2 + 6, (int) (8 / living.getBoundingBox().getSize()), living, angle);
		RenderUtil.disableScissor();
	}

}
