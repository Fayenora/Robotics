package com.ignis.igrobotics.client.screen.elements;

import com.ignis.igrobotics.core.util.MathUtil;
import com.ignis.igrobotics.core.util.RenderUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.function.Supplier;

import static com.ignis.igrobotics.client.screen.selectors.SelectorElement.BOUNDS;
import static com.ignis.igrobotics.client.screen.selectors.SelectorElement.TEXTURE;

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
    public void renderWidget(PoseStack poseStack, int pMouseX, int pMouseY, float pPartialTick) {
        super.renderWidget(poseStack, pMouseX, pMouseY, pPartialTick);
        RenderUtil.enableScissor(MathUtil.downsizeRect(getShape(), 1));
        RenderUtil.drawRotatingEntity(poseStack, getX() + BOUNDS.width / 2, getY() + 6 + BOUNDS.height / 2, (int) (8 / living.getBoundingBox().getSize()), living, angle.get());
        RenderUtil.disableScissor();
    }
}
