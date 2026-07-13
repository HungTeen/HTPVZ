package com.hungteen.pvz.client.renderer.blockentity;

import com.hungteen.pvz.common.network.ClientProxy;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.SpawnerRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;

public class TombstoneRender extends SpawnerRenderer {

    public TombstoneRender(BlockEntityRendererProvider.Context p_173673_) {
        super(p_173673_);
    }

    @Override
    public void render(SpawnerBlockEntity p_112563_, float p_112564_, PoseStack p_112565_, MultiBufferSource p_112566_, int p_112567_, int p_112568_) {
        Player player = ClientProxy.getPlayer();
        if (player == null
                || ! (player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof SpawnEggItem
                        || player.getItemInHand(InteractionHand.OFF_HAND).getItem() instanceof SpawnEggItem)) {
            return;
        }
        p_112565_.pushPose();
        p_112565_.translate(0, 1, 0);
        super.render(p_112563_, p_112564_, p_112565_, p_112566_, p_112567_, p_112568_);
        p_112565_.popPose();
    }
}
