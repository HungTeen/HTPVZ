package com.hungteen.pvz.common.entity.plants;

import com.hungteen.pvz.api.Skill;
import com.hungteen.pvz.common.entity.SimplePlant;
import com.hungteen.pvz.common.entity.bullet.PeaBullet;
import com.hungteen.pvz.common.entity.plants.base.ShooterPlant;
import com.hungteen.pvz.common.register.PVZItems;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.UUID;

public class PeaShooter extends ShooterPlant {
    protected static final UUID ATTRIBUTE_MODIFIER_UUID = UUID.fromString("fa192025-b0e7-65ef-9bc3-546a895a193d");
    protected boolean skillBoosted = false;
    protected static final double SHOOT_OFFSET = 0.2D;//pea spawning position in front of the original pos of pea shooters.
    public static List<Skill> staticSkillList = List.of(
            new Skill("skill.pvz.pea_shooter.punch", PVZItems.VENTUS_ESSENCE, 8, 4, 100, 0),
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
        PeaBullet bullet = new PeaBullet(this.level, this,
                hasSkill("skill.pvz.pea_shooter.fire_shooter") ? PeaBullet.PeaType.Fire : PeaBullet.PeaType.Common);
        if (hasSkill("skill.pvz.pea_shooter.sniper")) {
            bullet.ignoreShield = true;
        }
        return bullet;
    }
    @Override
    public void baseTick() {
        if (! skillBoosted) {
            skillBoosted = true;
            if (this.hasSkill("skill.pvz.pea_shooter.punch")) {
                this.getAttribute(Attributes.ATTACK_KNOCKBACK).addTransientModifier(new AttributeModifier(ATTRIBUTE_MODIFIER_UUID, "skill bonus", 1, AttributeModifier.Operation.ADDITION));
            } else if (this.hasSkill("skill.pvz.pea_shooter.sniper")) {
                this.getAttribute(Attributes.FOLLOW_RANGE).addTransientModifier(new AttributeModifier(ATTRIBUTE_MODIFIER_UUID, "skill bonus", 24, AttributeModifier.Operation.ADDITION));
            }
        }
        super.baseTick();
    }

    public float getAttackDamage() {
        return (float) (getAttribute(Attributes.ATTACK_DAMAGE).getValue() *
                        (this.hasSkill(this, "skill.pvz.pea_shooter.sniper") ? 6 :
                                this.hasSkill(this, "skill.pvz.pea_shooter.fire_shooter") ? 1.5 : 1));
    }

    @Override
    public int getShootCD() {
        return this.hasSkill(this, "skill.pvz.pea_shooter.sniper") ? 160 : 40;
    }

    @Override
    public float getBulletSpeed() {
        return (this.hasSkill(this, "skill.pvz.pea_shooter.sniper") ? 3F : 1F) * super.getBulletSpeed();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return SimplePlant.createAttributes()
                .add(Attributes.FOLLOW_RANGE, 24D)
                .add(Attributes.ATTACK_DAMAGE, 5D)
                .add(Attributes.ATTACK_KNOCKBACK, 0D);
    }

}
