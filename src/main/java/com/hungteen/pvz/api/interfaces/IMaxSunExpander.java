package com.hungteen.pvz.api.interfaces;


import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;

/**Add players' max sun amount if {@link com.hungteen.pvz.PVZConfig.Common#dynamicSunRule dynamicSunRule} is on.
 * <br>Can be Entity or Block.*/
public interface IMaxSunExpander {
    /**this function is called every time player get in the region unless it {@link IMaxSunExpander#requireRefreshExtraMaxSun() requireRefresh}.
     * <br>Player get the extra sun when stepping in a 6-block-rad region, and loses the modifier when 30 blocks away from them.
     * @param pos position of the max sun expander, for blocks to locate.*/
    int extraMaxSun(BlockPos pos, Entity giveTo);
    default boolean requireRefreshExtraMaxSun() {
        return false;
    }
}
