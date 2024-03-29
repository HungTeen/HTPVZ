package com.hungteen.pvz.client.renderer.plants;

import com.hungteen.pvz.client.layer.fullskin.LightLayer;
import com.hungteen.pvz.client.model.plants.GoldBloomModel;
import com.hungteen.pvz.client.model.plants.JalapenoModel;
import com.hungteen.pvz.client.renderer.PVZLayerHandler;
import com.hungteen.pvz.common.entity.plants.GoldBloom;
import com.hungteen.pvz.common.entity.plants.Jalapeno;
import com.hungteen.pvz.util.Util;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;


public class GoldBloomRenderer<T extends GoldBloom> extends MobRenderer<T, EntityModel<T>> {

    private static final ResourceLocation TEXTURE = Util.prefix("textures/entity/plants/gold_bloom/gold_bloom.png");
    public GoldBloomRenderer(EntityRendererProvider.Context context) {
        super(context, new GoldBloomModel<>(context.bakeLayer(PVZLayerHandler.LayerLocationMap.get("gold_bloom:main"))), 0.4F);
        this.addLayer(new LightLayer<>(this, Util.prefix("textures/entity/plants/gold_bloom/gold_bloom_light.png")));
    }


    public void render(T goldBloom, float p_115456_, float p_115457_, PoseStack poseStack, MultiBufferSource buffer, int p_115460_) {
        super.render(goldBloom, p_115456_, p_115457_, poseStack, buffer, p_115460_);
    }
    @Override
    public ResourceLocation getTextureLocation(T goldBloom) {
        return TEXTURE;
    }

}
