package com.hungteen.pvz.common.network;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

public class ClientProxy extends CommonProxy {
    public static final Minecraft MC = Minecraft.getInstance();
    @Override
    public Player getPlayer() {
        return MC.player;
    }

}
