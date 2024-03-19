package com.hungteen.pvz.common.entity.plants;

import com.hungteen.pvz.api.interfaces.IMaxSunExpander;
import com.hungteen.pvz.common.entity.SimplePlant;
import com.hungteen.pvz.common.entity.ai.goal.AttractEnemyGoal;
import com.hungteen.pvz.common.entity.plants.base.ProducerPlant;
import com.hungteen.pvz.common.register.PVZMobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;

public class SunFlower extends ProducerPlant implements IMaxSunExpander {
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
        final int time = 240;
        int light = level.getBrightness(LightLayer.SKY, this.blockPosition()) - level.getSkyDarken();
        return (light > 12 || this.hasEffect(PVZMobEffects.BRIGHTNESS.get())) ? time : (light > 9 ? 2 * time: 3 * time);
    }
    public int getSunAmount(){
        return 50;
    }
    public static AttributeSupplier.Builder createAttributes() {
        return SimplePlant.createAttributes()
                .add(Attributes.MAX_HEALTH, 8D)
                .add(Attributes.FOLLOW_RANGE, 2D);
    }

    @Override
    public int extraMaxSun(Entity giveTo) {
        return giveTo instanceof Player ? 50 : 0;
    }
}
