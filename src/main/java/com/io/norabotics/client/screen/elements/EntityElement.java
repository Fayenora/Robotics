package com.io.norabotics.client.screen.elements;

import com.io.norabotics.common.helpers.util.MathUtil;
import com.io.norabotics.common.helpers.util.RenderUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.function.Supplier;

import static com.io.norabotics.client.screen.selectors.SelectorElement.BOUNDS;
import static com.io.norabotics.client.screen.selectors.SelectorElement.TEXTURE;

@OnlyIn(Dist.CLIENT)
public class EntityElement extends ButtonElement {

    LivingEntity living;
    Supplier<Float> angle;

    public EntityElement(LivingEntity living, int x, int y, Supplier<Float> angle, Button.OnPress onPress) {
        super(x, y, BOUNDS.width, BOUNDS.height, onPress);
        initSingleTextureLocation(TEXTURE, 0, 164);
        setTooltip(living.getName());
        this.living = living;
        this.angle = angle;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.renderWidget(graphics, pMouseX, pMouseY, pPartialTick);
        RenderUtil.enableScissor(graphics, MathUtil.downsizeRect(getShape(), 1));
        RenderUtil.drawRotatingEntity(graphics.pose(), getX() + BOUNDS.width / 2, getY() + 6 + BOUNDS.height / 2, (int) (8 / living.getBoundingBox().getSize()), living, angle.get());
        RenderUtil.disableScissor(graphics);
    }
}
