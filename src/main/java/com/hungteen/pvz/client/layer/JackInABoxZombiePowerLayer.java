package com.hungteen.pvz.client.layer;

import com.hungteen.pvz.client.model.zombie.JackInABoxZombieModel;
import com.hungteen.pvz.common.entity.zombies.JackInABoxZombie;
import com.hungteen.pvz.util.Util;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EnergySwirlLayer;
import net.minecraft.resources.ResourceLocation;

public class JackInABoxZombiePowerLayer extends EnergySwirlLayer<JackInABoxZombie, JackInABoxZombieModel<JackInABoxZombie>> {
    private static final ResourceLocation TEXTURE = Util.prefix("textures/entity/zombie/jack_in_a_box_zombie/jack_in_a_box_zombie_light.png");
    private final JackInABoxZombieModel<JackInABoxZombie> model;

    public JackInABoxZombiePowerLayer(RenderLayerParent<JackInABoxZombie, JackInABoxZombieModel<JackInABoxZombie>> p_174471_, EntityModelSet p_174472_) {
        super(p_174471_);
        this.model = p_174471_.getModel();
    }

    protected float xOffset(float p_116683_) {
        return p_116683_ * 0.01F;
    }

    protected ResourceLocation getTextureLocation() {
        return TEXTURE;
    }

    protected JackInABoxZombieModel<JackInABoxZombie> model() {
        return this.model;
    }
}
