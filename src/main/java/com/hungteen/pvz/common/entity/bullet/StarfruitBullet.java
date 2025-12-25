package com.hungteen.pvz.common.entity.bullet;

import com.hungteen.pvz.common.register.PVZEntities;
import com.hungteen.pvz.common.register.PVZItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class StarfruitBullet extends BaseBullet {

    public boolean skill = false;
    public StarfruitBullet(Level worldIn, LivingEntity shooter) {
        super(PVZEntities.STARFRUIT_BULLET.get(), worldIn, shooter);
        this.setOwner(shooter);
        this.shootDamageName = "starfruit";
    }

    public StarfruitBullet(EntityType<? extends BaseBullet> bulletEntityType, Level level) {
        super(bulletEntityType, level);
        this.shootDamageName = "starfruit";
    }
    protected void splashParticle() {
        Vec3 movement = getDeltaMovement();
        for (int i = 0; i < 5; i ++) {
            level.addParticle(new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(PVZItems.STARFRUIT.get())),
                    getX(), getY(), getZ(),
                    - movement.x * 0.25 + random.nextFloat() * 0.25 - 0.12,
                    - movement.y * 0.25 + random.nextFloat() * 0.25,
                    - movement.z * 0.25 + random.nextFloat() * 0.25 - 0.12);
        }
    }
    @Override
    protected int getMaxLiveTick() {
        return 60;
    }
    @Override
    protected void onHitBlock(BlockHitResult result) {
        BlockPos pos = result.getBlockPos();
        Vec3 vec3 = this.position().subtract(Vec3.atCenterOf(pos));
        if (Math.abs(vec3.z) > Math.abs(vec3.x)) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(1, 1, -1));
        } else {
            this.setDeltaMovement(this.getDeltaMovement().multiply(-1, 1, 1));
        }
        BlockState blockstate = this.level.getBlockState(result.getBlockPos());
        blockstate.onProjectileHit(this.level, blockstate, result, this);
    }
    public void tick() {
        super.tick();
        this.yRot += 5;
        if (skill) {
            Vec3 addV = new Vec3(-this.getDeltaMovement().z, 0, this.getDeltaMovement().x).scale(Math.min(1F, tickCount == 0 ? 5 : 5f / this.tickCount));
            Vec3 mov = this.getDeltaMovement().multiply(1, 0, 1);
            this.setDeltaMovement(mov.add(addV).normalize().scale(mov.length()).add(0, this.getDeltaMovement().y, 0));
        }
        this.size = Math.min(((float) this.getMaxLiveTick() - this.tickCount) / 5, 1);
        if (level.isClientSide && this.random.nextBoolean()) {
            level.addParticle(new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(PVZItems.STARFRUIT.get())),
                    getX() - 0.3 + random.nextFloat() * 0.6, getY(), getZ() - 0.3 + random.nextFloat() * 0.6, 0, 0, 0);
        }
    }
    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("skill", this.skill);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.skill = tag.getBoolean("skill");
    }
}
