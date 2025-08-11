package com.hungteen.pvz.common.entity.zombies.zombotany;

import com.hungteen.pvz.common.entity.bullet.PeaBullet;
import com.hungteen.pvz.common.register.PVZEntities;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;

public class SnowPeaZombie extends PeaShooterZombie {
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState shootAnimationState = new AnimationState();

    public SnowPeaZombie(EntityType<? extends Zombie> type, Level level) {
        super(type, level);
    }
    @Override
    public EntityType<?> getPlantType() {
        return PVZEntities.SNOW_PEA.get();
    }
    @Override
    public ResourceLocation getPlantTextureLocation() {
        return null;
    }

    @Override
    protected PeaBullet createBullet() {
        PeaBullet bullet = new PeaBullet(this.level, this, PeaBullet.PeaType.Common);
        bullet.setPeaType(PeaBullet.PeaType.Ice);
        return bullet;
    }
    @Override
    public boolean canFreeze() {
        return false;
    }
} 