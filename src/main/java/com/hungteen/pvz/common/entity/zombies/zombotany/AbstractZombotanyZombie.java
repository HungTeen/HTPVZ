package com.hungteen.pvz.common.entity.zombies.zombotany;

import com.hungteen.pvz.common.entity.zombies.PVZZombie;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;

public abstract class AbstractZombotanyZombie extends PVZZombie {

    public boolean controlledByRenderHand = true; // controlled by renderer.
    public boolean controlledByRenderHead = true; // controlled by renderer.
    public boolean controlledByRenderHat = true; // controlled by renderer.
    public AbstractZombotanyZombie(EntityType<? extends Zombie> p_34271_, Level p_34272_) {
        super(p_34271_, p_34272_);
    }


    public float getPlantHeadScale() {
        return 1.0F;
    }

    public float getPlantHeadOffsetX() {
        return 0.0F;
    }

    public float getPlantHeadOffsetY() {
        return 0.0F;
    }

    public float getPlantHeadOffsetZ() {
        return 0.0F;
    }

    /**
     * 获取植物僵尸头部模型的资源位置，子类必须实现
     */

    public abstract ResourceLocation getPlantTextureLocation();

    /**
     * 获取对应的植物模型类名，子类必须实现
     */
    public abstract String getPlantModelClassName();
}