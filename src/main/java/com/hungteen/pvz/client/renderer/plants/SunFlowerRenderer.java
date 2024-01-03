package com.hungteen.pvz.client.renderer.plants;

import com.hungteen.pvz.client.layer.fullskin.SunLightLayer;
import com.hungteen.pvz.client.model.plants.SunFlowerModel;
import com.hungteen.pvz.client.renderer.PVZLayerHandler;
import com.hungteen.pvz.common.entity.plants.SunFlower;
import com.hungteen.pvz.util.Util;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;


public class SunFlowerRenderer<T extends SunFlower> extends MobRenderer<T, EntityModel<T>> {

    private static final ResourceLocation STATE0 = Util.prefix("textures/entity/plants/sun_flower/sun_flower.png");
    public SunFlowerRenderer(EntityRendererProvider.Context context) {
        super(context, new SunFlowerModel<>(context.bakeLayer(PVZLayerHandler.LayerLocationMap.get("sun_flower:main"))), 0.6F);
        this.addLayer(new SunLightLayer<>(this, Util.prefix("textures/entity/plants/sun_flower/sun_flower_light.png")));
    }


    public void render(T sunflower, float p_115456_, float p_115457_, PoseStack poseStack, MultiBufferSource buffer, int p_115460_) {
        super.render(sunflower, p_115456_, p_115457_, poseStack, buffer, p_115460_);
    }

    @Override
    public ResourceLocation getTextureLocation(T sunflower) {
        return STATE0;
    }

}
