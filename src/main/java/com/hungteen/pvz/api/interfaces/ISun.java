package com.hungteen.pvz.api.interfaces;

import net.minecraft.world.entity.Entity;

public interface ISun {
    int getAmount();
    void setAmount(int num);
    void onAbsorbedBy(Entity entity);
    boolean canAttractThis(Entity entity);
}
