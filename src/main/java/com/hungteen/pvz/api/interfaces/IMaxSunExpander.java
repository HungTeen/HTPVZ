package com.hungteen.pvz.api.interfaces;


import com.hungteen.pvz.util.MathUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.level.block.Block;

import java.util.UUID;

/**Add players' max sun amount if {@link com.hungteen.pvz.PVZConfig.Common#dynamicSunRule dynamicSunRule} is on.
 * <br>Can be Entity or Block.*/
public interface IMaxSunExpander {

    /**this function is called every time player get in the region unless it {@link IMaxSunExpander#requireRefreshExtraMaxSun() requireRefresh}.
     * <br>Player get the extra sun when stepping in a 6-block-rad region, and loses the modifier when 30 blocks away from them.
     * @param pos position of the max sun expander, for blocks to locate.*/
    int extraMaxSun(BlockPos pos, ISunContainer giveTo);
    int extraMaxSunLimit(BlockPos pos, ISunContainer giveTo);
    default boolean requireRefreshExtraMaxSun() {
        return false;
    }
    default UUID getUUID(BlockPos pos) {
        return this instanceof Block ? MathUtil.posToUuid(pos) : (this instanceof Entity e ? e.getUUID() : UUID.randomUUID());
    }

    default AttributeModifier getModifier(BlockPos pos, int maxSunLimit, ISunContainer iSunContainer) {
        return new AttributeModifier(getUUID(pos), "extra_max_sun"
                , Math.min(maxSunLimit - iSunContainer.getCapacity(), extraMaxSun(pos, iSunContainer)), AttributeModifier.Operation.ADDITION);
    }
}
