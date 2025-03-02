package com.hungteen.pvz.common.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.level.Level;

public class ClientProxy extends CommonProxy {
    public static final Minecraft MC = Minecraft.getInstance();

    public static AbstractClientPlayer getPlayer() {
        return MC.player;
    }

    public static Level getLevel() {
        return MC.level;
    }

}
