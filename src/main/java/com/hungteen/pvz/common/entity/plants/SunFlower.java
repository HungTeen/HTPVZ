package com.hungteen.pvz.common.entity.plants;

import com.hungteen.pvz.api.Skill;
import com.hungteen.pvz.api.interfaces.IMaxSunExpander;
import com.hungteen.pvz.api.interfaces.ISunContainer;
import com.hungteen.pvz.common.entity.ai.goal.AttractEnemyGoal;
import com.hungteen.pvz.common.entity.plants.base.ProducerPlant;
import com.hungteen.pvz.common.entity.plants.base.SimplePlant;
import com.hungteen.pvz.common.tags.PVZBiomeTags;
import com.hungteen.pvz.util.EntityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

import java.util.List;

public class SunFlower extends ProducerPlant implements IMaxSunExpander {
    public static List<Skill> staticSkillList = List.of(
    );
    public SunFlower(EntityType<? extends Mob> type, Level worldIn) {
        super(type, worldIn);
    }

    @Override
    protected void genSomething() {
        this.genSun(this.getSunAmount(),1);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new AttractEnemyGoal(this));
    }

    @Override
    public int getGenCD() {
        SunState sunState = this.getSunState();
        return sunState == SunState.FULL ? 240 : sunState == SunState.HALF ? 360 : 480;
    }
    public int getSunAmount(){
        return 50;
    }
    public static AttributeSupplier.Builder createAttributes() {
        return SimplePlant.createAttributes()
                .add(Attributes.FOLLOW_RANGE, 2D);
    }

    @Override
    public int extraMaxSun(BlockPos pos, ISunContainer giveTo) {
        if (giveTo instanceof Entity e && ! EntityUtil.isTeammate(e, this)) return 0;
        if (level.getBiome(blockPosition()).is(PVZBiomeTags.UNABLE_SUN_PRODUCTION)) return 0;
        SunState sunState = this.getSunState();
        return sunState == SunState.FULL ? 50 : sunState == SunState.HALF ? 25 : 0;
    }

    @Override
    public int extraMaxSunLimit(BlockPos pos, ISunContainer giveTo) {
        if (giveTo instanceof Entity e && ! EntityUtil.isTeammate(e, this)) return 0;
        if (level.getBiome(blockPosition()).is(PVZBiomeTags.UNABLE_SUN_PRODUCTION)) return 0;
        return 1000;
    }

    @Override
    public boolean requireRefreshExtraMaxSun() {
        return true;
    }
}
