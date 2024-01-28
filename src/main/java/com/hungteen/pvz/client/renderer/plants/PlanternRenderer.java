package com.hungteen.pvz.client.renderer.plants;

import com.hungteen.pvz.client.layer.fullskin.SunLightLayer;
import com.hungteen.pvz.client.model.plants.PlanternModel;
import com.hungteen.pvz.client.renderer.PVZLayerHandler;
import com.hungteen.pvz.common.entity.plants.Plantern;
import com.hungteen.pvz.util.Util;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;


public class PlanternRenderer<T extends Plantern> extends MobRenderer<T, EntityModel<T>> {

    private static final ResourceLocation STATE0 = Util.prefix("textures/entity/plants/plantern/plantern.png");
    public PlanternRenderer(EntityRendererProvider.Context context) {
        super(context, new PlanternModel<>(context.bakeLayer(PVZLayerHandler.LayerLocationMap.get("plantern:main"))), 0.6F);
        this.addLayer(new SunLightLayer<>(this, Util.prefix("textures/entity/plants/plantern/plantern_light.png")));
    }


    public void render(T plantern, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLightIn) {
        super.render(plantern, entityYaw, partialTicks, poseStack, buffer, packedLightIn);
    }

    @Override
    public ResourceLocation getTextureLocation(T sunflower) {
        return STATE0;
    }

    private static void vertex(VertexConsumer p_114090_, Matrix4f p_114091_, Matrix3f p_114092_, int p_114093_, float p_114094_, int p_114095_, int p_114096_, int p_114097_) {
        p_114090_.vertex(p_114091_, p_114094_ - 0.5F, (float)p_114095_ - 0.25F, 0.0F).color(255, 255, 255, 255).uv((float)p_114096_, (float)p_114097_).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(p_114093_).normal(p_114092_, 0.0F, 1.0F, 0.0F).endVertex();
    }
}
