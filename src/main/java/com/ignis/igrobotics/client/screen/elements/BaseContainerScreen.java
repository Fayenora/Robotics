package com.ignis.igrobotics.client.screen.elements;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.Optional;
import java.util.Stack;

/**
 * Supports features of IElement: tooltips & closing callback
 * Supports holding a stack of opened windows
 * @param <T>
 */
public abstract class BaseContainerScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> implements IElement {

    private Stack<IElement> subGuis = new Stack<>();

    public BaseContainerScreen(T menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
    }

    /////////////////
    // Rendering
    /////////////////

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        super.render(poseStack, mouseX, mouseY, delta);

        if(hasSubGui()) {
            for(IElement comp : subGuis) {
                renderBackground(poseStack);
                comp.render(poseStack, mouseX, mouseY, delta);
            }
        }
    }

    @Override
    protected void renderTooltip(PoseStack poseStack, int mouseX, int mouseY) {
        super.renderTooltip(poseStack, mouseX, mouseY);
        //Only show hovered tooltips of the current subGui (may be this gui itself)
        Optional<GuiEventListener> child = getSubGui().getChildAt(mouseX, mouseY);
        if(child.isPresent() && child.get() instanceof IElement) {
            IElement element = (IElement) child.get();
            if(element.isEnabled()) {
                renderTooltip(poseStack, element.getTooltip(mouseX, mouseY), Optional.empty(), mouseX, mouseY);
            }
        }
    }

    //////////////////////////////
    // Adding & Removing Components
    //////////////////////////////

    @Override
    public void addElement(IElement element) {
        if(element instanceof Renderable) {
            addRenderableWidget(element);
        }
        if(element instanceof GuiEventListener && element instanceof NarratableEntry) {
            addWidget(element);
        }
    }

    @Override
    protected <T extends Renderable> T addRenderableOnly(T renderable) {
        if(renderable instanceof IElement element) {
            element.setParentComponent(this);
        }
        return super.addRenderableOnly(renderable);
    }

    @Override
    protected <T extends GuiEventListener & NarratableEntry> T addWidget(T widget) {
        if(widget instanceof IElement element) {
            element.setParentComponent(this);
        }
        return super.addWidget(widget);
    }

    @Override
    protected void clearWidgets() {
        subGuis.clear();
        super.clearWidgets();
    }

    /////////////////
    // Hooks
    /////////////////


    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if(hasSubGui()) {
            return getSubGui().keyPressed(pKeyCode, pScanCode, pModifiers);
        }
        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
        if(hasSubGui()) {
            getSubGui().mouseScrolled(pMouseX, pMouseY, pDelta);
        }
        return super.mouseScrolled(pMouseX, pMouseY, pDelta);
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if(hasSubGui()) {
            return getSubGui().mouseClicked(pMouseX, pMouseY, pButton);
        }
        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    @Override
    public void onClose() {
        for(GuiEventListener child : children()) {
            if(!(child instanceof IElement)) continue;
            ((IElement) child).onClose();
        }
        super.onClose();
    }

    /////////////////
    // Implementations
    /////////////////

    public void addSubGui(IElement subGui) {
        //Add the next subGui
        subGuis.push(subGui);
        subGui.setX((width - subGui.getShape().width) / 2);
        subGui.setY((height - subGui.getShape().height) / 2);
        addElement(subGui);
    }

    public void removeSubGui() {
        IElement toRemove = subGuis.pop();
        toRemove.onClose();
        removeWidget(toRemove);
    }

    public boolean hasSubGui() {
        return subGuis.size() > 0;
    }

    public IElement getSubGui() {
        if(subGuis.size() == 0) return this;
        return subGuis.peek();
    }

    @Override
    public void setX(int x) {
        for(GuiEventListener b : children()) {
            if(!(b instanceof IElement)) continue;
            IElement element = (IElement) b;
            element.setX(element.getShape().x + x - this.leftPos);
        }
        leftPos = x;
    }

    @Override
    public void setY(int y) {
        for(GuiEventListener b : children()) {
            if(!(b instanceof IElement)) continue;
            IElement element = (IElement) b;
            element.setY(element.getShape().y + y - this.topPos);
        }
        topPos = y;
    }

    @Override
    public Rectangle getShape() {
        return new Rectangle(leftPos, topPos, width, height);
    }

    @Override
    public void setEnabled(boolean enabled) {}

    @Override
    public void setVisible(boolean visible) {}

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean isVisible() {
        return true;
    }

    @Override
    public void setParentComponent(IElement comp) {}

    @Override
    public @Nullable IElement getParentComponent() {
        return null;
    }

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public NarrationPriority narrationPriority() {
        return NarrationPriority.NONE;
    }

    @Override
    public void updateNarration(NarrationElementOutput p_169152_) {}
}
