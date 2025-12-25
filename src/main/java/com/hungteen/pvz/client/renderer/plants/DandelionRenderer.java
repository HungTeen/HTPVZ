package com.hungteen.pvz.client.renderer.plants;

import com.hungteen.pvz.client.layer.NegativeBlockLayer;
import com.hungteen.pvz.client.model.plants.DandelionModel;
import com.hungteen.pvz.client.renderer.PVZLayerHandler;
import com.hungteen.pvz.common.entity.plants.Dandelion;
import com.hungteen.pvz.util.Util;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class DandelionRenderer<T extends Dandelion> extends MobRenderer<T, EntityModel<T>> {

    private static final ResourceLocation TEXTURE = Util.prefix("textures/entity/plants/dandelion/dandelion.png");
    private static final ResourceLocation HAIR_TEXTURE = Util.prefix("textures/entity/plants/dandelion/dandelion_hair.png");

    public DandelionRenderer(EntityRendererProvider.Context context) {
        super(context, new DandelionModel<>(context.bakeLayer(PVZLayerHandler.LayerLocationMap.get("dandelion:main"))), 0.5F);
        this.addLayer(new NegativeBlockLayer<>(this, HAIR_TEXTURE));
    }

    @Override
    public void render(T dandelion, float p_115456_, float p_115457_, PoseStack poseStack, MultiBufferSource buffer, int p_115460_) {
        super.render(dandelion, p_115456_, p_115457_, poseStack, buffer, p_115460_);
    }

    @Override
    public ResourceLocation getTextureLocation(T dandelion) {
        return TEXTURE;
    }

}