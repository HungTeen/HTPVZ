package com.hungteen.pvz.common.entity.plants;

import com.hungteen.pvz.api.Skill;
import com.hungteen.pvz.common.register.PVZItems;
import com.hungteen.pvz.util.EntityUtil;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Set;

public class Repeater extends PeaShooter {
    public static final String TRIPLE_SKILL_NAME = "skill.pvz.repeater.triple_tap";
    public static List<Skill> staticSkillList = List.of(
            new Skill(PeaShooter.PUNCH_SKILL_NAME, PVZItems.VENTUS_ESSENCE, 8, 6, 100, 0),
            new Skill(TRIPLE_SKILL_NAME, PVZItems.VENTUS_ESSENCE, 8, 4, 50, 0)
);
    public Repeater(EntityType<? extends Mob> type, Level worldIn) {
        super(type, worldIn);
    }
    @Override
    public List<Skill> getStaticSkillList(){
        return staticSkillList;
    }

    public Set<Integer> shootTimes() {
        return hasSkill(this, TRIPLE_SKILL_NAME) ? Set.of(10, 11, 12) : Set.of(10, 11);
    }

    @Override
    public void baseTick() {
        if (! EntityUtil.attributeHasModifierOfUUID(this, Attributes.ATTACK_KNOCKBACK, ATTRIBUTE_MODIFIER_UUID)) {
            this.getAttribute(Attributes.ATTACK_KNOCKBACK).addTransientModifier(new AttributeModifier(ATTRIBUTE_MODIFIER_UUID, "skill bonus", 0.4, AttributeModifier.Operation.ADDITION));
        }
        super.baseTick();
    }
}
