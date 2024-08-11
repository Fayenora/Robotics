package com.ignis.igrobotics.client.screen.elements;

import com.ignis.igrobotics.Reference;
import com.ignis.igrobotics.client.screen.base.GuiElement;
import com.ignis.igrobotics.client.screen.base.IElement;
import com.ignis.igrobotics.common.helpers.util.RenderUtil;
import com.ignis.igrobotics.common.helpers.util.StringUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.awt.*;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class DialogElement extends GuiElement {

    public static final float OPTIONS_PART = 0.45f;
    public static final float TEXT_SIZE = 0.6f;

    String text;
    List<String> texts;

    /**
     * A dialogue box. Automatically takes the center of the component it is added on. <br>
     * Added components are automatically presented as options and aligned in the bottom {@link #OPTIONS_PART 45%} of the box.
     * @param width width of the box
     * @param height height of the box
     * @param text The text displayed in the box
     */
    public DialogElement(int width, int height, Component text) {
        super(0, 0, width, height);
        this.text = text.getString();
    }

    @Override
    public void setParentComponent(IElement comp) {
        super.setParentComponent(comp);
    }

    @Override
    public void addElement(IElement element) {
        super.addElement(element);
        Rectangle thisShape = this.getShape();
        thisShape.y += thisShape.height * (1 - OPTIONS_PART);
        thisShape.height *= OPTIONS_PART;

        Rectangle[] buttonSpaces = new Rectangle[children().size()];
        int width = thisShape.width / buttonSpaces.length;
        int i = 0;
        for(GuiEventListener child : children()) {
            if(!(child instanceof IElement el)) continue;
            buttonSpaces[i] = new Rectangle(i * width, thisShape.y, width, thisShape.height);
            center(el, buttonSpaces[i]);
            i++;
        }
    }

    /**
     * Center component c on the rectangle r
     * @param c the component
     * @param r the rectangle
     */
    private static void center(IElement c, Rectangle r) {
        c.element$setX(r.x + (r.width / 2) - (c.getShape().width / 2));
        c.element$setY(r.y + (r.height / 2) - (c.getShape().height / 2));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.render(graphics, mouseX, mouseY, delta);
        Font font = Minecraft.getInstance().font;
        if(texts == null) {
            texts = StringUtil.calculateStringSplit(font, text, (int) (width * (1 / TEXT_SIZE)));
        }

        Rectangle thisShape = this.getShape();
        thisShape.y += 5;
        thisShape.height *= (1 - OPTIONS_PART);
        thisShape.height -= 5;



        int offset = (thisShape.height / texts.size() - Minecraft.getInstance().font.lineHeight) / 2 + 5;
        for(int i = 0; i < texts.size(); i++) {
            int y_offset = i * (thisShape.height / texts.size());
            RenderUtil.drawCenteredString(graphics, texts.get(i), getX() + (width / 2), getY() + y_offset + offset, Reference.FONT_COLOR, TEXT_SIZE, width - 10);
        }
    }
}
