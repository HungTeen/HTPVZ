package com.hungteen.pvz.common.entity.plants;

import com.hungteen.pvz.api.Skill;
import com.hungteen.pvz.common.entity.plants.base.SimplePlant;
import com.hungteen.pvz.common.entity.bullet.DandelionSeedBullet;
import com.hungteen.pvz.common.entity.plants.base.ShooterPlant;
import com.hungteen.pvz.common.register.PVZParticles;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Set;

public class Dandelion extends ShooterPlant {
    public static List<Skill> staticSkillList = List.of(
    );

    public Dandelion(EntityType<? extends Mob> type, Level worldIn) {
        super(type, worldIn);
    }
    @Override
    public List<Skill> getBasicStaticSkillList(){
        return staticSkillList;
    }

    @Override
    public void shootBullet() {
        this.performShoot(0, 0, this.getBbHeight() + 0.4F, true, 0);
        ((ServerLevel) level).sendParticles(PVZParticles.DANDELION.get(), getX(), getY() + 1.5f, getZ(), 5,0, 0, 0, 0);
    }

    @Override
    public Vec3 getShootAngle(Entity target, double forwardOffset, double rightOffset, double heightOffset) {
        return super.getShootAngle(target, forwardOffset, rightOffset, heightOffset).add(0, 3.5, 0);
    }

    @Override
    public double getMaxShootAngleTangent() {
        return 3F;
    }
    @Override
    protected DandelionSeedBullet createBullet() {
        DandelionSeedBullet bullet = new DandelionSeedBullet(this.level, this);
        return bullet;
    }

    public float getAttackDamage() {
        return (float) getAttribute(Attributes.ATTACK_DAMAGE).getValue();
    }

    @Override
    public int getShootCD() {
        return 150;
    }

    @Override
    public float getBulletSpeed() {
        return 0.2F * super.getBulletSpeed();
    }

    public Set<Integer> shootTimes() {
        return Set.of(8);
    }

    public int shootAnimLength() {
        return 30;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return SimplePlant.createAttributes()
                .add(Attributes.FOLLOW_RANGE, 24D)
                .add(Attributes.ATTACK_DAMAGE, 5D)
                .add(Attributes.ATTACK_KNOCKBACK, 0.35D);
    }

}
