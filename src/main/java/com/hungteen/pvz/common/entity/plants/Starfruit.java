package com.hungteen.pvz.common.entity.plants;

import com.hungteen.pvz.api.Skill;
import com.hungteen.pvz.common.entity.SimplePlant;
import com.hungteen.pvz.common.entity.bullet.BaseBullet;
import com.hungteen.pvz.common.entity.bullet.StarfruitBullet;
import com.hungteen.pvz.common.entity.plants.base.ShooterPlant;
import com.hungteen.pvz.common.register.PVZItems;
import com.hungteen.pvz.util.EntityUtil;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class Starfruit extends ShooterPlant {
    public static final String SATELLITE_SKILL = "skill.pvz.starfruit.satellite";
    public static final UUID ATTRIBUTE_MODIFIER_UUID = UUID.fromString("f6ebe0aa-8c4d-737c-cabf-12418e4d00f6");

    public static List<Skill> staticSkillList = List.of(
            new Skill(SATELLITE_SKILL, PVZItems.VENTUS_ESSENCE, 32, 8, 125, 0)
    );

    public Starfruit(EntityType<? extends Mob> type, Level worldIn) {
        super(type, worldIn);
    }
    @Override
    public List<Skill> getBasicStaticSkillList(){
        return staticSkillList;
    }

    @Override
    public void shootBullet() {
        this.performShoot(0, 0, 0, true, 0);
    }

    @Override
    protected StarfruitBullet createBullet() {
        StarfruitBullet bullet = new StarfruitBullet(this.level, this);
        if (this.hasSkill(SATELLITE_SKILL)) {
            bullet.skill = true;
        }
        return bullet;
    }

    @Override
    public Set<Integer> shootTimes() {
        return Set.of(9);
    }
    @Override
    public void tick() {
        if (! EntityUtil.attributeHasModifierOfUUID(this, Attributes.FOLLOW_RANGE, ATTRIBUTE_MODIFIER_UUID)) {
            EntityUtil.addModifierToAttribute(this, Attributes.FOLLOW_RANGE, new AttributeModifier(ATTRIBUTE_MODIFIER_UUID, "skill bonus", -0.5, AttributeModifier.Operation.MULTIPLY_BASE));
        }
        super.tick();
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
        return 0.501F;
    }
    @Override
    public @Nullable Projectile performShoot(double forwardOffset, double rightOffset, double heightOffset, boolean needSound, double randomAngle) {
        Projectile bullet = super.performShoot(forwardOffset, rightOffset, heightOffset, needSound, randomAngle);
        if (bullet != null) {
            Vec3 deltaPos = bullet.getDeltaMovement();
            final double deltaY = bullet.getY() - this.getY();
            double deltaX = bullet.getX() - this.getX();
            double deltaZ = bullet.getZ() - this.getZ();
            float speed = getBulletSpeed();
            double angle = Math.atan2(deltaPos.z, deltaPos.x);
            double distSqr = deltaX * deltaX + deltaZ * deltaZ;
            for (int i = 0; i < 4; i ++) {
                angle += Math.PI / 2.5;
                deltaX = Math.sqrt(distSqr * Math.cos(angle));
                deltaZ = Math.sqrt(distSqr * Math.sin(angle));
                bullet = this.createBullet();
                bullet.setPos(this.getX() + deltaX, this.getY() + deltaY, this.getZ() + deltaZ);
                bullet.shoot(Math.cos(angle), 0.04, Math.sin(angle), speed, (float) randomAngle);
                bullet.setOwner(this);
                if (bullet instanceof BaseBullet bullet1) {
                    bullet1.setAttackDamage(this.getAttackDamage());
                }
                this.level.addFreshEntity(bullet);
            }
        }
        return bullet;
    }

    @Override
    public int getShootCD() {
        return 40;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return SimplePlant.createAttributes()
                .add(Attributes.FOLLOW_RANGE, 16D)
                .add(Attributes.ATTACK_DAMAGE, 5D)
                .add(Attributes.ATTACK_KNOCKBACK, 0D);
    }

}
