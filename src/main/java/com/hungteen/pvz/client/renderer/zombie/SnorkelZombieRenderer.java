package com.hungteen.pvz.client.renderer.zombie;

import com.hungteen.pvz.client.model.zombie.PVZZombieModel;
import com.hungteen.pvz.client.model.zombie.SnorkelZombieModel;
import com.hungteen.pvz.client.renderer.PVZLayerHandler;
import com.hungteen.pvz.common.entity.zombies.PVZZombie;
import com.hungteen.pvz.util.Util;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class SnorkelZombieRenderer<T extends PVZZombie, M extends PVZZombieModel<T>> extends AbstractPVZZombieRenderer<T, M> {
    private static final ResourceLocation LOCATION = Util.prefix("textures/entity/zombie/snorkel_zombie/snorkel_zombie.png");
    public SnorkelZombieRenderer(EntityRendererProvider.Context context) {
        super(context, (M) new SnorkelZombieModel<T>(context.bakeLayer(PVZLayerHandler.LayerLocationMap.get("snorkel_zombie:main"))));
    }

    @Override
    public ResourceLocation getTextureLocation(PVZZombie zombie) {
        return LOCATION;
    }

}
