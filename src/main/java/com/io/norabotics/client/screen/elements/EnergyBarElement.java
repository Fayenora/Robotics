package com.io.norabotics.client.screen.elements;

import com.io.norabotics.Reference;
import com.io.norabotics.client.screen.base.GuiElement;
import com.io.norabotics.common.helpers.util.Lang;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.List;

@OnlyIn(Dist.CLIENT)
@MethodsReturnNonnullByDefault
public class EnergyBarElement extends GuiElement {

    private final IEnergyStorage energyStorage;

    public EnergyBarElement(IEnergyStorage energyStorage, int x, int y, int height) {
        this(Component.translatable("gui.energy_bar"), energyStorage, x, y, height);
    }

    public EnergyBarElement(Component name, IEnergyStorage energyStorage, int x, int y, int height) {
        super(name, x, y, 13, height);
        this.energyStorage = energyStorage;
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
        List<Component> line = List.of(Lang.localise("stored_energy"), Component.literal(": "+ energyStorage.getEnergyStored() + "/" + energyStorage.getMaxEnergyStored() + " RF"));
        return List.of(ComponentUtils.formatList(line, CommonComponents.EMPTY));
    }

    protected int scaleEnergy(int pixels) {
        if(energyStorage.getMaxEnergyStored() <= 0) return 0;
        return Math.min(pixels, pixels * energyStorage.getEnergyStored() / energyStorage.getMaxEnergyStored());
    }
}
