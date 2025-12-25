package com.hungteen.pvz.client.renderer.zombie;

import com.hungteen.pvz.client.layer.GargantuarHeadLayer;
import com.hungteen.pvz.client.model.zombie.GargantuarModel;
import com.hungteen.pvz.client.renderer.PVZLayerHandler;
import com.hungteen.pvz.common.entity.zombies.Gargantuar;
import com.hungteen.pvz.util.Util;
import net.minecraft.client.model.ZombieModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;

public class GargantuarRenderer<T extends Gargantuar, M extends GargantuarModel<T>> extends MobRenderer<T, M> {
    private static final ResourceLocation TEXTURE = Util.prefix("textures/entity/zombie/gargantuar/gargantuar.png");
    public GargantuarRenderer(EntityRendererProvider.Context context) {
        this(context, (M) new GargantuarModel<>(context.bakeLayer(PVZLayerHandler.LayerLocationMap.get("gargantuar:main"))), 1.2F);
    }
    public GargantuarRenderer(EntityRendererProvider.Context context, M model, float shadowSize) {
        this(context, model, shadowSize, 1.0F, 1.0F, 1.0F);
    }
    public GargantuarRenderer(EntityRendererProvider.Context context, M model, float p_174175_, float p_174176_, float p_174177_, float p_174178_) {
        super(context, model, p_174175_);
        this.addLayer(new GargantuarHeadLayer<>(this, new ZombieModel<>(context.bakeLayer(ModelLayers.ZOMBIE_OUTER_ARMOR)),
                context.getModelSet(), context.getItemInHandRenderer()));
        this.addLayer((RenderLayer<T, M>) new ItemInHandLayer<>(
                (RenderLayerParent<Gargantuar, GargantuarModel<Gargantuar>>) this,
                context.getItemInHandRenderer()));
    }
    @Override
    public ResourceLocation getTextureLocation(Gargantuar gargantuar) {
        return TEXTURE;
    }
}
