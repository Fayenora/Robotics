package com.ignis.igrobotics.client.screen;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import com.ignis.igrobotics.network.messages.IMessage;
import com.ignis.igrobotics.network.messages.NetworkHandler;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;

/**
 * Introduces a component based gui system, where gui components can contain other components. <br>
 * Vanilla does this now with GuiEventListeners & Widgets! Also see {@link net.minecraft.client.gui.components.events.AbstractContainerEventHandler}
 * @author Ignis
 */
@Deprecated //
public interface IComponent extends GuiEventListener, Widget {
	
	void setX(int x);
	void setY(int y);
	Rectangle getShape();
	
	void setEnabled(boolean enabled);
	void setVisible(boolean visible);
	boolean isEnabled();
	boolean isVisible();
	
	void addComponent(IComponent comp);
	
	<T extends IComponent> List<T> getComponents();
	
	void setParentComponent(IComponent comp);
	
	IComponent getParentComponent();
	
	default boolean isHovered(double mouseX, double mouseY) {
		return mouseX >= getShape().x && mouseY >= getShape().y && mouseX < getShape().x + getShape().width && mouseY < getShape().y + getShape().height;
	}

	@Override
	default void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
		for(IComponent comp : getComponents()) {
			comp.render(poseStack, mouseX, mouseY, partialTicks);
		}
	}
	
	default ArrayList<String> getTooltip(int mouseX, int mouseY) {
		for(IComponent comp : getComponents()) {
			if(comp.isHovered(mouseX, mouseY) && comp.getTooltip(mouseX, mouseY) != null) {
				return comp.getTooltip(mouseX, mouseY);
			}
		}
		return null;
	}
	
	default void updateScreen() {
		for(IComponent comp : getComponents()) {
			comp.updateScreen();
		}
	}
	
	default void onClosed() {
		for(IComponent comp : getComponents()) {
			comp.onClosed();
		}
	}
	
	/**
	 * Called when clicked with any mouse button
	 * @param mouseX
	 * @param mouseY
	 * @return false, when a component button on this button has been clicked, true if this is not the case
	 */
	@Override
	default boolean mouseClicked(double mouseX, double mouseY, int button) {
		if(getNetworkAction() != null) {
			NetworkHandler.sendToServer(getNetworkAction());
		}
		
		for(IComponent comp : getComponents()) {
			if(comp.isHovered(mouseX, mouseY)) {
				comp.mouseClicked(mouseX, mouseY, button);
				return false;
			}
		}
		
		return true;
	}

	@Override
	default boolean mouseScrolled(double mouseX, double mouseY, double distance) {
		for(IComponent comp : getComponents()) {
			if(comp.isHovered(mouseX, mouseY) && comp.mouseScrolled(mouseX, mouseY, distance)) {
				return true;
			}
		}
		return false;
	}

	@Override
	default boolean charTyped(char typedChar, int keyCode) {
		for(IComponent comp : getComponents()) {
			if(comp.charTyped(typedChar, keyCode)) {
				return true;
			}
		}
		return false;
	}
	
	default IMessage getNetworkAction() {
		return null;
	}
	
	default IComponent[] getParentGuiPath() {
		ArrayList<IComponent> path = new ArrayList<IComponent>();
		IComponent search = this;
		while(search != null) {
			path.add(search);
			search = search.getParentComponent();
		}
		return path.toArray(new IComponent[path.size()]);
	}

}
