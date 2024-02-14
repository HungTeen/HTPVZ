package com.hungteen.pvz.common.entity.plants;

import com.hungteen.pvz.api.Skill;
import com.hungteen.pvz.common.entity.SimplePlant;
import com.hungteen.pvz.common.entity.bullet.PeaBullet;
import com.hungteen.pvz.common.entity.plants.base.ShooterPlant;
import com.hungteen.pvz.common.register.PVZItems;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

import java.util.List;

public class PeaShooter extends ShooterPlant {

    protected static final double SHOOT_OFFSET = 0.2D;//pea position offset
    public static List<Skill> staticSkillList = List.of(
            new Skill("skill.pvz.pea_shooter.punch", PVZItems.VENTUS_ESSENCE, 8, 4, 150, 0),
            new Skill("skill.pvz.pea_shooter.sniper", PVZItems.VENTUS_ESSENCE, 4, 12, 500, 800).avoidSkills(0), //for pvp.
            new Skill("skill.pvz.pea_shooter.fire_shooter", PVZItems.IGNIS_ESSENCE, 4, 4, 50, 0).avoidSkills(0, 1)
    );

    public PeaShooter(EntityType<? extends Mob> type, Level worldIn) {
        super(type, worldIn);
    }
    @Override
    public List<Skill> getStaticSkillList(){
        return staticSkillList;
    }

    @Override
    public void shootBullet() {
        this.performShoot(SHOOT_OFFSET, 0, 0, true, 0);
    }

    @Override
    protected PeaBullet createBullet() {
        PeaBullet bullet = new PeaBullet(this.level, this, PeaBullet.PeaType.Common);
        if (hasSkill(this, "skill.pvz.pea_shooter.punch")) {
            bullet.setKnockBackStrength(1F);
        } else if (hasSkill(this, "skill.pvz.pea_shooter.fire_shooter")) {
            bullet.setPeaType(PeaBullet.PeaType.Fire);
        }
        return bullet;
    }

    public float getAttackDamage() {
        return (float) (getAttribute(Attributes.ATTACK_DAMAGE).getValue() *
                        (this.hasSkill(this, "skill.pvz.pea_shooter.sniper") ? 6 :
                                this.hasSkill(this, "skill.pvz.pea_shooter.fire_shooter") ? 1.5 : 1));
    }

    @Override
    public int getShootCD() {
        return this.hasSkill(this, "skill.pvz.pea_shooter.sniper") ? 200 : 40;
    }

    @Override
    public float getBulletSpeed() {
        return (this.hasSkill(this, "skill.pvz.pea_shooter.sniper") ? 3F : 1F) * super.getBulletSpeed();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return SimplePlant.createAttributes()
                .add(Attributes.MAX_HEALTH, 8D)
                .add(Attributes.FOLLOW_RANGE, 24D)
                .add(Attributes.ATTACK_DAMAGE, 5D)
                .add(Attributes.ATTACK_KNOCKBACK, 0D);
    }

    @Override
    public EntityDimensions getDimensions(Pose poseIn) {
        return EntityDimensions.scalable(0.7F, 1.3F);
    }

}
