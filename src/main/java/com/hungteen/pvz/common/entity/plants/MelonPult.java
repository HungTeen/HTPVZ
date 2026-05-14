package com.hungteen.pvz.common.entity.plants;

import com.hungteen.pvz.api.Skill;
import com.hungteen.pvz.common.entity.plants.base.SimplePlant;
import com.hungteen.pvz.common.entity.bullet.MelonBullet;
import com.hungteen.pvz.common.entity.plants.base.ShooterPlant;
import com.hungteen.pvz.common.register.PVZItems;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Set;

public class MelonPult extends ShooterPlant {

    protected static final double SHOOT_OFFSET = 0.2D;//pea position offset
    public static final String POTION_SKILL_NAME = "skill.pvz.melon_pult.glistering_melon";
    public static final String GRAVITY_SKILL_NAME = "skill.pvz.melon_pult.gravitational_potential";
    public static List<Skill> staticSkillList = List.of(
            new Skill(POTION_SKILL_NAME, PVZItems.AQUA_ESSENCE, 8, 4, 100, 0),
            new Skill(GRAVITY_SKILL_NAME, PVZItems.TERRA_ESSENCE, 8, 8, 50, 0).avoidSkills(POTION_SKILL_NAME)
    );

    public MelonPult(EntityType<? extends Mob> type, Level worldIn) {
        super(type, worldIn);
    }
    @Override
    public List<Skill> getBasicStaticSkillList(){
        return staticSkillList;
    }

    @Override
    public void shootBullet() {
        this.performShoot(SHOOT_OFFSET, 0, this.getBbHeight(), true, 0);
    }
    @Override
    public double getMaxShootAngleTangent() {
        return Double.POSITIVE_INFINITY;
    }

    @Override
    protected MelonBullet createBullet() {
        MelonBullet bullet = new MelonBullet(this.level, this, MelonBullet.MelonType.Common);
        if (this.hasSkill(POTION_SKILL_NAME)) {
            bullet.setMelonSkill(MelonBullet.MelonSkill.POTION);
        } else if (this.hasSkill(GRAVITY_SKILL_NAME)) {
            bullet.setMelonSkill(MelonBullet.MelonSkill.GRAVITY);
        }
        return bullet;
    }

    @Override
    public float getAttackDamage() {
        return (float) getAttribute(Attributes.ATTACK_DAMAGE).getValue();
    }
    @Override
    public Set<Integer> shootTimes() {
        return Set.of(15);
    }
    @Override
    public int getShootCD() {
        return 50;
    }
    @Override
    public int shootAnimLength() {
        return 20;
    }
    @Override
    public float getBulletSpeed() {
        Entity target = this.getTarget();
        if (target != null) {
            double distance = target.distanceTo(this);
            return (float) (Math.max(0.5 * distance / 12, 0.1));
        }
        return 0.5F;
    }


    public static AttributeSupplier.Builder createAttributes() {
        return SimplePlant.createAttributes()
                .add(Attributes.FOLLOW_RANGE, 24D)
                .add(Attributes.ATTACK_DAMAGE, 16D)
                .add(Attributes.ATTACK_KNOCKBACK, 0D);
    }

}
