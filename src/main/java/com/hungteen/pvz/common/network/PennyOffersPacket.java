package com.hungteen.pvz.common.network;

import com.hungteen.pvz.common.menu.PennyMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundMerchantOffersPacket;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PennyOffersPacket extends ClientboundMerchantOffersPacket {

    public PennyOffersPacket(int p_132456_, MerchantOffers p_132457_, int p_132458_, int p_132459_, boolean p_132460_, boolean p_132461_) {
        super(p_132456_, p_132457_, p_132458_, p_132459_, p_132460_, p_132461_);
    }

    public PennyOffersPacket(FriendlyByteBuf buf) {
        super(buf);
    }

    public void toBytes(FriendlyByteBuf buf) {
        this.write(buf);
    }


    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            AbstractContainerMenu abstractcontainermenu = ClientProxy.getPlayer().containerMenu;
            if (this.getContainerId() == abstractcontainermenu.containerId && abstractcontainermenu instanceof PennyMenu menu) {
                menu.setOffers(new MerchantOffers(this.getOffers().createTag()));
                menu.setXp(this.getVillagerXp());
                menu.setMerchantLevel(this.getVillagerLevel());
                menu.setShowProgressBar(this.showProgress());
                menu.setCanRestock(this.canRestock());
                if (! menu.isVanillaUI) {
                    menu.merchantContainer.updateSellItem();
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
