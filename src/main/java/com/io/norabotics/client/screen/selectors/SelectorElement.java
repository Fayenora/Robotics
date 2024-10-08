package com.io.norabotics.client.screen.selectors;

import com.io.norabotics.Robotics;
import com.io.norabotics.client.screen.base.IElement;
import com.io.norabotics.client.screen.elements.ButtonElement;
import com.io.norabotics.common.helpers.types.Selection;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.awt.*;
import java.util.List;

@OnlyIn(Dist.CLIENT)
@MethodsReturnNonnullByDefault
public abstract class SelectorElement<A> extends ButtonElement {

    public static final ResourceLocation TEXTURE = new ResourceLocation(Robotics.MODID, "textures/gui/selectors.png");
    public static final Dimension BOUNDS = new Dimension(18, 18);

    protected Selection<A> selection;
    protected float angle = 0;

    public SelectorElement(Selection<A> selection, int x, int y) {
        super(x, y, BOUNDS.width, BOUNDS.height);
        initTextureLocation(TEXTURE, 0, 164);
        this.selection = selection;
    }

    protected abstract IElement getMaximizedVersion();

    public abstract void renderSelection(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks);

    @Override
    public void onPress() {
        super.onPress();
        getBaseGui().addSubGui(getMaximizedVersion());
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.renderWidget(graphics, pMouseX, pMouseY, pPartialTick);
        angle += pPartialTick * 5;
        angle %= 360;
        if(currentGuiActive()) {
            renderSelection(graphics, pMouseX, pMouseY, pPartialTick);
        }
    }

    /**
     * Check whether there is a subGui in front of the gui this selector is on
     * @return whether the current gui is in front
     */
    protected boolean currentGuiActive() {
        IElement currentSubGui = getBaseGui().getSubGui();
        for(IElement parent : getParentGuiPath()) {
            if(parent.equals(currentSubGui)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void addElement(IElement element) {
        //NO-OP
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return List.of();
    }

    public Selection<A> getSelection() {
        return selection;
    }
}
