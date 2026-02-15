package com.hungteen.pvz.client.renderer.creatures;

import com.hungteen.pvz.client.model.SnailModel;
import com.hungteen.pvz.client.renderer.PVZLayerHandler;
import com.hungteen.pvz.common.entity.creatures.Snail;
import com.hungteen.pvz.util.Util;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;

public class SnailRenderer<T extends Snail, M extends SnailModel<T>> extends MobRenderer<T, M> {
    public SnailRenderer(EntityRendererProvider.Context context, SnailModel.Type type) {
        super(context, (M) new SnailModel<>(context.bakeLayer(PVZLayerHandler.LayerLocationMap.get("snail:main")), type), 0.4F);
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
    }

    @Override
    public ResourceLocation getTextureLocation(T p_114482_) {
        return Util.prefix("textures/entity/snail/" + model.type.name() + ".png");
    }
}
