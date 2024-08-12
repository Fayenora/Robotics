package com.ignis.norabotics.client.screen.elements;

import com.ignis.norabotics.Reference;
import com.ignis.norabotics.Robotics;
import com.ignis.norabotics.client.screen.base.GuiElement;
import com.ignis.norabotics.common.helpers.util.RenderUtil;
import com.ignis.norabotics.common.helpers.util.StringUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.awt.*;
import java.text.DecimalFormat;

@OnlyIn(Dist.CLIENT)
public class AttributeElement extends GuiElement {

    public static final ResourceLocation TEXTURE = new ResourceLocation(Robotics.MODID, "textures/gui/robot_info.png");
    public static final DecimalFormat FORMAT = new DecimalFormat("#.##");

    private final Color color;
    private final Component attribute;
    private final String value;
    private int shimmer = 0;

    public AttributeElement(int x, int y, Attribute attribute, float value) {
        super(x, y, 67, 15);
        this.attribute = Component.translatable(attribute.getDescriptionId());
        this.color = new Color(Reference.ATTRIBUTE_COLORS.getOrDefault(attribute, TextColor.fromRgb(Reference.FONT_COLOR)).getValue());
        this.value = StringUtil.prettyPrintLargeNumber(value, FORMAT);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.enableBlend();
        RenderSystem.enableColorLogicOp();
        graphics.setColor(color.getRed() / 256f, color.getGreen() / 256f, color.getBlue() / 256f, 1);
        graphics.blit(TEXTURE, getX(), getY() + 2, 0, 182, 67, 11);
        RenderUtil.drawString(graphics, attribute, getX() + 3, getY() + 5, Reference.FONT_COLOR, 0.6f, 43);
        RenderUtil.drawString(graphics, value, getX() + 47, getY() + 5, Reference.FONT_COLOR, 0.6f, 15);
        if(shimmer > 0) {
            graphics.setColor(1, 1, 1, 0.5f);
            graphics.blit(TEXTURE, getX() + shimmer, getY() + 3, 0, 193, 5, 9);
            shimmer++;
            shimmer %= width - 5;
        } else if(Robotics.RANDOM.nextDouble() > 0.999) {
            shimmer++;
        }
        RenderSystem.disableBlend();
        RenderSystem.disableColorLogicOp();
        graphics.setColor(1, 1, 1, 1);
    }
}
