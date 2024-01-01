package com.hungteen.pvz.common.entity.plants;

import com.hungteen.pvz.common.entity.PVZPlant;
import com.hungteen.pvz.common.entity.bullet.AbstractBulletEntity;
import com.hungteen.pvz.common.entity.bullet.PeaBullet;
import com.hungteen.pvz.common.entity.plants.base.PlantShooterEntity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

public class PeaShooter extends PlantShooterEntity {

    protected static final double SHOOT_OFFSET = 0.2D;//pea position offset

    public PeaShooter(EntityType<? extends Mob> type, Level worldIn) {
        super(type, worldIn);
    }

    @Override
    public void shootBullet() {
        this.performShoot(SHOOT_OFFSET, 0, 0, true, FORWARD_SHOOT_ANGLE);
    }

    @Override
    protected AbstractBulletEntity createBullet() {
        return new PeaBullet(this.level, this);
    }

    public float getAttackDamage() {
        return 1;
    }

    @Override
    public int getShootCD() {
        return 20;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PVZPlant.createAttributes()
                .add(Attributes.MAX_HEALTH, 10D)
                .add(Attributes.FOLLOW_RANGE, 2D)
                .add(Attributes.ATTACK_DAMAGE, 2D)
                .add(Attributes.ATTACK_KNOCKBACK, 0.1D);
    }

    @Override
    public EntityDimensions getDimensions(Pose poseIn) {
        return EntityDimensions.scalable(0.7F, 1.3F);
    }

}
