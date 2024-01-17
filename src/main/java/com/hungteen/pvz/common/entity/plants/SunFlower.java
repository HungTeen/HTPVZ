package com.hungteen.pvz.common.entity.plants;

import com.hungteen.pvz.common.entity.SimplePlant;
import com.hungteen.pvz.common.entity.plants.base.ProducerPlant;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

public class SunFlower extends ProducerPlant {
    public SunFlower(EntityType<? extends Mob> type, Level worldIn) {
        super(type, worldIn);
    }

    @Override
    protected void genSomething() {
        this.genSun(this.getSunAmount(),1);
    }

    @Override
    public int getGenCD() {
        final int time = 240;
        return this.level.isDay() ?(this.level.isRaining() ? 2 * time : time) : 3 * time;
    }
    public int getSunAmount(){
        return 25;
    }
    public static AttributeSupplier.Builder createAttributes() {
        return SimplePlant.createAttributes()
                .add(Attributes.MAX_HEALTH, 8D)
                .add(Attributes.FOLLOW_RANGE, 2D);
    }
}
