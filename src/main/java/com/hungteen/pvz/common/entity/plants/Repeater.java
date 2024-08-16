package com.hungteen.pvz.common.entity.plants;

import com.hungteen.pvz.api.Skill;
import com.hungteen.pvz.common.register.PVZItems;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Set;

public class Repeater extends PeaShooter {
    public static List<Skill> staticSkillList = List.of(
            new Skill("skill.pvz.pea_shooter.punch", PVZItems.VENTUS_ESSENCE, 8, 4, 100, 0),
            new Skill("skill.pvz.repeater.triple_tap", PVZItems.VENTUS_ESSENCE, 8, 4, 50, 0)
);
    public Repeater(EntityType<? extends Mob> type, Level worldIn) {
        super(type, worldIn);
    }
    @Override
    public List<Skill> getStaticSkillList(){
        return staticSkillList;
    }

    public Set<Integer> shootTimes() {
        return hasSkill(this, "skill.pvz.repeater.triple_tap") ? Set.of(10, 11, 12) : Set.of(10, 11);
    }

}
