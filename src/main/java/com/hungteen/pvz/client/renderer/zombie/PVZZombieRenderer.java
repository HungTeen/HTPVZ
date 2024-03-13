package com.hungteen.pvz.client.renderer.zombie;

import com.hungteen.pvz.common.entity.zombies.PVZZombie;
import com.hungteen.pvz.util.Util;
import net.minecraft.client.model.ZombieModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.AbstractZombieRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Zombie;

public class PVZZombieRenderer extends AbstractZombieRenderer {
    private static final ResourceLocation OVERWORLD_LOCATION = Util.prefix("textures/entity/zombie/overworld_zombie.png");
    public PVZZombieRenderer(EntityRendererProvider.Context p_174458_, ModelLayerLocation p_174459_, ModelLayerLocation p_174460_, ModelLayerLocation p_174461_) {
        super(p_174458_, new ZombieModel<>(p_174458_.bakeLayer(p_174459_)), new ZombieModel<>(p_174458_.bakeLayer(p_174460_)), new ZombieModel<>(p_174458_.bakeLayer(p_174461_)));
    }
    public PVZZombieRenderer(EntityRendererProvider.Context p_174456_) {
        this(p_174456_, ModelLayers.ZOMBIE, ModelLayers.ZOMBIE_INNER_ARMOR, ModelLayers.ZOMBIE_OUTER_ARMOR);
    }

    public ResourceLocation getTextureLocation(Zombie zombie) {
        return zombie instanceof PVZZombie pvzZombie ? pvzZombie.getStyle().getPath().equals("") ? OVERWORLD_LOCATION :
                Util.prefix("textures/entity/zombie/" + pvzZombie.getStyle().getPath() + "_zombie.png") :
                super.getTextureLocation(zombie);
    }
}
