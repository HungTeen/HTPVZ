package com.hungteen.pvz.common.capability;

import com.hungteen.pvz.common.capability.fog.PVZFogCapability;
import com.hungteen.pvz.common.capability.owned.PVZOwnedCapability;
import com.hungteen.pvz.common.capability.player.PVZPlayerCapability;
import com.hungteen.pvz.util.Util;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CapabilityHandler {

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent ev){
        ev.register(PVZPlayerCapability.class);
        ev.register(PVZOwnedCapability.class);
        ev.register(PVZFogCapability.class);
    }

    @SubscribeEvent
    public static void attachCapabilities(AttachCapabilitiesEvent<Entity> ev){
        Entity entity = ev.getObject();
        if (entity instanceof Player){
            ev.addCapability(Util.prefix("player_data"), new PVZPlayerCapability((Player) entity));
        }
        if (entity.level != null && entity.getServer() != null){// deliberately. entity.getServer() sometimes lead to a client error because (level == null).
            ev.addCapability(Util.prefix("owned_data"), new PVZOwnedCapability(entity));
        }
    }

    @SubscribeEvent
    public static void initPVZRules(AttachCapabilitiesEvent<Level> ev) {
        if (ev.getObject() instanceof ServerLevel) {
            ev.addCapability(Util.prefix("pvz_fog"), new PVZFogCapability());
        }
    }
}
