package com.ignis.igrobotics.client.screen.elements;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A Widget with defined shape, visibility and parent component. Slightly extends on the GuiEventHandler.
 * In contrast to {@link net.minecraft.client.gui.components.AbstractWidget} this can hold children components which are handled properly
 * @author Ignis
 */
@MethodsReturnNonnullByDefault
public interface IElement extends ContainerEventHandler, Renderable, NarratableEntry {

	void setX(int x);
	void setY(int y);
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

	default List<Component> getTooltip(int mouseX, int mouseY) {
		for(GuiEventListener child : children()) {
			if(!(child instanceof IElement))  continue;
			IElement element = (IElement) child;
			if(element.isMouseOver(mouseX, mouseY) && element.getTooltip(mouseX, mouseY) != null) {
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
		IElement search = this;
		while(search != null) {
			path.add(search);
			search = search.getParentComponent();
		}
		return path.toArray(new IElement[path.size()]);
	}

}
