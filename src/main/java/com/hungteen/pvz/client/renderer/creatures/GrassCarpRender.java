package com.hungteen.pvz.client.renderer.creatures;

import com.hungteen.pvz.client.layer.GrassCarpItemLayer;
import com.hungteen.pvz.client.model.GrassCarpModel;
import com.hungteen.pvz.client.renderer.PVZLayerHandler;
import com.hungteen.pvz.common.entity.GrassCarp;
import com.hungteen.pvz.utils.Util;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * @program: pvzmod-1.18.x
 * @author: HungTeen
 * @create: 2022-03-28 20:53
 **/
public class GrassCarpRender extends MobRenderer<GrassCarp, GrassCarpModel<GrassCarp>> {

    private static final ResourceLocation RES = Util.prefix("textures/entity/grasscarp/grasscarp.png");
    public GrassCarpRender(EntityRendererProvider.Context rendererManager) {
        super(rendererManager, new GrassCarpModel<>(rendererManager.bakeLayer(PVZLayerHandler.LayerLocationMap.get("grass_carp:main"))), 0.4F);
        this.addLayer(new GrassCarpItemLayer<>(this));
    }

    @Override
    public ResourceLocation getTextureLocation(GrassCarp p_114482_) {
        return RES;
    }
}
