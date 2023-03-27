package com.ignis.igrobotics.client.screen;

import com.ignis.igrobotics.client.screen.elements.IElement;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.MobEffectTextureManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

//If Mojang just added one parameter, this whole class would be obsolete...
public abstract class EffectRenderingRobotScreen<T extends AbstractContainerMenu> extends EffectRenderingInventoryScreen<T> implements IElement {

    private int x, y;
    private boolean visible, enabled;
    private IElement parentElement;
    private Supplier<Collection<MobEffectInstance>> effects;

    public EffectRenderingRobotScreen(T menu, Inventory playerInv, Supplier<Collection<MobEffectInstance>> effects, Component title) {
        super(menu, playerInv, title);
        this.effects = effects;
    }

    @Override
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        renderEffects(pPoseStack, effects.get(), pMouseX, pMouseY);
    }

    private void renderEffects(PoseStack pPoseStack, Collection<MobEffectInstance> effects, int pMouseX, int pMouseY) {
        int i = this.leftPos + this.imageWidth + 2;
        int j = this.width - i;
        if (!effects.isEmpty() && j >= 32) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            boolean flag = j >= 120;
            var event = net.minecraftforge.client.ForgeHooksClient.onScreenPotionSize(this, j, !flag, i);
            if (event.isCanceled()) return;
            flag = !event.isCompact();
            i = event.getHorizontalOffset();
            int k = 33;
            if (effects.size() > 5) {
                k = 132 / (effects.size() - 1);
            }


            Iterable<MobEffectInstance> iterable = effects.stream().filter(net.minecraftforge.client.ForgeHooksClient::shouldRenderEffect).sorted().collect(java.util.stream.Collectors.toList());
            this.renderBackgrounds(pPoseStack, i, k, iterable, flag);
            this.renderIcons(pPoseStack, i, k, iterable, flag);
            if (flag) {
                this.renderLabels(pPoseStack, i, k, iterable);
            } else if (pMouseX >= i && pMouseX <= i + 33) {
                int l = this.topPos;
                MobEffectInstance mobeffectinstance = null;

                for(MobEffectInstance mobeffectinstance1 : iterable) {
                    if (pMouseY >= l && pMouseY <= l + k) {
                        mobeffectinstance = mobeffectinstance1;
                    }

                    l += k;
                }

                if (mobeffectinstance != null) {
                    List<Component> list = List.of(this.getEffectName(mobeffectinstance), Component.literal(MobEffectUtil.formatDuration(mobeffectinstance, 1.0F)));
                    this.renderTooltip(pPoseStack, list, Optional.empty(), pMouseX, pMouseY);
                }
            }

        }
    }

    private void renderBackgrounds(PoseStack pPoseStack, int pRenderX, int pYOffset, Iterable<MobEffectInstance> pEffects, boolean p_194007_) {
        RenderSystem.setShaderTexture(0, INVENTORY_LOCATION);
        int i = this.topPos;

        for(MobEffectInstance mobeffectinstance : pEffects) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            if (p_194007_) {
                this.blit(pPoseStack, pRenderX, i, 0, 166, 120, 32);
            } else {
                this.blit(pPoseStack, pRenderX, i, 0, 198, 32, 32);
            }

            i += pYOffset;
        }

    }

    private void renderIcons(PoseStack pPoseStack, int pRenderX, int pYOffset, Iterable<MobEffectInstance> pEffects, boolean p_194013_) {
        MobEffectTextureManager mobeffecttexturemanager = this.minecraft.getMobEffectTextures();
        int i = this.topPos;

        for(MobEffectInstance mobeffectinstance : pEffects) {
            var renderer = net.minecraftforge.client.extensions.common.IClientMobEffectExtensions.of(mobeffectinstance);
            if (renderer.renderInventoryIcon(mobeffectinstance, this, pPoseStack, pRenderX + (p_194013_ ? 6 : 7), i, this.getBlitOffset())) {
                i += pYOffset;
                continue;
            }
            MobEffect mobeffect = mobeffectinstance.getEffect();
            TextureAtlasSprite textureatlassprite = mobeffecttexturemanager.get(mobeffect);
            RenderSystem.setShaderTexture(0, textureatlassprite.atlasLocation());
            blit(pPoseStack, pRenderX + (p_194013_ ? 6 : 7), i + 7, this.getBlitOffset(), 18, 18, textureatlassprite);
            i += pYOffset;
        }

    }

    private void renderLabels(PoseStack pPoseStack, int pRenderX, int pYOffset, Iterable<MobEffectInstance> pEffects) {
        int i = this.topPos;

        for(MobEffectInstance mobeffectinstance : pEffects) {
            var renderer = net.minecraftforge.client.extensions.common.IClientMobEffectExtensions.of(mobeffectinstance);
            if (renderer.renderInventoryText(mobeffectinstance, this, pPoseStack, pRenderX, i, this.getBlitOffset())) {
                i += pYOffset;
                continue;
            }
            Component component = this.getEffectName(mobeffectinstance);
            this.font.drawShadow(pPoseStack, component, (float)(pRenderX + 10 + 18), (float)(i + 6), 16777215);
            String s = MobEffectUtil.formatDuration(mobeffectinstance, 1.0F);
            this.font.drawShadow(pPoseStack, s, (float)(pRenderX + 10 + 18), (float)(i + 6 + 10), 8355711);
            i += pYOffset;
        }

    }

    private Component getEffectName(MobEffectInstance pEffect) {
        MutableComponent mutablecomponent = pEffect.getEffect().getDisplayName().copy();
        if (pEffect.getAmplifier() >= 1 && pEffect.getAmplifier() <= 9) {
            mutablecomponent.append(" ").append(Component.translatable("enchantment.level." + (pEffect.getAmplifier() + 1)));
        }

        return mutablecomponent;
    }

    /////////////////////////////
    // IElement implementation
    /////////////////////////////

    @Override
    public void setX(int x) {
        for(GuiEventListener b : children()) {
            if(!(b instanceof IElement)) continue;
            IElement element = (IElement) b;
            element.setX(element.getShape().x + x - this.x);
        }
        this.x = x;
    }

    @Override
    public void setY(int y) {
        for(GuiEventListener b : children()) {
            if(!(b instanceof IElement)) continue;
            IElement element = (IElement) b;
            element.setY(element.getShape().y + y - this.y);
        }
        this.y = y;
    }

    @Override
    public Rectangle getShape() {
        return new Rectangle(x, y, width, height);
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    @Override
    public void setParentComponent(IElement comp) {
        this.parentElement = comp;
    }

    @Override
    public @Nullable IElement getParentComponent() {
        return parentElement;
    }

    @Override
    public NarrationPriority narrationPriority() {
        return NarrationPriority.NONE;
    }

    @Override
    public void updateNarration(NarrationElementOutput pNarrationElementOutput) {

    }
}
