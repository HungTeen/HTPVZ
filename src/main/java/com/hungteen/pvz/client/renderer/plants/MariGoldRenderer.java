package com.hungteen.pvz.client.renderer.plants;

import com.hungteen.pvz.client.layer.fullskin.LightLayer;
import com.hungteen.pvz.client.model.plants.MariGoldModel;
import com.hungteen.pvz.client.renderer.PVZLayerHandler;
import com.hungteen.pvz.common.entity.plants.MariGold;
import com.hungteen.pvz.util.Util;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class MariGoldRenderer<T extends MariGold> extends MobRenderer<T, EntityModel<T>> {

    private static final ResourceLocation STATE0 = Util.prefix("textures/entity/plants/marigold/marigold.png");
    public MariGoldRenderer(EntityRendererProvider.Context context) {
        super(context, new MariGoldModel<>(context.bakeLayer(PVZLayerHandler.LayerLocationMap.get("marigold:main"))), 0.6F);
        this.addLayer(new LightLayer<>(this, Util.prefix("textures/entity/plants/marigold/marigold_light.png")));
    }


    public void render(T sunflower, float p_115456_, float p_115457_, PoseStack poseStack, MultiBufferSource buffer, int p_115460_) {
        super.render(sunflower, p_115456_, p_115457_, poseStack, buffer, p_115460_);
    }

    @Override
    public ResourceLocation getTextureLocation(T sunflower) {
        return STATE0;
    }

}
