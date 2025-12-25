package com.hungteen.pvz.client.renderer.zombie;

import com.hungteen.pvz.client.model.zombie.PVZZombieModel;
import com.hungteen.pvz.common.entity.zombies.PVZZombie;
import com.hungteen.pvz.util.Util;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class LavaDiverZombieRenderer<T extends PVZZombie, M extends PVZZombieModel<T>> extends SnorkelZombieRenderer<T, M> {
    private static final ResourceLocation LOCATION = Util.prefix("textures/entity/zombie/lava_diver_zombie/lava_diver_zombie.png");
    public LavaDiverZombieRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(PVZZombie zombie) {
        return LOCATION;
    }
}
