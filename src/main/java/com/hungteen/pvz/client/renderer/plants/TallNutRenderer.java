package com.hungteen.pvz.client.renderer.plants;

import com.hungteen.pvz.client.model.plants.TallNutModel;
import com.hungteen.pvz.client.renderer.PVZLayerHandler;
import com.hungteen.pvz.common.entity.plants.TallNut;
import com.hungteen.pvz.util.Util;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class TallNutRenderer<T extends TallNut> extends MobRenderer<T, EntityModel<T>> {

    private static final ResourceLocation STATE0 = Util.prefix("textures/entity/plants/tall_nut/tall_nut.png");
    private static final ResourceLocation STATE1 = Util.prefix("textures/entity/plants/tall_nut/tall_nut_1.png");
    private static final ResourceLocation STATE2 = Util.prefix("textures/entity/plants/tall_nut/tall_nut_2.png");

    public TallNutRenderer(EntityRendererProvider.Context context) {
        super(context, new TallNutModel<>(context.bakeLayer(PVZLayerHandler.LayerLocationMap.get("tall_nut:main"))), 0.8F);
    }


    public void render(T tallNut, float p_115456_, float p_115457_, PoseStack poseStack, MultiBufferSource buffer, int p_115460_) {
        super.render(tallNut, p_115456_, p_115457_, poseStack, buffer, p_115460_);
    }

    @Override
    public ResourceLocation getTextureLocation(T tallNut) {
        float healthPercent = tallNut.getHealth()/tallNut.getMaxHealth();
        return healthPercent > 0.67 ? STATE0 : (healthPercent > 0.33 ? STATE1 : STATE2);
    }

}
