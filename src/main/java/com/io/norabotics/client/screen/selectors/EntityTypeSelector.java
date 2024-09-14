package com.io.norabotics.client.screen.selectors;

import com.io.norabotics.client.screen.base.IElement;
import com.io.norabotics.client.screen.elements.GuiSelectEntity;
import com.io.norabotics.common.CommonSetup;
import com.io.norabotics.common.helpers.types.Selection;
import com.io.norabotics.common.helpers.util.Lang;
import com.io.norabotics.common.helpers.util.MathUtil;
import com.io.norabotics.common.helpers.util.RenderUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@OnlyIn(Dist.CLIENT)
@MethodsReturnNonnullByDefault
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
	public List<Component> getTooltip(int mouseX, int mouseY) {
		if(selection.get() == null) return List.of();
		return List.of(Lang.localiseExisting(selection.get().getDescriptionId()));
	}

	@Override
	public void renderSelection(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		if(selection == null || selection.get() == null) return;
		Entity entity = CommonSetup.allLivingEntities.get(selection.get());
		if(!(entity instanceof LivingEntity living)) return;
		RenderUtil.enableScissor(graphics, MathUtil.downsizeRect(getShape(), 1));
		RenderUtil.drawRotatingEntity(graphics.pose(), getX() + width / 2, getY() + height / 2 + 6, (int) (8 / living.getBoundingBox().getSize()), living, angle);
		RenderUtil.disableScissor(graphics);
	}

}
