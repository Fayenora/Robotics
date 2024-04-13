package com.ignis.igrobotics.client.screen.elements;

import com.ignis.igrobotics.client.screen.base.GuiElement;
import com.ignis.igrobotics.client.screen.base.IElement;
import com.ignis.igrobotics.core.util.RenderUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class ScrollableElement extends GuiElement {

    private int scrollDistanceX;
    private int scrollDistanceY;
    /** The total width of everything in the scrollable area. This implementation does not support extending this range */
    private final int scrollMaxX;
    /** The total height of everything in the scrollable area */
    private int scrollMaxY = 0;

    private final List<Integer> toRemove = new ArrayList<>();

    public ScrollableElement(int x, int y, int width, int height) {
        super(x, y, width, height);
        setVisible(false);
        scrollMaxX = width;
    }

    @Override
    public void addElement(IElement element) {
        if(alignElement(element)) {
            super.addElement(element);
        }
    }

    public void removeComponent(int index) {
        toRemove.add(index);
        toRemove.sort(Comparator.reverseOrder());
    }

    public void removeComponent(IElement comp) {
        removeComponent(children().indexOf(comp));
    }

    /**
     * This ensures only one element is removed at a time. To clear all elements use {@link #clear()}
     * @param index the index of the element to be removed
     */
    private void internalRemove(int index) {
        if(index > children().size()) return;
        children().remove(index);
        for(int i = index; i < children().size(); i++) {
            alignElement(i);
        }
        updateScrollMax();
    }

    public void clear() {
        children().clear();
        updateScrollMax();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        for(int index : toRemove) {
            internalRemove(index);
        }
        toRemove.clear();
        RenderUtil.enableScissor(graphics, getShape());
        super.render(graphics, mouseX, mouseY, delta);
        RenderUtil.disableScissor(graphics);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if(delta == 0) return false;

        if(Screen.hasShiftDown()) {
            int scroll_diff = scrollDistanceX;

            scrollDistanceX += delta / -10;
            scrollDistanceX = Math.max(Math.min(scrollDistanceX, scrollMaxX - width), 0);

            scroll_diff = scroll_diff - scrollDistanceX;

            for(GuiEventListener listener : children()) {
                if(!(listener instanceof IElement element)) continue;
                int newX = element.getShape().x + scroll_diff;
                boolean shouldBeActive = getX() < newX + element.getShape().width && newX < getX() + width;
                element.element$setX(newX);
                element.setEnabled(shouldBeActive);
            }
        } else {
            int scroll_diff = scrollDistanceY;

            scrollDistanceY += delta * -10;
            scrollDistanceY = Math.max(Math.min(scrollDistanceY, scrollMaxY - height), 0);

            scroll_diff = scroll_diff - scrollDistanceY;

            for(GuiEventListener listener : children()) {
                if(!(listener instanceof IElement element)) continue;
                int newY = element.getShape().y + scroll_diff;
                boolean shouldBeActive = getY() < newY + element.getShape().height && newY < getY() + height;
                element.element$setY(newY);
                element.setEnabled(shouldBeActive);
            }
        }
        return true;
    }

    /**
     * Align a new component
     * @param element  to add
     * @return true if successful
     */
    private boolean alignElement(IElement element) {
        return alignElement(element, children().size());
    }

    /**
     * Align an existing component by index
     * @param index may be in range [0, componentCount]
     * @return true if successful
     */
    private boolean alignElement(int index) {
        return alignElement((IElement) children().get(index), index);
    }

    /**
     * Align a component to an index
     * @param element to add
     * @param index may be in range [0, componentCount]
     * @return true if successful
     */
    private boolean alignElement(IElement element, int index) {
        if(element.getShape().width > scrollMaxX) return false; //Component doesn't fit at all

        int alignX, alignY;
        if(index > 0) {
            Rectangle lastComponent = ((IElement) children().get(index - 1)).getShape();
            alignX = lastComponent.x + lastComponent.width;
            alignY = lastComponent.y;
            //Component doesn't fit into current row
            if(getX() + scrollMaxX - alignX < element.getShape().width) {
                setScrollMaxY(scrollMaxY + element.getShape().height);
                alignX = getX();
                alignY += lastComponent.height;
            }
        } else {
            setScrollMaxY(scrollMaxY + element.getShape().height);
            alignX = getX();
            alignY = getY();
        }

        element.element$setX(alignX);
        element.element$setY(alignY);
        return true;
    }

    /**
     * Force update scroll maximum. O(n) with n - number of components
     */
    private void updateScrollMax() {
        int highestY = getY();
        for(GuiEventListener listener : children()) {
            if(listener instanceof IElement comp) {
                highestY = Math.max(highestY, comp.getShape().y + comp.getShape().height);
            }
        }
        setScrollMaxY(highestY - getY());
    }

    protected void setScrollMaxY(int scrollMaxY) {
        this.scrollMaxY = scrollMaxY;
    }
}
