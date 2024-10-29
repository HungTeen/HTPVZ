package com.hungteen.pvz.client.renderer.plants;

import com.hungteen.pvz.client.layer.LightLayer;
import com.hungteen.pvz.client.model.plants.JalapenoModel;
import com.hungteen.pvz.client.renderer.PVZLayerHandler;
import com.hungteen.pvz.common.entity.plants.Jalapeno;
import com.hungteen.pvz.util.Util;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;


public class JalapenoRenderer<T extends Jalapeno> extends MobRenderer<T, EntityModel<T>> {

    private static final ResourceLocation TEXTURE = Util.prefix("textures/entity/plants/jalapeno/jalapeno.png");
    public JalapenoRenderer(EntityRendererProvider.Context context) {
        super(context, new JalapenoModel<>(context.bakeLayer(PVZLayerHandler.LayerLocationMap.get("jalapeno:main"))), 0.6F);
        this.addLayer(new LightLayer<>(this, Util.prefix("textures/entity/plants/jalapeno/jalapeno_light.png")));
    }


    public void render(T jalapeno, float p_115456_, float p_115457_, PoseStack poseStack, MultiBufferSource buffer, int p_115460_) {
        super.render(jalapeno, p_115456_, p_115457_, poseStack, buffer, p_115460_);
    }
    @Override
    protected boolean isShaking(T jalapeno) {
        return true;
    }
    @Override
    public ResourceLocation getTextureLocation(T jalapeno) {
        return TEXTURE;
    }

}
