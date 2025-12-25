package com.hungteen.pvz.client.renderer.creatures;

import com.hungteen.pvz.client.model.AngerModel;
import com.hungteen.pvz.common.entity.creatures.Anger;
import com.hungteen.pvz.util.Util;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public class AngerRenderer extends MobRenderer<Anger, AngerModel> {
    private static final ResourceLocation TEXTURE_LOCATION = Util.prefix("textures/entity/anger/anger.png");
    public AngerRenderer(EntityRendererProvider.Context p_234551_) {
        super(p_234551_, new AngerModel(p_234551_.bakeLayer(ModelLayers.ALLAY)), 0.4F);
        this.addLayer(new ItemInHandLayer<>(this, p_234551_.getItemInHandRenderer()));
    }

    protected int getBlockLightLevel(Anger p_234560_, BlockPos p_234561_) {
        return 15;
    }
    @Override
    public ResourceLocation getTextureLocation(Anger p_114482_) {
        return TEXTURE_LOCATION;
    }
}
