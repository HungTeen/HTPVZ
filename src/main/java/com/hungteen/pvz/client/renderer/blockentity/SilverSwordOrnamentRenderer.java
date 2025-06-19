package com.hungteen.pvz.client.renderer.blockentity;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.client.model.SilverSwordOrnamentModel;
import com.hungteen.pvz.client.renderer.PVZLayerHandler;
import com.hungteen.pvz.common.block.SilverSwordOrnamentBlock;
import com.hungteen.pvz.common.block.entity.SilverSwordOrnamentBlockEntity;
import com.hungteen.pvz.common.network.ClientProxy;
import com.hungteen.pvz.util.Util;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

public class SilverSwordOrnamentRenderer implements BlockEntityRenderer<SilverSwordOrnamentBlockEntity> {
    private final SilverSwordOrnamentModel model;
    private final ResourceLocation RES = Util.prefix("textures/blockentity/silver_sword_ornament.png");

    public SilverSwordOrnamentRenderer(BlockEntityRendererProvider.Context p_173619_) {
        this.model = new SilverSwordOrnamentModel(p_173619_.bakeLayer(PVZLayerHandler.LayerLocationMap.get("silver_sword_ornament:main")));
    }
    @Override
    public void render(SilverSwordOrnamentBlockEntity blockEntity, float partialTicks, PoseStack stack, MultiBufferSource bufferSource, int combinedLightIn, int combinedOverlayIn) {
        stack.pushPose();
        stack.scale(1, - 1, 1);
        BlockPos pos =  blockEntity.getBlockPos();
        BlockState state = ClientProxy.getLevel().getBlockState(pos);
        if (state.getBlock() instanceof SilverSwordOrnamentBlock) {
            stack.translate(0.5, - 1.68D, 0.5);
            stack.mulPose(Vector3f.YP.rotation(- state.getValue(SilverSwordOrnamentBlock.FACING).toYRot() / 57.3F));
            VertexConsumer builder = bufferSource.getBuffer(RenderType.entityCutoutNoCull(RES, false));
            model.setupAnim(null, pos.getX() + 0.5F, pos.getY() + 0.5F, partialTicks + PVZMod.clientTime * 20, pos.getZ() + 0.5F, 0);
            model.renderToBuffer(stack, builder, combinedLightIn, OverlayTexture.NO_OVERLAY, 1, 1, 1 ,1);
        }
        stack.popPose();

    }
}
