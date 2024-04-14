package com.hungteen.pvz.api.interfaces;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public interface ISun {
    int getAmount();
    void setAmount(int num);

    void onAbsorbedBy(ISunAbsorber entity);
    boolean canAttractThis(ISunAbsorber entity);

    /**Player can absorb sun but doesn't implement ISunAbsorber. So put it separately.*/
    void onAbsorbedBy(Player player);
    boolean canAttractThis(Player player);

    /**@return Can be ISunAbsorber or player.*/
    Object getAttractor();
    /**@return whether succeeded in setting attractor.*/
    default boolean setAttractor(Object attractor) {
        return false;
    }
}
