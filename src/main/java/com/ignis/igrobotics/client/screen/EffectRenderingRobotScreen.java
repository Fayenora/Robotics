package com.ignis.igrobotics.client.screen;

import com.ignis.igrobotics.client.screen.elements.BaseContainerScreen;
import com.ignis.igrobotics.network.messages.NetworkHandler;
import com.ignis.igrobotics.network.messages.server.PacketSetWatched;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
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
import net.minecraftforge.client.ForgeHooksClient;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        renderEffects(pPoseStack, entity.getActiveEffects(), pMouseX, pMouseY);
    }

    private static final int WIDTH_SMALL = 32;
    private static final int WIDTH_LARGE = 120;

    private void renderEffects(PoseStack pPoseStack, Collection<MobEffectInstance> effects, int pMouseX, int pMouseY) {
        int i = this.leftPos - WIDTH_LARGE - 2;
        boolean compact = i >= 0;
        if(!compact) i = this.leftPos - WIDTH_SMALL - 2;
        if (!effects.isEmpty() && i >= 0) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            int k = 33;
            if (effects.size() > 5) {
                k = 132 / (effects.size() - 1);
            }


            Iterable<MobEffectInstance> iterable = effects.stream().filter(ForgeHooksClient::shouldRenderEffect).sorted().collect(Collectors.toList());
            this.renderBackgrounds(pPoseStack, i, k, iterable, compact);
            this.renderIcons(pPoseStack, i, k, iterable, compact);
            if (compact) {
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

    private void renderBackgrounds(PoseStack pPoseStack, int pRenderX, int pYOffset, Iterable<MobEffectInstance> pEffects, boolean compact) {
        RenderSystem.setShaderTexture(0, INVENTORY_LOCATION);
        int i = this.topPos;

        for(MobEffectInstance ignored : pEffects) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            if (compact) {
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
}
