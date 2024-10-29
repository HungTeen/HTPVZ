package com.hungteen.pvz.client.renderer.plants;

import com.hungteen.pvz.client.layer.LightLayer;
import com.hungteen.pvz.client.model.plants.HypnoShroomModel;
import com.hungteen.pvz.client.renderer.PVZLayerHandler;
import com.hungteen.pvz.common.entity.plants.HypnoShroom;
import com.hungteen.pvz.util.Util;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;


public class HypnoShroomRenderer<T extends HypnoShroom> extends MobRenderer<T, EntityModel<T>> {

    private static final ResourceLocation TEXTURE = Util.prefix("textures/entity/plants/hypno_shroom/hypno_shroom.png");
    public HypnoShroomRenderer(EntityRendererProvider.Context context) {
        super(context, new HypnoShroomModel<>(context.bakeLayer(PVZLayerHandler.LayerLocationMap.get("hypno_shroom:main"))), 0.4F);
        this.addLayer(new LightLayer<>(this, Util.prefix("textures/entity/plants/hypno_shroom/hypno_shroom_light_red.png"),
                (shroom, partialTicks, ageInTicks) -> (float) Math.sin(ageInTicks / 20) * 0.3F + (shroom.isSleeping() ? 0.2F : 0.7F)));
        this.addLayer(new LightLayer<>(this, Util.prefix("textures/entity/plants/hypno_shroom/hypno_shroom_light_blue.png"),
                (shroom, partialTicks, ageInTicks) -> (float) Math.cos(ageInTicks / 20) * 0.3F + (shroom.isSleeping() ? 0.2F : 0.7F)));
    }


    public void render(T hypnoShroom, float p_115456_, float p_115457_, PoseStack poseStack, MultiBufferSource buffer, int p_115460_) {
        super.render(hypnoShroom, p_115456_, p_115457_, poseStack, buffer, p_115460_);
    }

    @Override
    public ResourceLocation getTextureLocation(T chomper) {
        return TEXTURE;
    }

}
