package com.hungteen.pvz.api.events;

import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.EntityEvent;

@net.minecraftforge.eventbus.api.Cancelable
public class GardenPlantGrowUpEvent extends EntityEvent {
    public boolean shouldProduce;
    public GardenPlantGrowUpEvent(Entity entity, boolean shouldProduce) {
        super(entity);
        this.shouldProduce = shouldProduce;
    }
}
