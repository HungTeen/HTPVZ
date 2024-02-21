package com.hungteen.pvz.common.entity.plants;

import com.hungteen.pvz.api.Skill;
import com.hungteen.pvz.common.entity.SimplePlant;
import com.hungteen.pvz.common.entity.ai.goal.AxisLookAroundGoal;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;

import java.util.List;

public class SpikeWeed extends SimplePlant {
    public static List<Skill> staticSkillList = List.of(
    );

    public SpikeWeed(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);
    }
    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(2, new AxisLookAroundGoal(this));
    }
    @Override
    public List<Skill> getStaticSkillList(){
        return staticSkillList;
    }
    @Override
    public boolean canBeCollidedWith() {
        return true;
    }


}
