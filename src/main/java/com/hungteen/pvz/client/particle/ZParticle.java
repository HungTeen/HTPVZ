package com.hungteen.pvz.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import org.jetbrains.annotations.Nullable;

public class ZParticle extends TextureSheetParticle {
    protected ZParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        this.quadSize = 0F;
        this.lifetime = 100;
        this.hasPhysics = false;
        this.gravity = 0f;
        this.xd = Math.sin(5 * Math.PI) / 20;
        this.yd = 0;
        this.zd = Math.cos(5 * Math.PI) / 20;
    }

    @Override
    public void tick() {
        super.tick();
        this.quadSize = 0.015F * Math.min(this.age, Math.min(10, this.lifetime - this.age));
        this.xd = Math.sin((float)(lifetime - age) / 20 * Math.PI) / 25;
        this.zd = Math.cos((float)(lifetime - age) / 20 * Math.PI) / 25;
        this.yd = 0.03;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;
        public Provider(SpriteSet sprite) {
            this.sprite = sprite;
        }

        @Nullable
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            ZParticle particle = new ZParticle(level, x, y, z, xSpeed, ySpeed, zSpeed);
            particle.pickSprite(this.sprite);
            return particle;
        }
    }
}
