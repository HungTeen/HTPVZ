package com.hungteen.pvz.common.network;

import com.hungteen.pvz.common.capability.player.PVZPlayerCapability;
import com.hungteen.pvz.common.entity.IEntityPacketHandler;
import com.hungteen.pvz.common.item.EnderSeedBundleItem;
import com.hungteen.pvz.common.item.SeedPacketItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class PVZEntityInteractPacket {
    private final UUID entityID;
    private final int type;

    public PVZEntityInteractPacket(Entity entity, int type) {
        this.entityID = entity.getUUID();
        this.type = type;
    }

    public PVZEntityInteractPacket(FriendlyByteBuf buf) {
        this.entityID = buf.readUUID();
        this.type = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(entityID);
        buf.writeInt(type);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        ctx.get().enqueueWork(() -> {
            Entity entity = ((ServerLevel) context.getSender().level).getEntity(this.entityID);
            if (entity instanceof IEntityPacketHandler entity1) {
                entity1.handlePVZPacket(ctx.get().getSender(), type);
            } else if (entity instanceof Player player) {
                ItemStack itemStack = EnderSeedBundleItem.getHoldingEnderSeedBundle(player);
                if (itemStack.getItem() instanceof EnderSeedBundleItem item) {
                    if (type < 9) {//select slot
                        item.setPointer(itemStack, type);
                    } else if (type == 9) {//swap to offhand
                        boolean mainHandHoldingBundle = itemStack == player.getMainHandItem();
                        ItemStack swapping = (mainHandHoldingBundle ? player.getOffhandItem() : player.getMainHandItem());
                        if (swapping.getItem() instanceof SeedPacketItem<?> || swapping.isEmpty()) { //swappable
                            int slot = item.getPointer(mainHandHoldingBundle ? player.getMainHandItem() : player.getOffhandItem());
                            ItemStack itemStack1;
                            if (mainHandHoldingBundle) {
                                itemStack1 = player.getOffhandItem();
                                player.setItemInHand(InteractionHand.OFF_HAND, PVZPlayerCapability.getEnderSeedBundleSlot(player, slot));
                            } else {
                                itemStack1 = player.getMainHandItem();
                                player.setItemInHand(InteractionHand.MAIN_HAND, PVZPlayerCapability.getEnderSeedBundleSlot(player, slot));
                            }
                            PVZPlayerCapability.setEnderSeedBundleSlot(player, slot, itemStack1);
                        }
                    } else if (type == 10) {//drop
                        int slot = item.getPointer(itemStack);
                        ItemStack toDrop = PVZPlayerCapability.getEnderSeedBundleSlot(player, slot);
                        if (! toDrop.isEmpty()) player.drop(toDrop, false);
                        PVZPlayerCapability.setEnderSeedBundleSlot(player, slot, ItemStack.EMPTY);
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
