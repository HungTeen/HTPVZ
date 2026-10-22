package com.hungteen.pvz.client.renderer.zombies;

import com.hungteen.pvz.client.model.zombie.LadderZombieModel;
import com.hungteen.pvz.client.model.zombie.PVZZombieModel;
import com.hungteen.pvz.client.renderer.PVZLayerHandler;
import com.hungteen.pvz.common.entity.zombies.PVZZombie;
import com.hungteen.pvz.util.Util;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class LadderZombieRenderer<T extends PVZZombie, M extends PVZZombieModel<T>> extends AbstractPVZZombieRenderer<T, M> {
    private static final ResourceLocation TEXTURE = Util.prefix("textures/entity/zombie/ladder_zombie/ladder_zombie.png");
    public LadderZombieRenderer(EntityRendererProvider.Context context) {
        super(context, (M) new LadderZombieModel<T>(context.bakeLayer(PVZLayerHandler.LayerLocationMap.get("ladder_zombie:main"))));
    }

    @Override
    public ResourceLocation getTextureLocation(T zombie) {
        return TEXTURE;
    }
}
