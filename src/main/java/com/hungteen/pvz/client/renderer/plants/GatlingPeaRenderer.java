package com.hungteen.pvz.client.renderer.plants;

import com.hungteen.pvz.client.layer.LightLayer;
import com.hungteen.pvz.client.model.plants.GatlingPeaModel;
import com.hungteen.pvz.client.model.plants.JalapenoModel;
import com.hungteen.pvz.client.renderer.PVZLayerHandler;
import com.hungteen.pvz.common.entity.plants.GatlingPea;
import com.hungteen.pvz.common.entity.plants.Jalapeno;
import com.hungteen.pvz.util.Util;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;


public class GatlingPeaRenderer<T extends GatlingPea> extends MobRenderer<T, EntityModel<T>> {

    private static final ResourceLocation TEXTURE = Util.prefix("textures/entity/plants/gatling_pea/gatling_pea.png");
    public GatlingPeaRenderer(EntityRendererProvider.Context context) {
        super(context, new GatlingPeaModel<>(context.bakeLayer(PVZLayerHandler.LayerLocationMap.get("gatling_pea:main"))), 0.6F);
        this.addLayer(new LightLayer<>(this, Util.prefix("textures/entity/plants/gatling_pea/gatling_pea_light.png"),
                (gatlingPea, partialTicks, ageInTicks) -> gatlingPea.getOverheat() < 100 ? 0 : (((float) gatlingPea.getOverheat() - 100) / GatlingPea.MAX_OVERHEAT)));
    }


    public void render(T jalapeno, float p_115456_, float p_115457_, PoseStack poseStack, MultiBufferSource buffer, int p_115460_) {
        super.render(jalapeno, p_115456_, p_115457_, poseStack, buffer, p_115460_);
    }
    @Override
    public ResourceLocation getTextureLocation(T jalapeno) {
        return TEXTURE;
    }

}
