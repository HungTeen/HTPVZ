package com.hungteen.pvz.common.entity.plants;

import com.hungteen.pvz.api.Skill;
import com.hungteen.pvz.api.interfaces.IMaxSunExpander;
import com.hungteen.pvz.api.interfaces.ISunContainer;
import com.hungteen.pvz.common.entity.SimplePlant;
import com.hungteen.pvz.common.entity.ai.goal.AttractEnemyGoal;
import com.hungteen.pvz.common.entity.plants.base.ProducerPlant;
import com.hungteen.pvz.common.register.PVZAttributes;
import com.hungteen.pvz.util.EntityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
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
        return sunState == SunState.FULL ? 400 : sunState == SunState.HALF ? 600 : 800;
    }
    public int getSunAmount(){
        return 50;
    }
    public static AttributeSupplier.Builder createAttributes() {
        return SimplePlant.createAttributes()
                .add(Attributes.FOLLOW_RANGE, 2D);
    }

    @Override
    public int extraMaxSun(BlockPos pos, Entity giveTo) {
        SunState sunState = this.getSunState();
        int extra = sunState == SunState.FULL ? 50 : sunState == SunState.HALF ? 25 : 0;
        int current = 0;
        if (giveTo instanceof Player player && player.getAttribute(PVZAttributes.SUN.get()) != null) {
            current = (int) ((LivingEntity) giveTo).getAttributeValue(PVZAttributes.SUN.get());
        } else if (giveTo instanceof ISunContainer container) {
            current = container.getCapacity();
        }
        return EntityUtil.isTeammate(giveTo, this) ?
                Math.min(extra, Math.max(1000 - current, 0)) : 0;
    }
    @Override
    public boolean requireRefreshExtraMaxSun() {
        return true;
    }
}
