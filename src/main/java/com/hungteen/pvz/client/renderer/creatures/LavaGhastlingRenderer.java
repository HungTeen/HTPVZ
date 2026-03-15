package com.hungteen.pvz.client.renderer.creatures;

import com.hungteen.pvz.common.entity.LavaGhastling;
import com.hungteen.pvz.util.Util;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.GhastModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;


public class LavaGhastlingRenderer<T extends LavaGhastling> extends MobRenderer<T, EntityModel<T>> {

    private static final ResourceLocation SHOOTING = Util.prefix("textures/entity/lava_ghastling/lava_ghastling_shooting.png");
    private static final ResourceLocation COMMON = Util.prefix("textures/entity/lava_ghastling/lava_ghastling.png");
    private static final ResourceLocation RIDEN_SHOOTING = Util.prefix("textures/entity/lava_ghastling/lava_ghastling_shooting_riden.png");
    private static final ResourceLocation RIDEN_COMMON = Util.prefix("textures/entity/lava_ghastling/lava_ghastling_riden.png");

    public LavaGhastlingRenderer(EntityRendererProvider.Context context) {
        super(context, new GhastModel<>(context.bakeLayer(ModelLayers.GHAST)), 0.4F);
    }

    public void render(T ghastling, float p_115456_, float p_115457_, PoseStack poseStack, MultiBufferSource buffer, int p_115460_) {
        super.render(ghastling, p_115456_, p_115457_, poseStack, buffer, p_115460_);
    }

    @Override
    public ResourceLocation getTextureLocation(T ghastling) {
        boolean shooting = ghastling.isCharging();
        return ghastling.isVehicle() ? (shooting ? RIDEN_SHOOTING : RIDEN_COMMON) : (shooting ? SHOOTING : COMMON);
    }

}
