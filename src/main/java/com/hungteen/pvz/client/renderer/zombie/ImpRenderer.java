package com.hungteen.pvz.client.renderer.zombie;

import com.hungteen.pvz.client.model.zombie.PVZZombieModel;
import com.hungteen.pvz.common.entity.zombies.PVZZombie;
import com.hungteen.pvz.common.network.ClientProxy;
import com.hungteen.pvz.util.Util;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

import java.io.FileNotFoundException;

public class ImpRenderer<T extends PVZZombie, M extends PVZZombieModel<T>> extends PVZZombieRenderer<T, M>{
    private static final ResourceLocation TEXTURE = Util.prefix("textures/entity/zombie/imp/imp.png");
    public ImpRenderer(EntityRendererProvider.Context context) {
        super(context);
    }
    @Override
    public ResourceLocation getTextureLocation(T zombie) {
//        try {
//            ResourceLocation res = zombie.getStyle().equals("") ? OVERWORLD_LOCATION :
//                    Util.prefix("textures/entity/zombie/" + zombie.getStyle() + "_zombie.png");
//            ClientProxy.MC.getResourceManager().getResourceOrThrow(res);
//            return res;
//        } catch (FileNotFoundException e) {
//            return OVERWORLD_LOCATION;
//        }
        return TEXTURE;
    }
}
