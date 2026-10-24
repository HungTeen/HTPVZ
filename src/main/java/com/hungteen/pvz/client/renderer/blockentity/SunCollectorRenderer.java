package com.hungteen.pvz.client.renderer.blockentity;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.client.model.FloatEssenceBlockModel;
import com.hungteen.pvz.client.renderer.PVZLayerHandler;
import com.hungteen.pvz.common.block.entity.SunCollectorBlockEntity;
import com.hungteen.pvz.util.Util;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * @author PangTeen
 * @program HTPVZ
 * @create 2026/10/24 16:00
 **/
public class SunCollectorRenderer implements BlockEntityRenderer<SunCollectorBlockEntity> {

    private final FloatEssenceBlockModel model;
    private static final ResourceLocation TEXTURE = Util.prefix("textures/blockentity/float_essence_block.png");

    public SunCollectorRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new FloatEssenceBlockModel(
                context.bakeLayer(PVZLayerHandler.LayerLocationMap.get("floating_essence_block:main")));
    }

    @Override
    public void render(SunCollectorBlockEntity blockEntity, float partialTicks, PoseStack stack,
                       MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {
        stack.pushPose();
        stack.scale(-1.0F, -1.0F, 1.0F);
        BlockPos pos = blockEntity.getBlockPos();
        int fakeRandom = (pos.getX() % 53 * (pos.getX() + 1)) % 13
                + (pos.getY() % 57 * (pos.getY() + 1)) % 17
                + (pos.getZ() % 59 * (pos.getZ() + 1)) % 19;
        stack.translate(-0.5D, -2.3D - 0.15D * Math.sin(PVZMod.clientTime * 0.4D * Mth.PI + fakeRandom), 0.5D);
        VertexConsumer builder = bufferSource.getBuffer(RenderType.entityCutoutNoCull(TEXTURE, false));
        this.model.setupAnim(PVZMod.clientTime + fakeRandom % 61 * (fakeRandom + 1));
        this.model.renderToBuffer(stack, builder, combinedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        stack.popPose();
    }
}
