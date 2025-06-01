package com.hungteen.pvz.client.renderer.plants;

import com.hungteen.pvz.client.model.plants.FlowerPotModel;
import com.hungteen.pvz.client.renderer.PVZLayerHandler;
import com.hungteen.pvz.common.entity.plants.FlowerPot;
import com.hungteen.pvz.util.Util;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;


public class FlowerPotRenderer<T extends FlowerPot> extends MobRenderer<T, EntityModel<T>> {

    private static final ResourceLocation COMMON = Util.prefix("textures/entity/plants/flower_pot/lava_ghastling.png");
    private static final ResourceLocation CHINAWARE = Util.prefix("textures/entity/plants/flower_pot/lava_ghastling_light.png");

    public FlowerPotRenderer(EntityRendererProvider.Context context) {
        super(context, new FlowerPotModel<>(context.bakeLayer(PVZLayerHandler.LayerLocationMap.get("flower_pot:main"))), 0.4F);
    }


    public void render(T flowerPot, float p_115456_, float p_115457_, PoseStack poseStack, MultiBufferSource buffer, int p_115460_) {
        super.render(flowerPot, p_115456_, p_115457_, poseStack, buffer, p_115460_);
    }

    @Override
    public ResourceLocation getTextureLocation(T flowerPot) {
        return flowerPot.hasSkill(flowerPot, "skill.pvz.flower_pot.chinaware") ? CHINAWARE : COMMON;
    }

}
