package com.hungteen.pvz.client.renderer.blockentity;

import com.hungteen.pvz.client.model.FloatEssenceBlockModel;
import com.hungteen.pvz.client.renderer.PVZLayerHandler;
import com.hungteen.pvz.common.block.entity.EssenceAltarBlockEntity;
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

public class EssenceAltarRenderer implements BlockEntityRenderer<EssenceAltarBlockEntity> {
    private final FloatEssenceBlockModel model;
    public static float time = 0;
    private final ResourceLocation RES = Util.prefix("textures/blockentity/float_essence_block.png");

    public EssenceAltarRenderer(BlockEntityRendererProvider.Context p_173619_) {
        this.model = new FloatEssenceBlockModel(p_173619_.bakeLayer(PVZLayerHandler.LayerLocationMap.get("floating_essence_block:main")));
    }
    @Override
    public void render(EssenceAltarBlockEntity blockEntity, float partialTicks, PoseStack stack, MultiBufferSource bufferSource, int combinedLightIn, int combinedOverlayIn) {
        stack.pushPose();
        stack.scale(- 1, - 1, 1);
        float size = 1F;
        BlockPos pos =  blockEntity.getBlockPos();
        int fakeRandom = (pos.getX() % 53 * (pos.getX() + 1)) % 13 + (pos.getY() % 57 * (pos.getY() + 1)) % 17 + (pos.getZ() % 59 * (pos.getZ() + 1)) % 19;
        stack.scale(size, size, size);
        stack.translate(- 0.5 / size, - 2.3D - 0.15 * Math.sin(time * 0.02 * Mth.PI + fakeRandom), 0.5 / size);
        VertexConsumer builder = bufferSource.getBuffer(RenderType.entityTranslucentCull(RES));
        model.renderToBuffer(stack, builder, combinedLightIn, OverlayTexture.NO_OVERLAY, 1, 1, 1 ,1);
        model.setupAnim(time + fakeRandom % 61 * (fakeRandom + 1));
        stack.popPose();
    }
}
