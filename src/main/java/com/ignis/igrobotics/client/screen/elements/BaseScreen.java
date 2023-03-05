package com.ignis.igrobotics.client.screen.elements;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.Optional;
import java.util.Stack;

/**
 * Supports features of IElement: tooltips & closing callback
 * Supports holding a stack of opened windows
 * @param <T>
 */
public abstract class BaseScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> implements IElement {

    Stack<IElement> subGuis = new Stack<>();

    public BaseScreen(T menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
    }

    @Override
    protected void clearWidgets() {
        subGuis.clear();
        super.clearWidgets();
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        super.render(poseStack, mouseX, mouseY, delta);
        Optional<GuiEventListener> child = getChildAt(mouseX, mouseY);
        if(child.isPresent() && child.get() instanceof IElement) {
            IElement element = (IElement) child.get();
            if(element.isEnabled()) {
                renderTooltip(poseStack, element.getTooltip(mouseX, mouseY), Optional.empty(), mouseX, mouseY);
            }
        }
    }

    @Override
    public void onClose() {
        for(GuiEventListener child : children()) {
            if(!(child instanceof IElement)) continue;
            ((IElement) child).onClose();
        }
        super.onClose();
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
