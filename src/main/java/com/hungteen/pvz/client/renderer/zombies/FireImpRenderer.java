package com.hungteen.pvz.client.renderer.zombies;

import com.hungteen.pvz.client.layer.LightLayer;
import com.hungteen.pvz.client.model.zombie.FireImpModel;
import com.hungteen.pvz.client.renderer.PVZLayerHandler;
import com.hungteen.pvz.common.entity.zombies.FireImp;
import com.hungteen.pvz.util.Util;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class FireImpRenderer<T extends FireImp, M extends FireImpModel<T>> extends AbstractPVZZombieRenderer<T, M> {
    private static final ResourceLocation TEXTURE = Util.prefix("textures/entity/zombie/imp/fire_imp.png");
    private static final  ResourceLocation LIGHT_TEXTURE = Util.prefix("textures/entity/zombie/imp/fire_imp_light.png");
    public FireImpRenderer(EntityRendererProvider.Context context) {
        super(context, (M) new FireImpModel<T>(context.bakeLayer(PVZLayerHandler.LayerLocationMap.get("fire_imp:main"))));
        this.addLayer(new LightLayer<>(this, getLightTextureLocation()));
    }
    @Override
    public ResourceLocation getTextureLocation(T zombie) {
        return TEXTURE;
    }
    public ResourceLocation getLightTextureLocation() {
        return LIGHT_TEXTURE;
    }
}
