package com.hungteen.pvz.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import org.jetbrains.annotations.Nullable;

public class SunParticle extends TextureSheetParticle {
    private SpriteSet sprites;
    protected SunParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        this.quadSize = 0.25f;
        this.lifetime = this.random.nextInt(5) + 15;
        this.hasPhysics = true;
        this.gravity = 0.5f;
        this.xd = (level.random.nextFloat() - 0.5) * 0.3;
        this.yd = level.random.nextFloat() * 0.3;
        this.zd = (level.random.nextFloat() - 0.5) * 0.3;
    }

    @Override
    public void tick(){
        super.tick();
        setSpriteFromAge(this.sprites);
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
            SunParticle particle = new SunParticle(level, x, y, z, xSpeed, ySpeed, zSpeed);
            particle.pickSprite(this.sprite);
            particle.sprites = sprite;
            return particle;
        }
    }
}
