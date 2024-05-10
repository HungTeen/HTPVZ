package com.hungteen.pvz.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;

public class MashedPotatoParticle extends TextureSheetParticle {

    public MashedPotatoParticle(ClientLevel clientLevel, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
        super(clientLevel, x, y, z, xSpeed, ySpeed, zSpeed);
        this.gravity = 0.3F;
        this.friction = 0.9F;
        this.quadSize = 0.5F;
        this.xd = xSpeed + (Math.random() * 2.0D - 1.0D) * (double)0.05F;
        this.yd = ySpeed + (Math.random() * 2.0D - 1.0D) * (double)0.05F + 0.05F;
        this.zd = zSpeed + (Math.random() * 2.0D - 1.0D) * (double)0.05F;
        this.lifetime = (int)(16.0D / ((double)this.random.nextFloat() * 0.8D + 0.2D)) + 5;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {

        private final SpriteSet sprites;

        public Provider(SpriteSet spriteSet) {
            this.sprites = spriteSet;
        }

        public Particle createParticle(SimpleParticleType particleType, ClientLevel clientLevel, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            MashedPotatoParticle particle = new MashedPotatoParticle(clientLevel, x, y, z, xSpeed, ySpeed, zSpeed);
            particle.pickSprite(this.sprites);
            return particle;
        }

    }
}
