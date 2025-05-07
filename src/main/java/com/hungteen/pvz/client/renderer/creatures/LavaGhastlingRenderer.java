package com.hungteen.pvz.client.renderer.creatures;

import com.hungteen.pvz.client.model.zombie.LavaGhastlingModel;
import com.hungteen.pvz.client.renderer.PVZLayerHandler;
import com.hungteen.pvz.common.entity.LavaGhastling;
import com.hungteen.pvz.util.Util;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;


public class LavaGhastlingRenderer<T extends LavaGhastling> extends MobRenderer<T, EntityModel<T>> {

    private static final ResourceLocation COMMON = Util.prefix("textures/entity/plants/flower_pot/flower_pot.png");

    public LavaGhastlingRenderer(EntityRendererProvider.Context context) {
        super(context, new LavaGhastlingModel<>(context.bakeLayer(PVZLayerHandler.LayerLocationMap.get("lava_ghastling:main"))), 0.4F);
    }


    public void render(T flowerPot, float p_115456_, float p_115457_, PoseStack poseStack, MultiBufferSource buffer, int p_115460_) {
        super.render(flowerPot, p_115456_, p_115457_, poseStack, buffer, p_115460_);
    }

    @Override
    public ResourceLocation getTextureLocation(T flowerPot) {
        return COMMON;
    }

}
