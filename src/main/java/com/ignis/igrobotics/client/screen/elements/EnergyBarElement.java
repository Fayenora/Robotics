package com.ignis.igrobotics.client.screen.elements;

import com.ignis.igrobotics.Reference;
import com.ignis.igrobotics.client.screen.base.GuiElement;
import com.ignis.igrobotics.core.util.Lang;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
@MethodsReturnNonnullByDefault
public class EnergyBarElement extends GuiElement {

    private final Supplier<Integer> energy, maxEnergy;

    public EnergyBarElement(int x, int y, int height, Supplier<Integer> energy, Supplier<Integer> maxEnergy) {
        this(Component.translatable("gui.energy_bar"), x, y, height, energy, maxEnergy);
    }

    public EnergyBarElement(Component name, int x, int y, int height, Supplier<Integer> energy, Supplier<Integer> maxEnergy) {
        super(name, x, y, 13, height);
        this.energy = energy;
        this.maxEnergy = maxEnergy;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int k = scaleEnergy(height);
        graphics.blit(Reference.ENERGY_BAR, getX(), getY() + height - k, 0, height - k, width, k);
    }

    @Override
    public List<Component> getTooltip(int mouseX, int mouseY) {
        List<Component> line = List.of(Lang.localise("stored_energy"), Component.literal(": "+ energy.get() + "/" + maxEnergy.get() + " RF"));
        return List.of(ComponentUtils.formatList(line, CommonComponents.EMPTY));
    }

    protected int scaleEnergy(int pixels) {
        int max = maxEnergy.get();
        int current = energy.get();
        if(max <= 0) return 0;
        return Math.min(pixels, pixels * current / max);
    }
}
