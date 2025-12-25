package com.hungteen.pvz.common.entity;

import com.hungteen.pvz.common.entity.zombies.BungeeZombie;
import com.hungteen.pvz.util.EntityUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class Hook extends Projectile {
    public Hook(EntityType<? extends Projectile> p_37248_, Level p_37249_) {
        super(p_37248_, p_37249_);
        this.setNoGravity(true);
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        HitResult.Type type = result.getType();
        if (type != HitResult.Type.MISS) {
            this.gameEvent(GameEvent.PROJECTILE_LAND, this.getOwner());
        }
    }
    @Override
    protected boolean canHitEntity(Entity entity) {
        return false;
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        Entity owner = this.getOwner();
        if (EntityUtil.isEntityValid(owner)) {
            if (owner instanceof BungeeZombie bungeeZombie) {
                bungeeZombie.setHangingPosition(result.getBlockPos());
                bungeeZombie.ropeLengthSqr = result.getBlockPos().distSqr(bungeeZombie.blockPosition());
            }
        }
        this.discard();
    }

    @Override
    public void tick() {
        super.tick();
        HitResult hitresult = ProjectileUtil.getHitResult(this, this::canHitEntity);
        if (hitresult.getType() != HitResult.Type.MISS && !net.minecraftforge.event.ForgeEventFactory.onProjectileImpact(this, hitresult)) {
            this.onHit(hitresult);
        }
        Vec3 vec3 = this.getDeltaMovement();
        double dx = vec3.x;
        double dy = vec3.y;
        double dz = vec3.z;
        this.updateRotation();
        this.setDeltaMovement(dx, dy, dz);
        this.setPos(this.getX() + dx, this.getY() + dy, this.getZ() + dz);
        if (this.tickCount > 200) {
            this.discard();
        }
    }
}
