package com.hungteen.pvz.common.event;

import com.hungteen.pvz.common.entity.creatures.Sprout;
import net.minecraftforge.eventbus.api.Event;

public class SproutTransformEvent extends Event {
    public Sprout sprout;
    public String name;

    public SproutTransformEvent(Sprout sprout, String name) {
        super();
        this.sprout = sprout;
        this.name = name;
    }
}
