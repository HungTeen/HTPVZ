package com.hungteen.pvz.client.renderer.plants;

import com.hungteen.pvz.client.layer.LightLayer;
import com.hungteen.pvz.client.model.plants.TangleKelpModel;
import com.hungteen.pvz.client.renderer.PVZLayerHandler;
import com.hungteen.pvz.common.entity.plants.TangleKelp;
import com.hungteen.pvz.util.Util;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;


public class TangleKelpRenderer<T extends TangleKelp> extends MobRenderer<T, EntityModel<T>> {

    private static final ResourceLocation TEXTURE = Util.prefix("textures/entity/plants/tangle_kelp/tangle_kelp.png");
    public TangleKelpRenderer(EntityRendererProvider.Context context) {
        super(context, new TangleKelpModel<>(context.bakeLayer(PVZLayerHandler.LayerLocationMap.get("tangle_kelp:main"))), 0.6F);
        this.addLayer(new LightLayer<>(this, Util.prefix("textures/entity/plants/tangle_kelp/tangle_kelp_light.png")));
    }


    public void render(T sunflower, float p_115456_, float p_115457_, PoseStack poseStack, MultiBufferSource buffer, int p_115460_) {
        super.render(sunflower, p_115456_, p_115457_, poseStack, buffer, p_115460_);
    }

    @Override
    public ResourceLocation getTextureLocation(T sunflower) {
        return TEXTURE;
    }

}
