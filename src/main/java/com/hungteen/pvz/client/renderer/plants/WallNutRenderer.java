package com.hungteen.pvz.client.renderer.plants;

import com.hungteen.pvz.Util;
import com.hungteen.pvz.client.layer.GrassCarpItemLayer;
import com.hungteen.pvz.client.layer.WallNutColorLayer;
import com.hungteen.pvz.client.model.plants.WallNutModel;
import com.hungteen.pvz.client.renderer.PVZLayerHandler;
import com.hungteen.pvz.common.entity.plants.WallNut;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;


public class WallNutRenderer<T extends WallNut> extends MobRenderer<T, EntityModel<T>> {

    private static final ResourceLocation STATE0 = Util.prefix("textures/entity/plants/wall_nut/wall_nut.png");
    private static final ResourceLocation STATE1 = Util.prefix("textures/entity/plants/wall_nut/wall_nut_1.png");
    private static final ResourceLocation STATE2 = Util.prefix("textures/entity/plants/wall_nut/wall_nut_2.png");

    public WallNutRenderer(EntityRendererProvider.Context context) {
        super(context, new WallNutModel<>(context.bakeLayer(PVZLayerHandler.LayerLocationMap.get("wall_nut:main"))), 0.6F);
        this.addLayer(new WallNutColorLayer(this, context.getModelSet()));
    }

    @Override
    public ResourceLocation getTextureLocation(T wallNut) {
        float healthPercent = wallNut.getHealth()/wallNut.getMaxHealth();
        return healthPercent > 0.67 ? STATE0 : (healthPercent > 0.33 ? STATE1 : STATE2);
    }

}
