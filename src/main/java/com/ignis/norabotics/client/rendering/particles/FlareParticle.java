package com.ignis.norabotics.client.rendering.particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import org.jetbrains.annotations.Nullable;

public class FlareParticle extends TextureSheetParticle {
    protected FlareParticle(ClientLevel pLevel, double pX, double pY, double pZ) {
        super(pLevel, pX, pY, pZ);
        lifetime = 1;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    protected int getLightColor(float pPartialTick) {
        return 15728880;
    }

    public static class FlareProvider implements ParticleProvider<SimpleParticleType> {

        private SpriteSet sprites;

        public FlareProvider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Nullable
        @Override
        public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
            FlareParticle flare = new FlareParticle(pLevel, pX, pY, pZ);
            flare.pickSprite(sprites);
            return flare;
        }
    }
}
