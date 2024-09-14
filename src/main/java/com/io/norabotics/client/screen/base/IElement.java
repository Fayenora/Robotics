package com.io.norabotics.client.screen.base;

import com.io.norabotics.Robotics;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A Widget with defined shape, visibility and parent component. Slightly extends on the GuiEventHandler.
 * In contrast to {@link net.minecraft.client.gui.components.AbstractWidget} this can hold children components which are handled properly
 * @author Ignis
 */
@OnlyIn(Dist.CLIENT)
@MethodsReturnNonnullByDefault
public interface IElement extends ContainerEventHandler, Renderable, NarratableEntry {

	void element$setX(int x);
	void element$setY(int y);
	Rectangle getShape();

	void setEnabled(boolean enabled);
	void setVisible(boolean visible);
	boolean isEnabled();
	boolean isVisible();

	void addElement(IElement element);

	void setParentComponent(IElement comp);
	@Nullable IElement getParentComponent();

	@Override
	default boolean isMouseOver(double mouseX, double mouseY) {
		return mouseX >= getShape().x && mouseY >= getShape().y && mouseX < getShape().x + getShape().width && mouseY < getShape().y + getShape().height;
	}

	@Override
	default boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
		if(pKeyCode == InputConstants.KEY_ESCAPE) {
			getBaseGui().removeSubGui();
		}
		return ContainerEventHandler.super.keyPressed(pKeyCode, pScanCode, pModifiers);
	}

	default List<Component> getTooltip(int mouseX, int mouseY) {
		for(GuiEventListener child : children()) {
			if(!(child instanceof IElement element)) continue;
			if(element.isMouseOver(mouseX, mouseY) && !element.getTooltip(mouseX, mouseY).isEmpty()) {
				return element.getTooltip(mouseX, mouseY);
			}
		}
		return List.of();
	}

	default void onClose() {
		for(GuiEventListener child : children()) {
			if(!(child instanceof IElement))  continue;
			((IElement) child).onClose();
		}
	}

	default IElement[] getParentGuiPath() {
		ArrayList<IElement> path = new ArrayList<>();
		IElement thisElement = this;
		while(thisElement != null) {
			path.add(thisElement);
			thisElement = thisElement.getParentComponent();
		}
		return path.toArray(new IElement[0]);
	}

	default IBaseGui getBaseGui() {
		IElement[] parentPath = getParentGuiPath();
		IElement baseGui = parentPath[parentPath.length - 1];
		if(!(baseGui instanceof IBaseGui)) {
			Robotics.LOGGER.error("Opened Gui is not a Robotics Gui!");
			return null; //Not recoverable -> Return null, which should forge make the gui close
		}
		return (IBaseGui) baseGui;
	}

	default List<Rect2i> getBlockingAreas() {
		List<Rect2i> blockedAreas = new ArrayList<>();
		if(!(this instanceof BaseContainerScreen)) {
			blockedAreas.add(new Rect2i(getShape().x, getShape().y, getShape().width, getShape().height));
		}
		for(var comp : children()) {
			if(comp instanceof IElement element) {
				blockedAreas.addAll(element.getBlockingAreas());
			}
		}
		return blockedAreas;
	}

}
