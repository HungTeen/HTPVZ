package com.hungteen.pvz.common.entity.plants;

import com.hungteen.pvz.api.Skill;
import com.hungteen.pvz.common.entity.SimplePlant;
import com.hungteen.pvz.common.entity.bullet.CabbageBullet;
import com.hungteen.pvz.common.entity.plants.base.ShooterPlant;
import com.hungteen.pvz.common.register.PVZItems;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Set;

public class CabbagePult extends ShooterPlant {

    protected static final double SHOOT_OFFSET = 0.2D;//pea position offset
    public static List<Skill> staticSkillList = List.of(
            new Skill("skill.pvz.cabbage_pult.deft_hand", PVZItems.ORIGIN_ESSENCE, 8, 8, 100, 0)
    );

    public CabbagePult(EntityType<? extends Mob> type, Level worldIn) {
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
    public double getMaxShootAngleTangent() {
        return Double.POSITIVE_INFINITY;
    }

    @Override
    protected CabbageBullet createBullet() {
        return new CabbageBullet(this.level, this);
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
        return this.hasSkill("skill.pvz.cabbage_pult.deft_hand") ? 20 : 40;
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
                .add(Attributes.ATTACK_DAMAGE, 5D)
                .add(Attributes.ATTACK_KNOCKBACK, 0D);
    }

}
