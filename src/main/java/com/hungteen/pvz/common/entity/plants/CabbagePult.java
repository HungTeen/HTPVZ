package com.hungteen.pvz.common.entity.plants;

import com.hungteen.pvz.api.Skill;
import com.hungteen.pvz.common.entity.plants.base.SimplePlant;
import com.hungteen.pvz.common.entity.bullet.CabbageBullet;
import com.hungteen.pvz.common.entity.plants.base.ShooterPlant;
import com.hungteen.pvz.common.register.PVZItems;
import com.hungteen.pvz.common.register.PVZSoundEvents;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Set;

public class CabbagePult extends ShooterPlant {

    protected static final double SHOOT_OFFSET = 0.2D;//pea position offset
    public static final String SPEED_SKILL_NAME = "skill.pvz.cabbage_pult.deft_hand";
    public static List<Skill> staticSkillList = List.of(
            new Skill(SPEED_SKILL_NAME, PVZItems.ORIGIN_ESSENCE, 18, 16, 100, 0)
    );

    public CabbagePult(EntityType<? extends Mob> type, Level worldIn) {
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
    protected CabbageBullet createBullet() {
        return new CabbageBullet(this.level, this);
    }

    @Override
    protected SoundEvent getShootSound() {
        return PVZSoundEvents.CABBAGE_SHOOT.get();
    }

    @Override
    public float getAttackDamage() {
        return (float) getAttribute(Attributes.ATTACK_DAMAGE).getValue();
    }
    @Override
    public Set<Integer> shootTimes() {
        return Set.of(17);
    }
    @Override
    public int getShootCD() {
        return this.hasSkill(SPEED_SKILL_NAME) ? 20 : 40;
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
                .add(Attributes.ATTACK_DAMAGE, 8D)
                .add(Attributes.ATTACK_KNOCKBACK, 0D);
    }

}
