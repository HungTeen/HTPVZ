package com.hungteen.pvz.client.layer;

import com.hungteen.pvz.client.model.zombie.FireImpModel;
import com.hungteen.pvz.common.entity.zombies.FireImp;
import com.hungteen.pvz.util.Util;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;

public class GhastRiderLightLayer<T extends FireImp, M extends FireImpModel<T>> extends EyesLayer<T, M> {
    private static final  ResourceLocation LIGHT_TEXTURE = Util.prefix("textures/entity/zombie/imp/ghast_rider_light.png");
    private static final RenderType RENDER_TYPE = RenderType.eyes(LIGHT_TEXTURE);

    public GhastRiderLightLayer(RenderLayerParent<T, M> p_116964_) {
        super(p_116964_);
    }

    public RenderType renderType() {
        return RENDER_TYPE;
    }
}
