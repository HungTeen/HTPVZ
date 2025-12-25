package com.hungteen.pvz.common.network;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.IEventBus;

public class CommonProxy {
    public static Player getPlayer(){
        return null;
    }

    public void addClientListeners(IEventBus modBus, IEventBus forgeBus) {}

}
