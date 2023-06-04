package com.ignis.igrobotics.client.screen.base;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.awt.*;
import java.util.Optional;
import java.util.Stack;

/**
 * Supports features of IElement: tooltips & closing callback
 * Supports holding a stack of opened windows
 * @param <T>
 */
@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class BaseContainerScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> implements IElement, IBaseGui {

    private final Stack<IElement> subGuis = new Stack<>();

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
                poseStack.translate(0, 0, 100);
                comp.render(poseStack, mouseX, mouseY, delta);
            }
        }
    }

    @Override
    protected void renderTooltip(PoseStack poseStack, int mouseX, int mouseY) {
        super.renderTooltip(poseStack, mouseX, mouseY);
        //Only show hovered tooltips of the current subGui (may be this gui itself)
        Optional<GuiEventListener> child = getSubGui().getChildAt(mouseX, mouseY);
        if(child.isPresent() && child.get() instanceof IElement element) {
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
        addRenderableWidget(element);
    }

    @Override
    protected <R extends Renderable> R addRenderableOnly(R renderable) {
        if(renderable instanceof IElement element) {
            element.setParentComponent(this);
        }
        return super.addRenderableOnly(renderable);
    }

    @Override
    protected <W extends GuiEventListener & NarratableEntry> W addWidget(W widget) {
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
    public boolean charTyped(char pCodePoint, int pModifiers) {
        if(hasSubGui()) {
            return getSubGui().charTyped(pCodePoint, pModifiers);
        }
        return super.charTyped(pCodePoint, pModifiers);
    }

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
            return getSubGui().mouseScrolled(pMouseX, pMouseY, pDelta);
        }
        return super.mouseScrolled(pMouseX, pMouseY, pDelta);
    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        if(hasSubGui()) {
            return getSubGui().mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
        }
        return super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if(hasSubGui()) {
            return getSubGui().mouseClicked(pMouseX, pMouseY, pButton);
        }
        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        if(hasSubGui()) {
            return getSubGui().mouseReleased(pMouseX, pMouseY, pButton);
        }
        return super.mouseReleased(pMouseX, pMouseY, pButton);
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

    @Override
    public void addSubGui(IElement subGui) {
        //Add the next subGui
        subGuis.push(subGui);
        subGui.element$setX((width - subGui.getShape().width) / 2);
        subGui.element$setY((height - subGui.getShape().height) / 2);
        addElement(subGui);
    }

    @Override
    public void removeSubGui() {
        if(!hasSubGui()) return;
        IElement toRemove = subGuis.pop();
        toRemove.onClose();
        removeWidget(toRemove);
    }

    @Override
    public boolean hasSubGui() {
        return subGuis.size() > 0;
    }

    @Override
    public IElement getSubGui() {
        if(subGuis.size() == 0) return this;
        return subGuis.peek();
    }

    @Override
    public void element$setX(int x) {
        for(GuiEventListener b : children()) {
            if(b instanceof IElement element) {
                element.element$setX(element.getShape().x + x - this.leftPos);
            } else if(b instanceof AbstractWidget widget) {
                widget.setX(widget.getX() + x - this.leftPos);
            }
        }
        leftPos = x;
    }

    @Override
    public void element$setY(int y) {
        for(GuiEventListener b : children()) {
            if(b instanceof IElement element) {
                element.element$setY(element.getShape().y + y - this.topPos);
            } else if(b instanceof AbstractWidget widget) {
                widget.setY(widget.getY() + y - this.topPos);
            }
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
