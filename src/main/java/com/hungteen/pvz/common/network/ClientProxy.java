package com.hungteen.pvz.common.network;

import com.hungteen.pvz.client.PVZClientEventHandler;
import com.hungteen.pvz.client.gui.PVZOverlayHandler;
import com.hungteen.pvz.client.gui.components.ClientSunImageToolTipComponent;
import com.hungteen.pvz.common.world.zen_garden.ZenGardenEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.IEventBus;

public class ClientProxy extends CommonProxy {
    public static final Minecraft MC = Minecraft.getInstance();

    public static Player getPlayer() {
        return MC.player;
    }

    public static Level getLevel() {
        return MC.level;
    }

    public void addClientListeners(IEventBus modBus, IEventBus forgeBus) {
        modBus.addListener(PVZOverlayHandler::registerOverlay);
        modBus.addListener(ClientSunImageToolTipComponent::register);
        modBus.addListener(ZenGardenEffects::register);
        modBus.addListener(PVZClientEventHandler::addLayers);
        modBus.addListener(PVZClientEventHandler::registerExtraModels);
        forgeBus.addListener(PVZClientEventHandler::renderPumpkinHelmet);
    }

}
