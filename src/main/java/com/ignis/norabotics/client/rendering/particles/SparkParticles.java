package com.ignis.norabotics.client.rendering.particles;

import com.ignis.norabotics.Robotics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import org.jetbrains.annotations.Nullable;

public class SparkParticles extends TextureSheetParticle {
    protected SparkParticles(ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
        super(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
        this.gravity = 0.7f;
        this.lifetime = 5 + Robotics.RANDOM.nextInt(5);
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    protected int getLightColor(float pPartialTick) {
        return 15728880;
    }

    public static class SparkProvider implements ParticleProvider<SimpleParticleType> {

        private SpriteSet sprites;

        public SparkProvider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Nullable
        @Override
        public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
            SparkParticles spark = new SparkParticles(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
            spark.pickSprite(sprites);
            return spark;
        }
    }
}
