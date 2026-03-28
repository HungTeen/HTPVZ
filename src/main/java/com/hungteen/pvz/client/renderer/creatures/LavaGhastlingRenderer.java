package com.hungteen.pvz.client.renderer.creatures;

import com.hungteen.pvz.client.layer.VanillaLightLayer;
import com.hungteen.pvz.client.model.LavaGhastlingLightModel;
import com.hungteen.pvz.client.renderer.PVZLayerHandler;
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
import net.minecraft.world.entity.ai.attributes.Attributes;


public class LavaGhastlingRenderer<T extends LavaGhastling> extends MobRenderer<T, EntityModel<T>> {

    private static final ResourceLocation SHOOTING = Util.prefix("textures/entity/lava_ghastling/lava_ghastling_shooting.png");
    private static final ResourceLocation COMMON = Util.prefix("textures/entity/lava_ghastling/lava_ghastling.png");
    private static final ResourceLocation RIDEN_SHOOTING = Util.prefix("textures/entity/lava_ghastling/lava_ghastling_shooting_riden.png");
    private static final ResourceLocation RIDEN_COMMON = Util.prefix("textures/entity/lava_ghastling/lava_ghastling_riden.png");
    private static final ResourceLocation LIGHT = Util.prefix("textures/entity/lava_ghastling/lava_ghastling_light.png");

    public LavaGhastlingRenderer(EntityRendererProvider.Context context) {
        super(context, new GhastModel<>(context.bakeLayer(ModelLayers.GHAST)), 0.4F);
        this.addLayer(new VanillaLightLayer<>(this
                ,new LavaGhastlingLightModel<>(context.bakeLayer(PVZLayerHandler.LayerLocationMap.get("lava_ghastling_light:main")))
                , LIGHT, ghast -> ghast.getAttribute(Attributes.ARMOR).modifierById.containsKey(LavaGhastling.RIDEN_BY_BOSS_MODIFIER_UUID)));
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
