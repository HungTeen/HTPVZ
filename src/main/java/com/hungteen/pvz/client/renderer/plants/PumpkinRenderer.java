package com.hungteen.pvz.client.renderer.plants;

import com.hungteen.pvz.client.model.plants.PumpkinModel;
import com.hungteen.pvz.client.renderer.PVZLayerHandler;
import com.hungteen.pvz.common.entity.plants.Pumpkin;
import com.hungteen.pvz.util.Util;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;


public class PumpkinRenderer<T extends Pumpkin> extends MobRenderer<T, EntityModel<T>> {

    private static final ResourceLocation STATE0 = Util.prefix("textures/entity/plants/pumpkin/pumpkin_0.png");
    private static final ResourceLocation STATE1 = Util.prefix("textures/entity/plants/pumpkin/pumpkin_1.png");
    private static final ResourceLocation STATE2 = Util.prefix("textures/entity/plants/pumpkin/pumpkin_2.png");

    public PumpkinRenderer(EntityRendererProvider.Context context) {
        super(context, new PumpkinModel<>(context.bakeLayer(PVZLayerHandler.LayerLocationMap.get("pumpkin:main"))), 0.6F);
    }


    public void render(T pumpkin, float p_115456_, float p_115457_, PoseStack poseStack, MultiBufferSource buffer, int p_115460_) {
        super.render(pumpkin, p_115456_, p_115457_, poseStack, buffer, p_115460_);
    }

    @Override
    public ResourceLocation getTextureLocation(T pumpkin) {
        float healthPercent = pumpkin.getHealth()/pumpkin.getMaxHealth();
        return healthPercent > 0.67 ? STATE0 : (healthPercent > 0.33 ? STATE1 : STATE2);
    }

}
