package com.ignis.igrobotics.client.screen.elements;

import com.ignis.igrobotics.client.screen.base.IElement;
import com.ignis.igrobotics.client.screen.base.IGuiTexturable;
import com.ignis.igrobotics.network.messages.IMessage;
import com.ignis.igrobotics.network.NetworkHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

public class ButtonElement extends Button implements IElement, IGuiTexturable {

    protected ResourceLocation resource;
    protected Point[][] icons;
    private int state;
    private final int maxStates;
    private final List<Component>[] tooltips;
    private Supplier<IMessage> networkAction;

    private boolean dragging;
    private final List<IElement> components = new CopyOnWriteArrayList<>();
    private IElement parentComponent;
    private GuiEventListener focused;

    public ButtonElement(int x, int y, int widthIn, int heightIn, int currentState, int maxStates) {
        this(x, y, widthIn, heightIn, currentState, maxStates, button -> {});
    }

    public ButtonElement(int x, int y, int widthIn, int heightIn, int currentState, int maxStates, Button.OnPress onPress) {
        this(x, y, widthIn, heightIn, Component.empty(), currentState, maxStates, onPress);
    }

    public ButtonElement(int pX, int pY, int pWidth, int pHeight) {
        this(pX, pY, pWidth, pHeight, button -> {});
    }

    public ButtonElement(int pX, int pY, int pWidth, int pHeight, Button.OnPress onPress) {
        this(pX, pY, pWidth, pHeight, Component.empty(), onPress);
    }

    public ButtonElement(int pX, int pY, int pWidth, int pHeight, Component pMessage, Button.OnPress onPress) {
        this(pX, pY, pWidth, pHeight, pMessage, 0, 1, onPress);
    }

    public ButtonElement(int pX, int pY, int pWidth, int pHeight, Component pMessage, int currentState, int maxStates, Button.OnPress onPress) {
        super(pX, pY, pWidth, pHeight, pMessage, onPress, DEFAULT_NARRATION);
        this.state = currentState;
        this.maxStates = maxStates;
        tooltips = new List[getMaxStates()];
    }

    @Override
    public void onPress() {
        //Client stuff
        nextState();
        onPress.onPress(this);
        //Server stuff
        if(networkAction != null) {
            NetworkHandler.sendToServer(networkAction.get());
        }
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        GuiEventListener targeted = null;

        for(GuiEventListener child : List.copyOf(this.children())) {
            if (child.mouseClicked(pMouseX, pMouseY, pButton)) {
                targeted = child;
            }
        }

        if (targeted != null) {
            this.setFocused(targeted);
            if (pButton == 0) {
                this.setDragging(true);
            }
            return true;
        }

        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    @Override
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        for(var comp : children()) {
            if(!(comp instanceof Renderable renderable)) continue;
            renderable.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        }
    }

    @Override
    public void renderWidget(PoseStack poseStack, int pMouseX, int pMouseY, float pPartialTick) {
        if(icons == null) return;
        Point[] currentIcons = icons[state];

        int color = getFGColor();
        int i = currentIcons[0].x;
        int j = currentIcons[0].y;
        if(this.active) {
            i = currentIcons[1].x;
            j = currentIcons[1].y;

            if(this.isHovered) {
                i = currentIcons[2].x;
                j = currentIcons[2].y;
            }
        }

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, resource);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        this.blit(poseStack, getX(), getY(), i, j, width, height);

        drawCenteredString(poseStack, Minecraft.getInstance().font, getMessage(), getX() + this.width / 2, getY() + (height - 8) / 2, color | Mth.ceil(this.alpha * 255.0F) << 24);
    }

    public int getState() {
        return state;
    }

    public int getMaxStates() {
        return maxStates;
    }

    public void nextState() {
        if(icons == null) return;
        if(state < icons.length - 1) {
            state += 1;
        } else state = 0;
    }

    public void initSingleTextureLocation(ResourceLocation resource, int x, int y) {
        this.resource = resource;
        icons = new Point[1][];
        icons[0] = new Point[3];
        for(int i = 0; i < 3; i++) {
            icons[0][i] = new Point(x, y);
        }
    }

    @Override
    public void initTextureLocation(ResourceLocation resource, int x, int y) {
        this.resource = resource;
        this.icons = new Point[getMaxStates()][];
        for(int i = 0; i < getMaxStates(); i++) {
            icons[i] = new Point[3];
            for(int j = 0; j < 3; j++) {
                icons[i][j] = new Point(x + j * width, y + i * height);
            }
        }
    }

    public void setNetworkAction(Supplier<IMessage> action) {
        this.networkAction = action;
    }

    public void setTooltip(List<Component> tooltip) throws Exception {
        if(getMaxStates() > 1) throw new Exception("Unintended function use!");
        setTooltip(0, tooltip);
    }

    public void setTooltip(Component tooltip) {
        setTooltip(0, tooltip);
    }

    public void setTooltip(int state, Component tooltip) {
        setTooltip(state, List.of(tooltip));
    }

    public void setTooltip(int state, List<Component> tooltip) {
        if(state + 1 > getMaxStates()) return;
        tooltips[state] = tooltip;
    }

    @Override
    public List<Component> getTooltip(int mouseX, int mouseY) {
        if(tooltips[getState()] != null) return tooltips[getState()];
        return IElement.super.getTooltip(mouseX, mouseY);
    }

    /////////////////////////////
    // IElement implementation
    /////////////////////////////

    @Override
    public void setX(int x) {
        for(GuiEventListener b : children()) {
            if(!(b instanceof IElement element)) continue;
            element.setX(element.getShape().x + x - getX());
        }
        super.setX(x);
    }

    @Override
    public void setY(int y) {
        for(GuiEventListener b : children()) {
            if(!(b instanceof IElement element)) continue;
            element.setY(element.getShape().y + y - getY());
        }
        super.setY(y);
    }

    @Override
    public Rectangle getShape() {
        return new Rectangle(getX(), getY(), width, height);
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.active = enabled;
    }

    @Override
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @Override
    public boolean isEnabled() {
        return active;
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
    public List<? extends GuiEventListener> children() {
        return components;
    }

    @Override
    public void addElement(IElement element) {
        components.add(element);
        element.setParentComponent(this);
    }

    @Override
    public boolean isDragging() {
        return dragging;
    }

    @Override
    public void setDragging(boolean dragging) {
        this.dragging = dragging;
    }

    @Nullable
    @Override
    public GuiEventListener getFocused() {
        return focused;
    }

    @Override
    public void setFocused(@Nullable GuiEventListener focused) {
        this.focused = focused;
    }
}
