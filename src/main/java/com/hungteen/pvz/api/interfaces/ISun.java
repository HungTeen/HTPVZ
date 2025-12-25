package com.hungteen.pvz.api.interfaces;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**ISun can be absorbed by ISunAbsorber. It doesn't directly interact with ISunContainer but ISunContainers may call {@link ISun#getAmount()}.*/
public interface ISun extends ISunAbsorber, ISunContainer {

    void setAmount(int num);
    @Override
    int getAmount();
    @Override
    default int getCapacity() {
        return 150;
    }

    void onAbsorbedBy(ISunAbsorber entity);
    boolean canAttractThis(ISunAbsorber entity);

    /**Player can absorb sun but doesn't implement ISunAbsorber. So put it separately.*/
    void onAbsorbedBy(Player player);

    @Override
    default boolean canAbsorb(ISun sun) {
        return ISunAbsorber.super.canAbsorb(sun);
    }

    @Override
    void onAbsorb(ISun sun);


    boolean canAttractThis(Player player);

    /**@return Can be ISunAbsorber or player.*/
    Object getAttractor();
    /**@return whether succeeded in setting attractor.*/
    default boolean setAttractor(Object attractor) {
        return false;
    }

    @Override
    default Vec3 position() {
        return ISunAbsorber.super.position();
    }
}
