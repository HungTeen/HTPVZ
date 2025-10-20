package com.hungteen.pvz.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;

public class DandelionParticle extends TextureSheetParticle {

    public DandelionParticle(ClientLevel clientLevel, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
        super(clientLevel, x, y, z, xSpeed, ySpeed, zSpeed);
        this.gravity = 0F;
        this.friction = 0.9F;
        this.quadSize = 0.03125F;
        this.lifetime = (int) (20F * (double) this.random.nextFloat()) + 40;
    }
    @Override
    public void tick() {
        super.tick();
        this.quadSize = 0.03125F * Math.min(10, this.lifetime - this.age);
        this.yd = Math.cos((float)(lifetime - age) / 20 * Math.PI) / 50;
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
            DandelionParticle particle = new DandelionParticle(clientLevel, x, y, z, xSpeed, ySpeed, zSpeed);
            particle.pickSprite(this.sprites);
            return particle;
        }

    }
}
