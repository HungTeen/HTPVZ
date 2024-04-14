package com.hungteen.pvz.api.interfaces;


import net.minecraft.world.entity.Entity;

/**Add players' max sun amount if {@link com.hungteen.pvz.PVZConfig.Common#dynamicSunRule dynamicSunRule} is on.
 * <br>Can be Entity or Block.*/
public interface IMaxSunExpander {
    int extraMaxSun(Entity giveTo);
}
