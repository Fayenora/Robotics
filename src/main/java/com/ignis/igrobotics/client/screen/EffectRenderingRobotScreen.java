package com.ignis.igrobotics.client.screen;

import com.ignis.igrobotics.client.screen.base.BaseContainerScreen;
import com.ignis.igrobotics.network.NetworkHandler;
import com.ignis.igrobotics.network.messages.server.PacketSetWatched;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.MobEffectTextureManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.client.extensions.common.IClientMobEffectExtensions;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@ParametersAreNonnullByDefault
public abstract class EffectRenderingRobotScreen<T extends AbstractContainerMenu> extends BaseContainerScreen<T> {

    private final LivingEntity entity;

    public EffectRenderingRobotScreen(T menu, Inventory playerInv, LivingEntity entity, Component title) {
        super(menu, playerInv, title);
        this.entity = entity;
        NetworkHandler.sendToServer(new PacketSetWatched(entity, true));
    }

    @Override
    public void onClose() {
        super.onClose();
        NetworkHandler.sendToServer(new PacketSetWatched(entity, false));
    }

    @Override
    public void render(GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.render(graphics, pMouseX, pMouseY, pPartialTick);
        renderEffects(graphics, entity.getActiveEffects(), pMouseX, pMouseY);
    }

    private static final int WIDTH_SMALL = 32;
    private static final int WIDTH_LARGE = 120;

    private void renderEffects(GuiGraphics graphics, Collection<MobEffectInstance> effects, int pMouseX, int pMouseY) {
        int i = this.leftPos - WIDTH_LARGE - 2;
        boolean compact = i >= 0;
        if(!compact) i = this.leftPos - WIDTH_SMALL - 2;
        if (!effects.isEmpty() && i >= 0) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            int k = 33;
            if (effects.size() > 5) {
                k = 132 / (effects.size() - 1);
            }


            Iterable<MobEffectInstance> iterable = effects.stream().filter(this::shouldRenderEffect).sorted().collect(Collectors.toList());
            this.renderBackgrounds(graphics, i, k, iterable, compact);
            this.renderIcons(graphics, i, k, iterable, compact);
            if (compact) {
                this.renderLabels(graphics, i, k, iterable);
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
                    List<Component> list = List.of(this.getEffectName(mobeffectinstance), MobEffectUtil.formatDuration(mobeffectinstance, 1.0F));
                    graphics.renderTooltip(Minecraft.getInstance().font, list, Optional.empty(), pMouseX, pMouseY);
                }
            }

        }
    }

    private boolean shouldRenderEffect(MobEffectInstance effectInstance) {
        return IClientMobEffectExtensions.of(effectInstance).isVisibleInInventory(effectInstance);
    }

    private void renderBackgrounds(GuiGraphics graphics, int pRenderX, int pYOffset, Iterable<MobEffectInstance> pEffects, boolean compact) {
        int i = this.topPos;

        for(MobEffectInstance ignored : pEffects) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            if (compact) {
                graphics.blit(INVENTORY_LOCATION, pRenderX, i, 0, 166, 120, 32);
            } else {
                graphics.blit(INVENTORY_LOCATION, pRenderX, i, 0, 198, 32, 32);
            }

            i += pYOffset;
        }

    }

    private void renderIcons(GuiGraphics graphics, int pRenderX, int pYOffset, Iterable<MobEffectInstance> pEffects, boolean p_194013_) {
        MobEffectTextureManager mobeffecttexturemanager = this.minecraft.getMobEffectTextures();
        int i = this.topPos;

        for(MobEffectInstance mobeffectinstance : pEffects) {
            MobEffect mobeffect = mobeffectinstance.getEffect();
            TextureAtlasSprite textureatlassprite = mobeffecttexturemanager.get(mobeffect);
            graphics.blit(pRenderX + (p_194013_ ? 6 : 7), i + 7, this.getGuiLeft(), 18, 18, textureatlassprite);
            i += pYOffset;
        }

    }

    private void renderLabels(GuiGraphics graphics, int pRenderX, int pYOffset, Iterable<MobEffectInstance> pEffects) {
        int i = this.topPos;

        for(MobEffectInstance mobeffectinstance : pEffects) {
            Component component = this.getEffectName(mobeffectinstance);
            graphics.drawString(this.font, component, pRenderX + 10 + 18, i + 6, 16777215);
            Component s = MobEffectUtil.formatDuration(mobeffectinstance, 1.0F);
            graphics.drawString(this.font, s, pRenderX + 10 + 18, i + 6 + 10, 8355711);
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
}
