package com.hungteen.pvz.common.network;

import com.hungteen.pvz.common.menu.EssenceAltarMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Send form Client to Server. Only when player is using Essence Altar ({@link com.hungteen.pvz.common.block.EssenceAltarBlock}).
 */
public class PVZAddSkillPacket {
    int skillID;

    public PVZAddSkillPacket(Integer skillID){
        this.skillID = skillID;
    }

    public PVZAddSkillPacket(FriendlyByteBuf buf) {
        this.skillID = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(skillID);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        final ServerPlayer player = ctx.get().getSender();
        ctx.get().enqueueWork(() -> {
            if (player != null && player.containerMenu instanceof EssenceAltarMenu menu) {
                menu.clickMenuButton(player, skillID);
            }
        });
        ctx.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    public static void addSkill(int skillID) {
        PVZPacketHandler.sendToServer(new PVZAddSkillPacket(skillID));
    }
}
