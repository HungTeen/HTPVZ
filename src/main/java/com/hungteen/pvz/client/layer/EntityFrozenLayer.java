package com.hungteen.pvz.client.layer;

import com.hungteen.pvz.client.model.attached.FrozenModel;
import com.hungteen.pvz.client.model.plants.WallNutModel;
import com.hungteen.pvz.client.renderer.PVZLayerHandler;
import com.hungteen.pvz.common.network.ClientProxy;
import com.hungteen.pvz.common.register.PVZMobEffects;
import com.hungteen.pvz.util.Util;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;

import static com.hungteen.pvz.common.entity.plants.WallNut.EXPLODE_COUNT;

public class EntityFrozenLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    private final LivingEntityRenderer<T, M> renderer;
    private final FrozenModel frozenModel;
    public EntityFrozenLayer(RenderLayerParent<T, M> layerParent) {
        super(layerParent);
        renderer = (LivingEntityRenderer<T, M>) layerParent;
        frozenModel = new FrozenModel<>(ClientProxy.MC.getEntityModels().bakeLayer(PVZLayerHandler.LayerLocationMap.get("ice:main")));
    }

    @Override
    public void render(PoseStack stack, MultiBufferSource bufferSource, int p_117351_, T entity, float p_117353_, float p_117354_, float p_117355_, float p_117356_, float p_117357_, float p_117358_) {
        EntityModel<T> model = renderer.getModel();
        if (entity.getTicksFrozen() > 0 && entity.hurtTime <= 0 && entity.deathTime <= 0) {
            model.renderToBuffer(stack, bufferSource.getBuffer(model.renderType(renderer.getTextureLocation(entity))), p_117351_, OverlayTexture.NO_OVERLAY, 0.4F, 0.8F, 1.0F, 1.0F);
        }
        if (entity.isAlive() && entity.getAttribute(Attributes.MOVEMENT_SPEED).getModifier(PVZMobEffects.FREEZE_EFFECT_UUID) != null) {
            VertexConsumer vertexConsumer = bufferSource.getBuffer(model.renderType(Util.prefix("textures/models/frozen/ice.png")));
            frozenModel.renderToBuffer(stack, vertexConsumer, p_117351_, OverlayTexture.NO_OVERLAY, 1F, 1F, 1F, 1F);
        }
    }
}
