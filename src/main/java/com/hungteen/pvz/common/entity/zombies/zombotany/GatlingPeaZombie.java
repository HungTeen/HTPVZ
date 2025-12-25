package com.hungteen.pvz.common.entity.zombies.zombotany;

import com.hungteen.pvz.common.register.PVZEntities;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;

import java.util.Set;

public class GatlingPeaZombie extends PeaShooterZombie {
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState shootAnimationState = new AnimationState();

    public GatlingPeaZombie(EntityType<? extends Zombie> type, Level level) {
        super(type, level);
    }

    public Set<Integer> shootTimes() {
        return Set.of(8, 10, 12, 14);
    }

    public int getShootCD() {
        return 40;
    }

    public float getBulletSpeed() {
        return 1.5F;
    }

    @Override
    public EntityType<?> getPlantType() {
        return PVZEntities.GATLING_PEA.get();
    }
    @Override
    public ResourceLocation getPlantTextureLocation() {
        return null;
    }
    public AnimationState getAnimationState(String name) {
        return switch (name) {
            case "idle" -> this.idleAnimationState;
            case "shoot" -> this.shootAnimationState;
            default -> null;
        };
    }
} 