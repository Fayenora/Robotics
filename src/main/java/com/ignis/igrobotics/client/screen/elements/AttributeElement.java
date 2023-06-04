package com.ignis.igrobotics.client.screen.elements;

import com.ignis.igrobotics.Reference;
import com.ignis.igrobotics.Robotics;
import com.ignis.igrobotics.client.screen.base.GuiElement;
import com.ignis.igrobotics.core.util.RenderUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.Random;

@OnlyIn(Dist.CLIENT)
public class AttributeElement extends GuiElement {

    public static final ResourceLocation TEXTURE = new ResourceLocation(Robotics.MODID, "textures/gui/robot_info.png");
    public static final DecimalFormat FORMAT = new DecimalFormat("#.##");

    private final Color color;
    private final Component attribute;
    private final String value;
    private int shimmer = 0;
    private final Random r = new Random();

    public AttributeElement(int x, int y, Attribute attribute, float value) {
        super(x, y, 67, 15);
        this.attribute = Component.translatable(attribute.getDescriptionId());
        this.color = new Color(Reference.ATTRIBUTE_COLORS.getOrDefault(attribute, TextColor.fromRgb(Reference.FONT_COLOR)).getValue());
        this.value = FORMAT.format(value);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        RenderSystem.enableBlend();
        RenderSystem.enableColorLogicOp();
        RenderSystem.setShaderTexture(0, TEXTURE);
        RenderSystem.setShaderColor(color.getRed() / 256f, color.getGreen() / 256f, color.getBlue() / 256f, 1);
        blit(poseStack, getX(), getY() + 2, 0, 182, 67, 11);
        RenderUtil.drawString(poseStack, attribute, getX() + 3, getY() + 5, Reference.FONT_COLOR, 0.6f, 43);
        RenderUtil.drawString(poseStack, value, getX() + 47, getY() + 5, Reference.FONT_COLOR, 0.6f, 15);
        if(shimmer > 0) {
            RenderSystem.setShaderTexture(0, TEXTURE);
            RenderSystem.setShaderColor(1, 1, 1, 0.5f);
            blit(poseStack, getX() + shimmer, getY() + 3, 0, 193, 5, 9);
            shimmer++;
            shimmer %= width - 5;
        } else if(r.nextDouble() > 0.999) {
            shimmer++;
        }
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.disableBlend();
        RenderSystem.disableColorLogicOp();
    }
}
