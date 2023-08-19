package com.hungteen.pvz.common.capability;

import com.hungteen.pvz.Util;
import com.hungteen.pvz.common.capability.player.PVZPlayerCapability;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CapabilityHandler {

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent ev){
        ev.register(PVZPlayerCapability.class);
    }

    @SubscribeEvent
    public static void attachCapabilities(AttachCapabilitiesEvent<Entity> ev){
        Entity entity = ev.getObject();
        if (entity instanceof Player){
            ev.addCapability(Util.prefix("player_data"), new PVZPlayerCapability((Player) entity));
        }
    }
}
