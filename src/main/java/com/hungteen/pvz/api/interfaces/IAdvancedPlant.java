package com.hungteen.pvz.api.interfaces;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;


/**This class is not yet used. GrassCarp will use this after updating cost mechanism. */
@Deprecated
public interface IAdvancedPlant {
    default int getExtraCost() {
        return 50;
    }

    static int getExtraCost(Class<Entity> clazz, ServerLevel level, BlockPos position) {
        AtomicInteger result = new AtomicInteger();
        if (Arrays.asList(clazz.getInterfaces()).contains(IAdvancedPlant.class)) {
            level.getEntitiesOfClass(clazz, new AABB(position).inflate(20))
                    .forEach(entity1 -> result.addAndGet(((IAdvancedPlant) entity1).getExtraCost()));
        }
        return result.get();
    }
}
