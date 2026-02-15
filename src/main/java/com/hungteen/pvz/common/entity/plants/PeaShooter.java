package com.hungteen.pvz.common.entity.plants;

import com.hungteen.pvz.api.Skill;
import com.hungteen.pvz.common.entity.plants.base.SimplePlant;
import com.hungteen.pvz.common.entity.bullet.PeaBullet;
import com.hungteen.pvz.common.entity.plants.base.ShooterPlant;
import com.hungteen.pvz.common.register.PVZItems;
import com.hungteen.pvz.util.EntityUtil;
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
    protected static final double SHOOT_OFFSET = -0.3D;//pea spawning position in front of the original pos of pea shooters.
    public static String PUNCH_SKILL_NAME = "skill.pvz.pea_shooter.punch";
    public static String SNIPER_SKILL_NAME = "skill.pvz.pea_shooter.sniper";
    public static String FIRE_SKILL_NAME = "skill.pvz.pea_shooter.fire_shooter";
    public static List<Skill> staticSkillList = List.of(
            new Skill(PUNCH_SKILL_NAME, PVZItems.VENTUS_ESSENCE, 8, 4, 100, 0),
            new Skill(SNIPER_SKILL_NAME, PVZItems.VENTUS_ESSENCE, 4, 12, 150, 940).avoidSkills(PUNCH_SKILL_NAME), //for pvp.
            new Skill(FIRE_SKILL_NAME, PVZItems.IGNIS_ESSENCE, 4, 4, 50, 0).avoidSkills(PUNCH_SKILL_NAME, SNIPER_SKILL_NAME)
    );

    public PeaShooter(EntityType<? extends Mob> type, Level worldIn) {
        super(type, worldIn);
    }
    @Override
    public List<Skill> getBasicStaticSkillList(){
        return staticSkillList;
    }

    @Override
    public void shootBullet() {
        this.performShoot(SHOOT_OFFSET, 0, -0.2F, true, 0);
    }

    @Override
    protected PeaBullet createBullet() {
        PeaBullet bullet = new PeaBullet(this.level, this,
                hasSkill(FIRE_SKILL_NAME) ? PeaBullet.PeaType.Fire : PeaBullet.PeaType.Common);
        if (hasSkill(SNIPER_SKILL_NAME)) {
            bullet.ignoreShield = true;
        }
        return bullet;
    }
    @Override
    public void tick() {
        if (! EntityUtil.attributeHasModifierOfUUID(this, Attributes.ATTACK_KNOCKBACK, ATTRIBUTE_MODIFIER_UUID)
                && ! EntityUtil.attributeHasModifierOfUUID(this, Attributes.ATTACK_DAMAGE, ATTRIBUTE_MODIFIER_UUID)) {
            if (this.hasSkill(PUNCH_SKILL_NAME)) {
                this.getAttribute(Attributes.ATTACK_KNOCKBACK).addTransientModifier(new AttributeModifier(ATTRIBUTE_MODIFIER_UUID, "skill bonus", 0.8, AttributeModifier.Operation.ADDITION));
            } else if (this.hasSkill(SNIPER_SKILL_NAME)) {
                this.getAttribute(Attributes.FOLLOW_RANGE).addTransientModifier(new AttributeModifier(ATTRIBUTE_MODIFIER_UUID, "skill bonus", 64, AttributeModifier.Operation.ADDITION));
                this.getAttribute(Attributes.ATTACK_KNOCKBACK).addTransientModifier(new AttributeModifier(ATTRIBUTE_MODIFIER_UUID, "skill bonus", 1, AttributeModifier.Operation.ADDITION));
                this.getAttribute(Attributes.ATTACK_DAMAGE).addTransientModifier(new AttributeModifier(ATTRIBUTE_MODIFIER_UUID, "skill bonus", 15, AttributeModifier.Operation.MULTIPLY_BASE));
            } else if (this.hasSkill(FIRE_SKILL_NAME)) {
                this.getAttribute(Attributes.ATTACK_DAMAGE).addTransientModifier(new AttributeModifier(ATTRIBUTE_MODIFIER_UUID, "skill bonus", 0.5, AttributeModifier.Operation.MULTIPLY_BASE));
            }
        }
        super.tick();
    }

    public float getAttackDamage() {
        return (float) getAttribute(Attributes.ATTACK_DAMAGE).getValue();
    }

    @Override
    public int getShootCD() {
        return this.hasSkill(this, SNIPER_SKILL_NAME) ? 160 : 40;
    }

    @Override
    public float getBulletSpeed() {
        return (this.hasSkill(this, SNIPER_SKILL_NAME) ? 5F : 1F) * super.getBulletSpeed();
    }
    @Override
    public int getDisappearTicks() {
        return this.hasSkill(SNIPER_SKILL_NAME) ? 2100000000 : super.getDisappearTicks();
    }
    public static AttributeSupplier.Builder createAttributes() {
        return SimplePlant.createAttributes()
                .add(Attributes.FOLLOW_RANGE, 24D)
                .add(Attributes.ATTACK_DAMAGE, 5D)
                .add(Attributes.ATTACK_KNOCKBACK, 0.35D);
    }

}
