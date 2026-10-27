package com.hungteen.pvz.client.renderer.plants;

import com.hungteen.pvz.client.model.plants.FlowerPotGolemModel;
import com.hungteen.pvz.client.renderer.PVZLayerHandler;
import com.hungteen.pvz.common.entity.creatures.FlowerPotGolem;
import com.hungteen.pvz.util.Util;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;

/**
 * @author PangTeen
 * @program HTPVZ
 * @create 2026/10/26 23:00
 **/
public class FlowerPotGolemRenderer extends MobRenderer<FlowerPotGolem, FlowerPotGolemModel<FlowerPotGolem>> {

    private static final ResourceLocation TEXTURE =
            Util.prefix("textures/entity/plants/flower_pot/flower_pot.png");

    public FlowerPotGolemRenderer(EntityRendererProvider.Context context) {
        super(context, new FlowerPotGolemModel<>(
                context.bakeLayer(PVZLayerHandler.LayerLocationMap.get("flower_pot_golem:main"))), 0.4F);
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
    }

    @Override
    public ResourceLocation getTextureLocation(FlowerPotGolem golem) {
        return TEXTURE;
    }
}
