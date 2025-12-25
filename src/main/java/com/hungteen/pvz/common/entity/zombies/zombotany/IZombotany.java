package com.hungteen.pvz.common.entity.zombies.zombotany;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;

public interface IZombotany {

    default float getPlantHeadScale() {
        return 1.001F;
    }

    default Vec3 getPlantHeadOffset() {
        return Vec3.ZERO;
    }

    /**
     * 获取植物僵尸头部模型的资源位置，子类必须实现
     */
    default ResourceLocation getPlantTextureLocation() {
        return null;
    }
    /**
    * Return the type of the attached plant on the zombie.
    */
    EntityType<?> getPlantType();
}