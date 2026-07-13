package com.hungteen.pvz.common.entity;

import com.hungteen.pvz.common.block.EntityLightBlock;
import com.hungteen.pvz.common.register.PVZBlocks;
import com.hungteen.pvz.common.register.PVZDamageSource;
import com.hungteen.pvz.common.register.PVZEntities;
import com.hungteen.pvz.common.register.PVZItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

public class FallenStar extends ItemEntity {
    public Vec3 storedSpeed;
    public boolean persistent = false;
    public static Random random = new Random();
    public FallenStar(EntityType<? extends ItemEntity> p_19870_, Level p_19871_) {
        super(p_19870_, p_19871_);
        this.setItem((random.nextInt(35) == 0 ? PVZItems.STARFRUIT.get() : PVZItems.FALLEN_STAR.get()).getDefaultInstance());
        this.storedSpeed = this.getDeltaMovement();
    }

    public FallenStar(Level p_32001_, double p_32002_, double p_32003_, double p_32004_) {
        super(p_32001_, p_32002_, p_32003_, p_32004_, (random.nextInt(35) == 0 ? PVZItems.STARFRUIT.get() : PVZItems.FALLEN_STAR.get()).getDefaultInstance());
        this.storedSpeed = this.getDeltaMovement();
    }

    public static FallenStar spawnAt(Level level, BlockPos pos) {
        FallenStar star = PVZEntities.FALLEN_STAR.get().create(level);
        star.setItem((random.nextInt(35) == 0 ? PVZItems.STARFRUIT.get() : PVZItems.FALLEN_STAR.get()).getDefaultInstance());
        star.moveTo(Vec3.atCenterOf(pos));
        star.setDeltaMovement(random.nextFloat() * 2 - 1, 0, random.nextFloat() * 2 - 1);
        level.addFreshEntity(star);
        return star;
    }
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        tag.putBoolean("Persistent", this.persistent);
        super.addAdditionalSaveData(tag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Persistent")) {
            this.persistent = tag.getBoolean("Persistent");
        }
    }
    @Override
    public void baseTick() {
        super.baseTick();
        //disappear when day
        if (! this.persistent && ! level.isClientSide && level.isDay() && random.nextInt(100) == 0) {
            this.discard();
        }
        //moving && bouncing.
        if (this.getDeltaMovement().y < - 1) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(1, 0, 1).add(0, - 1, 0));
        }
        if (! level.isClientSide && this.verticalCollisionBelow) {
            double speedSqr = this.storedSpeed.lengthSqr();
            if (speedSqr > 0.5) {
                level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(0.5)).forEach(entity -> entity.hurt(PVZDamageSource.FALLEN_STAR, (float) speedSqr * 3));
            }
            this.setDeltaMovement(this.getDeltaMovement().x, (this.storedSpeed.y < -1 ? - this.storedSpeed.y * 0.5 : this.getDeltaMovement().y), this.getDeltaMovement().z);
        }
        this.storedSpeed = this.getDeltaMovement();
        //particle
        if (level.isClientSide && random.nextBoolean()) {
            long light = level.getDayTime();
            if ((light - 1000) % 24000 > 10000) {
                if (random.nextBoolean()) {
                    level.addParticle(ParticleTypes.FIREWORK, this.position().x + random.nextFloat() - 0.5, this.position().y + random.nextFloat(), this.position().z + random.nextFloat() - 0.5, 0, 0, 0);
                }
            } else {
                for (int i = 0; i < 5; i ++) {
                    level.addParticle(ParticleTypes.FIREWORK, this.position().x + random.nextFloat() - 0.5, this.position().y + random.nextFloat(), this.position().z + random.nextFloat() - 0.5,
                            random.nextFloat() - 0.5, random.nextFloat() * 0.5, random.nextFloat() - 0.5);
                }
            }
        }
        //light.
        BlockPos pos = this.blockPosition().above();
        if (level.isClientSide() || this.getY() - this.level.getHeight(Heightmap.Types.MOTION_BLOCKING, (int) this.getX(), (int) this.getZ()) > 5) {
            return ;
        } else if (level.getBlockState(pos).isAir()) {
            level.setBlock(pos, PVZBlocks.ENTITY_LIGHT.get().defaultBlockState()
                    .setValue(EntityLightBlock.LEVEL, random.nextInt(5) == 0 ? 10 : 12), 2);
        } else if (level.getBlockState(pos).is(Blocks.WATER)) {
            level.setBlock(pos, PVZBlocks.ENTITY_LIGHT.get().defaultBlockState()
                    .setValue(EntityLightBlock.WATERLOGGED, true).setValue(EntityLightBlock.LEVEL, 12), 2);
        }
        if (level.getBlockState(pos).is(PVZBlocks.ENTITY_LIGHT.get())) {
            level.setBlock(pos, level.getBlockState(pos)
                    .setValue(EntityLightBlock.HAS_SOURCE, true), 2);
        }
    }
}
