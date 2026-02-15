package com.hungteen.pvz.client.renderer.zombies;

import com.hungteen.pvz.client.model.zombie.PVZZombieModel;
import com.hungteen.pvz.common.entity.zombies.PVZZombie;
import com.hungteen.pvz.common.network.ClientProxy;
import com.hungteen.pvz.util.Util;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

import java.io.FileNotFoundException;

public class PVZZombieRenderer<T extends PVZZombie, M extends PVZZombieModel<T>> extends AbstractPVZZombieRenderer<T, M> {
    private static final ResourceLocation OVERWORLD_LOCATION = Util.prefix("textures/entity/zombie/minecraft_overworld_zombie.png");
    public PVZZombieRenderer(EntityRendererProvider.Context context) {
        super(context, (M) new PVZZombieModel<T>(context.bakeLayer(ModelLayers.PLAYER)));
    }

    @Override
    public ResourceLocation getTextureLocation(T zombie) {
        try {
            ResourceLocation res = zombie.getStyle().equals("") ? OVERWORLD_LOCATION :
                    Util.prefix("textures/entity/zombie/" + zombie.getStyle() + "_zombie.png");
            ClientProxy.MC.getResourceManager().getResourceOrThrow(res);
            return res;
        } catch (FileNotFoundException e) {
            return OVERWORLD_LOCATION;
        }
    }
}
