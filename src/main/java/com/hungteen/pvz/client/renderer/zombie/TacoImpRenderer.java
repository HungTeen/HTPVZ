package com.hungteen.pvz.client.renderer.zombie;

import com.hungteen.pvz.client.model.zombie.TacoImpModel;
import com.hungteen.pvz.client.renderer.PVZLayerHandler;
import com.hungteen.pvz.common.entity.zombies.TacoImp;
import com.hungteen.pvz.util.Util;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class TacoImpRenderer<T extends TacoImp, M extends TacoImpModel<T>> extends AbstractPVZZombieRenderer<T, M> {
    private static final ResourceLocation TEXTURE = Util.prefix("textures/entity/zombie/imp/taco_imp.png");
    public TacoImpRenderer(EntityRendererProvider.Context context) {
        super(context, (M) new TacoImpModel<T>(context.bakeLayer(PVZLayerHandler.LayerLocationMap.get("taco_imp:main"))));
    }
    @Override
    public ResourceLocation getTextureLocation(T zombie) {
        return TEXTURE;
    }
}
