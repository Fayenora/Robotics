package com.ignis.igrobotics.client.screen.base;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

public class BaseScreen extends Screen implements IElement {

    private int x, y;
    private boolean visible, enabled;

    private IElement parentElement;

    protected BaseScreen(int x, int y, int width, int height) {
        this(Component.empty());
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    protected BaseScreen(Component pTitle) {
        super(pTitle);
    }

    @Override
    public void element$setX(int x) {
        for(GuiEventListener b : children()) {
            if(b instanceof IElement element) {
                element.element$setX(element.getShape().x + x - this.x);
            } else if(b instanceof AbstractWidget widget) {
                widget.setX(widget.getX() + x - this.x);
            }
        }
        this.x = x;
    }

    @Override
    public void element$setY(int y) {
        for(GuiEventListener b : children()) {
            if(b instanceof IElement element) {
                element.element$setY(element.getShape().y + y - this.y);
            } else if(b instanceof AbstractWidget widget) {
                widget.setY(widget.getY() + y - this.y);
            }
        }
        this.y = y;
    }

    @Override
    public Rectangle getShape() {
        return new Rectangle(x, y, width, height);
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    @Override
    public void addElement(IElement element) {
        //NO-OP
    }

    @Override
    public void setParentComponent(IElement comp) {
        this.parentElement = comp;
    }

    @Override
    public @Nullable IElement getParentComponent() {
        return parentElement;
    }

    @Override
    public NarrationPriority narrationPriority() {
        return NarrationPriority.NONE;
    }

    @Override
    public void updateNarration(NarrationElementOutput pNarrationElementOutput) {

    }
}
