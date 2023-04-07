package com.ignis.igrobotics.client.screen.elements;

import com.ignis.igrobotics.Reference;
import com.ignis.igrobotics.Robotics;
import com.ignis.igrobotics.client.screen.base.GuiElement;
import com.ignis.igrobotics.core.util.Lang;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.function.Supplier;

public class EnergyBarElement extends GuiElement {

    private Supplier<Integer> energy, maxEnergy;

    public EnergyBarElement(int x, int y, int height, Supplier<Integer> energy, Supplier<Integer> maxEnergy) {
        this(Component.translatable("gui.energy_bar"), x, y, height, energy, maxEnergy);
    }

    public EnergyBarElement(Component name, int x, int y, int height, Supplier<Integer> energy, Supplier<Integer> maxEnergy) {
        super(name, x, y, 13, height);
        this.energy = energy;
        this.maxEnergy = maxEnergy;
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, Reference.ENERGY_BAR);
        int k = scaleEnergy(height);
        this.blit(poseStack, getX(), getY() + height - k, 0, height - k, width, k);
    }

    @Override
    public List<Component> getTooltip(int mouseX, int mouseY) {
        List line = List.of(Lang.localise("stored_energy"), Component.literal(": "+ energy.get() + "/" + maxEnergy.get() + " RF"));
        return List.of(ComponentUtils.formatList(line, CommonComponents.EMPTY));
    }

    protected int scaleEnergy(int pixels) {
        int max = maxEnergy.get();
        int current = energy.get();
        if(max <= 0) return 0;
        return Math.min(pixels, pixels * current / max);
    }
}
