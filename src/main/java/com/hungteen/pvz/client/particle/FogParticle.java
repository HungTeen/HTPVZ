package com.hungteen.pvz.client.particle;

import com.hungteen.pvz.common.entity.plants.Plantern;
import com.hungteen.pvz.common.world.PVZFog;
import com.hungteen.pvz.common.network.ClientProxy;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class FogParticle extends TextureSheetParticle {
    public SpriteSet sprites;
    private final boolean isFog;
    protected FogParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, boolean isFog) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        this.hasPhysics = false;
        this.lifetime = this.random.nextInt(15) + 200;
        this.quadSize = 2;
        this.setSize(2, 2);
        this.alpha = (float) ((level.random.nextFloat() - 0.5) * 0.05 + 0.3);
        this.isFog = isFog;
        this.xd = (level.random.nextFloat() - 0.5) * (isFog ? 0.0005 : 0.025) - (isFog ? 0.025 : 0.05);
        this.yd = (level.random.nextFloat() - 0.5) * 0.005;
        this.zd = (level.random.nextFloat() - 0.5) * 0.005;
        this.friction = 1;
    }

    @Override
    public void tick(){
        super.tick();
        if (!this.removed) {
            if (lifetime > age + 24 && random.nextInt(5) == 0) {
                if (isFog) {
                    if (PVZFog.getFogStrengthAt(level, new Vec3(x, y, z)) < 0.5) {
                        lifetime = Math.min(age * 4, age + 24);
                    }
                } else {
                    Player player = ClientProxy.getPlayer();
                    if ((x - player.getX()) * (x - player.getX()) + (y - player.getY()) * (y - player.getY()) + (z - player.getZ()) * (z - player.getZ()) < 25) {
                        lifetime = Math.min(age * 4, age + 24);
                    }
                }
            }
            if (lifetime > age + 24 && random.nextInt(10) == 0) {
                if (! level.getEntities(EntityTypeTest.forClass(Plantern.class),
                        new AABB(x - 4, y - 4, z - 4, x + 4, y + 4, z + 4),
                        (plantern) -> true).isEmpty()) {
                    lifetime = Math.min(age * 4, age + 24);
                }
            }
            int draw = 6;
            if (age < 24) {
                draw = Math.min((int) Math.floor((float) age / 4), draw);
            } else if (age + 24 > lifetime) {
                draw = Math.min((int) Math.floor((float) (lifetime - age) / 4), draw);
            }
            draw = Mth.clamp(draw, 0, 6);
            this.setSprite(sprites.get(draw, 6));
        }
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
            FogParticle particle = new FogParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, PVZFog.getFogStrengthAt(level, new Vec3(x, y ,z)) > 0);
            particle.setSprite(sprite.get(0, 6));
            particle.sprites = sprite;
            return particle;
        }
    }
}
