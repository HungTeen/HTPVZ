package com.hungteen.pvz.common.network;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

/**
 * @program: pvzmod-1.18.x
 * @author: HungTeen
 * @create: 2022-03-08 19:59
 **/
public class ClientProxy extends CommonProxy {
    public static final Minecraft MC = Minecraft.getInstance();
    @Override
    public Player getPlayer() {
        return MC.player;
    }

}
