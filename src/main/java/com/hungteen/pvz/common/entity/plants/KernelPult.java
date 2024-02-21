package com.hungteen.pvz.common.entity.plants;

import com.hungteen.pvz.api.Skill;
import com.hungteen.pvz.common.entity.SimplePlant;
import com.hungteen.pvz.common.entity.bullet.BaseBullet;
import com.hungteen.pvz.common.entity.bullet.ButterBullet;
import com.hungteen.pvz.common.entity.bullet.CornBullet;
import com.hungteen.pvz.common.entity.plants.base.ShooterPlant;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Set;

public class KernelPult extends ShooterPlant {
    public static final EntityDataAccessor<Integer> CURRENT_BULLET = SynchedEntityData.defineId(KernelPult.class, EntityDataSerializers.INT);
    private static final int BUTTER_CHANCE = 3;
    protected static final double SHOOT_OFFSET = 0.2D;//pea position offset

    public static List<Skill> staticSkillList = List.of(
    );

    public KernelPult(EntityType<? extends Mob> type, Level worldIn) {
        super(type, worldIn);
    }
    @Override
    public List<Skill> getStaticSkillList(){
        return staticSkillList;
    }
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(CURRENT_BULLET, CornTypes.KERNEL.ordinal());
    }
    @Override
    public void shootBullet() {
        this.performShoot(SHOOT_OFFSET, 0, 0, true, 0);
        this.changeBullet();
    }
    @Override
    public double getMaxShootAngleTangent() {
        return 10;
    }

    @Override
    protected BaseBullet createBullet() {
        if(this.getCurrentBullet() == CornTypes.BUTTER) {
            return new ButterBullet(level, this);
        }
        return new CornBullet(level, this);
    }

    @Override
    public float getAttackDamage() {
        return (float) getAttribute(Attributes.ATTACK_DAMAGE).getValue() * (getCurrentBullet() == CornTypes.BUTTER ? 2 : 1);
    }
    @Override
    public Set<Integer> shootTimes() {
        return Set.of(10);
    }
    @Override
    public int getShootCD() {
        return 40;
    }
    @Override
    public int shootAnimLength() {
        return 15;
    }
    @Override
    public float getBulletSpeed() {
        Entity target = this.getTarget();
        if (target != null) {
            double distance = target.distanceTo(this);
            return (float) (Math.max(0.5 * distance / 12, 0.05));
        }
        return 0.5F;
    }

    protected void changeBullet() {
        this.setCurrentBullet(this.getRandom().nextInt(BUTTER_CHANCE) == 0 ? CornTypes.BUTTER : CornTypes.KERNEL);
    }
    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if(compound.contains("current_bullet_type")) {
            this.setCurrentBullet(CornTypes.values()[compound.getInt("current_bullet_type")]);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("current_bullet_type", this.getCurrentBullet().ordinal());
    }

    public void setCurrentBullet(CornTypes type) {
        this.entityData.set(CURRENT_BULLET, type.ordinal());
    }
    public CornTypes getCurrentBullet() {
        return CornTypes.values()[this.entityData.get(CURRENT_BULLET)];
    }

    public static AttributeSupplier.Builder createAttributes() {
        return SimplePlant.createAttributes()
                .add(Attributes.MAX_HEALTH, 8D)
                .add(Attributes.FOLLOW_RANGE, 24D)
                .add(Attributes.ATTACK_DAMAGE, 4D)
                .add(Attributes.ATTACK_KNOCKBACK, 0D);
    }

    @Override
    public EntityDimensions getDimensions(Pose poseIn) {
        return EntityDimensions.scalable(0.7F, 1.0F);
    }
    public enum CornTypes{
        KERNEL,
        BUTTER
    }
}
