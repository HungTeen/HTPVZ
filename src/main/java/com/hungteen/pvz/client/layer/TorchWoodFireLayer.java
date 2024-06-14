package com.hungteen.pvz.client.layer;

import com.hungteen.pvz.client.model.plants.TorchWoodModel;
import com.hungteen.pvz.common.entity.plants.TorchWood;
import com.hungteen.pvz.common.network.ClientProxy;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.block.Blocks;

public class TorchWoodFireLayer<T extends TorchWood, M extends TorchWoodModel<T>> extends RenderLayer<T, M> {
    private BlockRenderDispatcher blockRenderer = ClientProxy.MC.getBlockRenderer();
    public TorchWoodFireLayer(RenderLayerParent<T, M> p_117346_) {
        super(p_117346_);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int p_117351_, T torchWood, float p_117353_, float p_117354_, float p_117355_, float p_117356_, float p_117357_, float p_117358_) {
        poseStack.pushPose();
        poseStack.mulPose(Vector3f.XP.rotationDegrees(180.0F));
        poseStack.translate(-0.5, -0.8, -0.5);
        this.blockRenderer.renderSingleBlock(torchWood.isSoulFire() ? Blocks.SOUL_FIRE.defaultBlockState() : Blocks.FIRE.defaultBlockState(), poseStack, bufferSource, p_117351_, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }
}
