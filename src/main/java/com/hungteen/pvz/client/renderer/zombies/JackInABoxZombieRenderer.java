package com.hungteen.pvz.client.renderer.zombies;

import com.hungteen.pvz.client.layer.JackInABoxZombiePowerLayer;
import com.hungteen.pvz.client.model.zombie.JackInABoxZombieModel;
import com.hungteen.pvz.client.renderer.PVZLayerHandler;
import com.hungteen.pvz.common.entity.zombies.JackInABoxZombie;
import com.hungteen.pvz.util.Util;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;

public class JackInABoxZombieRenderer<T extends JackInABoxZombie, M extends JackInABoxZombieModel<T>> extends AbstractPVZZombieRenderer<T, M> {
    private static final ResourceLocation TEXTURE = Util.prefix("textures/entity/zombie/jack_in_a_box_zombie/jack_in_a_box_zombie.png");
    public JackInABoxZombieRenderer(EntityRendererProvider.Context context) {
        super(context, (M) new JackInABoxZombieModel<T>(context.bakeLayer(PVZLayerHandler.LayerLocationMap.get("jack_in_a_box_zombie:main"))));
        this.addLayer(
                (RenderLayer<T, M>) new JackInABoxZombiePowerLayer((RenderLayerParent<JackInABoxZombie, JackInABoxZombieModel<JackInABoxZombie>>) this
                        , context.getModelSet()));
    }

    @Override
    public ResourceLocation getTextureLocation(T zombie) {
        return TEXTURE;
    }
}
