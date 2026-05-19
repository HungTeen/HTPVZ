package com.hungteen.pvz.common.capability;

import com.hungteen.pvz.common.capability.entity.PVZEntityCapability;
import com.hungteen.pvz.common.capability.level.PVZFogCapability;
import com.hungteen.pvz.common.capability.level.PVZZombieEventCapability;
import com.hungteen.pvz.common.capability.player.PVZPlayerCapability;
import com.hungteen.pvz.util.Util;
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
        ev.register(PVZEntityCapability.class);
        ev.register(PVZFogCapability.class);
    }

    @SubscribeEvent
    public static void attachEntityCaps(AttachCapabilitiesEvent<Entity> ev){
        Entity entity = ev.getObject();
        if (entity instanceof Player){
            ev.addCapability(Util.prefix("player_data"), new PVZPlayerCapability((Player) entity));
        }
        ev.addCapability(Util.prefix("entity_data"), new PVZEntityCapability(entity));
    }

    @SubscribeEvent
    public static void attachLevelCaps(AttachCapabilitiesEvent<Level> ev) {
        ev.addCapability(Util.prefix("pvz_fog"), new PVZFogCapability(ev.getObject()));
        ev.addCapability(Util.prefix("zombie_event"), new PVZZombieEventCapability(ev.getObject()));
    }
}
