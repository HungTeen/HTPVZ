package com.hungteen.pvz.common.entity.plants;

import com.hungteen.pvz.api.Skill;
import com.hungteen.pvz.common.entity.SimplePlant;
import com.hungteen.pvz.common.entity.bullet.BaseBullet;
import com.hungteen.pvz.common.entity.bullet.PeaBullet;
import com.hungteen.pvz.common.entity.bullet.StarfruitBullet;
import com.hungteen.pvz.common.entity.plants.base.ShooterPlant;
import com.hungteen.pvz.common.register.PVZItems;
import com.hungteen.pvz.util.EntityUtil;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class Starfruit extends ShooterPlant {
    public static List<Skill> staticSkillList = List.of(
    );

    public Starfruit(EntityType<? extends Mob> type, Level worldIn) {
        super(type, worldIn);
    }
    @Override
    public List<Skill> getStaticSkillList(){
        return staticSkillList;
    }

    @Override
    public void shootBullet() {
        this.performShoot(0, 0, 0, true, 0);
    }

    @Override
    protected StarfruitBullet createBullet() {
        return new StarfruitBullet(this.level, this);
    }
    @Override
    public Set<Integer> shootTimes() {
        return Set.of(9);
    }
    @Override
    public float getAttackDamage() {
        return (float) this.getAttribute(Attributes.ATTACK_DAMAGE).getValue();
    }
    @Override
    public double getMaxShootAngleTangent() {
        return 0.05;
    }
    @Override
    public float getBulletSpeed() {
        return 0.5F;
    }
    @Override
    public void performShoot(double forwardOffset, double rightOffset, double heightOffset, boolean needSound, double randomAngle) {
        LivingEntity target = this.getTarget();
        //create bullet
        final Vec3 vec = getShootAngle(target);
        final double deltaY = this.getDimensions(getPose()).height * 0.7F + heightOffset;
        final double deltaX = forwardOffset * vec.x - rightOffset * vec.z;
        final double deltaZ = forwardOffset * vec.z + rightOffset * vec.x;
        BaseBullet bullet = this.createBullet();
        bullet.setPos(this.getX() + deltaX, this.getY() + deltaY, this.getZ() + deltaZ);
        //predict
        float speed = this.getBulletSpeed();
        Vec3 deltaPos;
        if (target != null) {
            Vec3 targetSpeed;
            if (storedEnemyPos != null) {
                targetSpeed = target.position().subtract(storedEnemyPos)
                        .multiply(1 / (float) aimTime, 1 / (float) aimTime, 1 / (float) aimTime);
            } else {
                targetSpeed = target.getDeltaMovement();
            }
            int time = Math.round(distanceTo(target) / speed);
            deltaPos = new Vec3(target.getX() + targetSpeed.x * time - bullet.getX(),
                    target.getY() + targetSpeed.y * time + target.getBbHeight() / 2 - bullet.getY(),//angle limit move to targeting goals.
                    target.getZ() + targetSpeed.z * time - bullet.getZ());
            for (int tmp = 0; tmp < 3; tmp ++) {
                //recurse to increase accuracy.
                time = (int) Math.round(Math.sqrt(deltaPos.x * deltaPos.x + deltaPos.y * deltaPos.y + deltaPos.z * deltaPos.z) / speed);
                deltaPos = new Vec3(target.getX() + targetSpeed.x * time - bullet.getX(),
                        target.getY() + targetSpeed.y * time + target.getBbHeight() / 2 - bullet.getY(),
                        target.getZ() + targetSpeed.z * time - bullet.getZ());
            }
        } else {
            deltaPos = vec;
        }
        deltaPos = new Vec3 (deltaPos.x, 0, deltaPos.z);
        //shoot
        if (needSound) {
            EntityUtil.playSound(this, this.getShootSound());
        }
        bullet.shoot(deltaPos.x, 0, deltaPos.z, speed, (float) randomAngle);
        bullet.setOwner(this);
        bullet.setAttackDamage(this.getAttackDamage());
        this.level.addFreshEntity(bullet);
        double angle = Math.atan2(deltaPos.z, deltaPos.x);
        for (int i = 0; i < 4; i ++) {
            angle += Math.PI / 2.5;
            bullet = this.createBullet();
            bullet.setPos(this.getX() + deltaX, this.getY() + deltaY, this.getZ() + deltaZ);
            bullet.shoot(Math.cos(angle), 0, Math.sin(angle), speed, (float) randomAngle);
            bullet.setOwner(this);
            bullet.setAttackDamage(this.getAttackDamage());
            this.level.addFreshEntity(bullet);
        }
    }

    @Override
    public int getShootCD() {
        return 40;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return SimplePlant.createAttributes()
                .add(Attributes.MAX_HEALTH, 8D)
                .add(Attributes.FOLLOW_RANGE, 16D)
                .add(Attributes.ATTACK_DAMAGE, 5D)
                .add(Attributes.ATTACK_KNOCKBACK, 0D);
    }

}
