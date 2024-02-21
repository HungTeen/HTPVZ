package com.hungteen.pvz.client.renderer.plants;

import com.hungteen.pvz.client.layer.WallNutArmorLayer;
import com.hungteen.pvz.client.layer.WallNutColorLayer;
import com.hungteen.pvz.client.model.plants.SnowPeaModel;
import com.hungteen.pvz.client.model.plants.WallNutModel;
import com.hungteen.pvz.client.renderer.PVZLayerHandler;
import com.hungteen.pvz.common.entity.plants.SnowPea;
import com.hungteen.pvz.common.entity.plants.WallNut;
import com.hungteen.pvz.util.Util;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import static com.hungteen.pvz.common.entity.plants.WallNut.EXPLODE_COUNT;


public class SnowPeaRenderer<T extends SnowPea> extends MobRenderer<T, EntityModel<T>> {

    private static final ResourceLocation TEXTURE = Util.prefix("textures/entity/plants/snow_pea/snow_pea.png");

    public SnowPeaRenderer(EntityRendererProvider.Context context) {
        super(context, new SnowPeaModel<>(context.bakeLayer(PVZLayerHandler.LayerLocationMap.get("snow_pea:main"))), 0.5F);
    }

    @Override
    protected boolean isShaking(T entity) {
        return entity.isConverting() || super.isShaking(entity);
    }

    @Override
    public ResourceLocation getTextureLocation(T wallNut) {
        return TEXTURE;
    }

}
