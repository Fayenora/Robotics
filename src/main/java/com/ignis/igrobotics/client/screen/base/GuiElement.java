package com.ignis.igrobotics.client.screen.base;

import com.ignis.igrobotics.client.screen.base.IElement;
import com.ignis.igrobotics.client.screen.base.IGuiTexturable;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class GuiElement extends AbstractContainerEventHandler implements IElement, IGuiTexturable {

    private int x, y;
    public int width, height;
    private boolean enabled = true, visible = true;
    private final List<GuiEventListener> children = new CopyOnWriteArrayList<>();
    private IElement parentComponent;
    private Component name;
    protected ResourceLocation resource;
    protected Point textureLoc;

    private float tickCounter;

    public GuiElement(int x, int y, int width, int height) {
        this(CommonComponents.EMPTY, x, y, width, height);
    }

    public GuiElement(Component name, int x, int y, int width, int height) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    @Override
    public void setX(int x) {
        for(GuiEventListener b : children()) {
            if(b instanceof IElement element) {
                element.setX(element.getShape().x + x - this.x);
            } else if(b instanceof AbstractWidget widget) {
                widget.setX(widget.getX() + x - this.x);
            }
        }
        this.x = x;
    }

    @Override
    public void setY(int y) {
        for(GuiEventListener b : children()) {
            if(b instanceof IElement element) {
                element.setY(element.getShape().y + y - this.y);
            } else if(b instanceof AbstractWidget widget) {
                widget.setY(widget.getY() + y - this.y);
            }
        }
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
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
    public void setParentComponent(IElement comp) {
        this.parentComponent = comp;
    }

    @Override
    public @Nullable IElement getParentComponent() {
        return parentComponent;
    }

    @Override
    public void initTextureLocation(ResourceLocation texture, int textureX, int textureY) {
        this.resource = texture;
        this.textureLoc = new Point(textureX, textureY);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        tickCounter += delta;
        RenderSystem.enableDepthTest();
        poseStack.translate(0, 0, 1);
        if(resource != null) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShaderTexture(0, resource);
            blit(poseStack, getX(), getY(), textureLoc.x, textureLoc.y, getShape().width, getShape().height);
        }
        for(var child : children()) {
            if(child instanceof Renderable renderable) {
                renderable.render(poseStack, mouseX, mouseY, delta);
            }
        }
        RenderSystem.disableDepthTest();
        if(tickCounter >= 5) {
            tickCounter = 0;
            if(getFocused() instanceof EditBox editBox) {
                editBox.tick();
            }
        }
    }

    @Override
    public void addElement(IElement element) {
        children.add(element);
        element.setParentComponent(this);
    }

    public void addElement(GuiEventListener element) {
        children.add(element);
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return this.children;
    }

    @Override
    public NarrationPriority narrationPriority() {
        return NarrationPriority.HOVERED;
    }

    @Override
    public void updateNarration(NarrationElementOutput narrOutput) {
        narrOutput.add(NarratedElementType.TITLE, Component.translatable("gui.narrate.button", name));
    }
}
