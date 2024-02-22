package com.hungteen.pvz.client.layer;

import com.hungteen.pvz.client.model.attached.DirtModel;
import com.hungteen.pvz.client.renderer.PVZLayerHandler;
import com.hungteen.pvz.common.network.ClientProxy;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class DirtLayer<T extends LivingEntity> extends RenderLayer<T, DirtModel<T>> {
    private final DirtModel<T> model;
    public DirtLayer(RenderLayerParent<T, DirtModel<T>> p_117346_, EntityModelSet modelSet) {
        super(p_117346_);
        this.model = new DirtModel<>(modelSet.bakeLayer(PVZLayerHandler.LayerLocationMap.get("dirt:main")));
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int p_117351_, T entity, float entityYaw, float partialTicks, float p_117355_, float p_117356_, float p_117357_, float p_117358_) {
        final ResourceLocation blockRes = ClientProxy.MC.getBlockRenderer().getBlockModelShaper().getTexture(entity.level.getBlockState(entity.blockPosition().below()), entity.level, entity.blockPosition().below()).getName();
        final ResourceLocation textureRes = new ResourceLocation(blockRes.getNamespace(), "textures/" + blockRes.getPath() + ".png");
        VertexConsumer vertexconsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(textureRes));
        this.model.setupAnim(entity, 0, 0, entity.tickCount + partialTicks, 0, 0);
        this.model.renderToBuffer(poseStack, vertexconsumer, p_117351_, OverlayTexture.NO_OVERLAY, 1F, 1F, 1F, 1.0F);

    }


}
