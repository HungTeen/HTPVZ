package com.hungteen.pvz.client.renderer.plants;

import com.hungteen.pvz.client.layer.LightLayer;
import com.hungteen.pvz.client.model.plants.PlanternModel;
import com.hungteen.pvz.client.renderer.PVZLayerHandler;
import com.hungteen.pvz.common.entity.plants.Plantern;
import com.hungteen.pvz.util.Util;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class PlanternRenderer<T extends Plantern> extends MobRenderer<T, EntityModel<T>> {

    private static final ResourceLocation STATE0 = Util.prefix("textures/entity/plants/plantern/plantern.png");
    public PlanternRenderer(EntityRendererProvider.Context context) {
        super(context, new PlanternModel<>(context.bakeLayer(PVZLayerHandler.LayerLocationMap.get("plantern:main"))), 0.6F);
        this.addLayer(new LightLayer<>(this, Util.prefix("textures/entity/plants/plantern/plantern_light.png")));
    }


    public void render(T plantern, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLightIn) {
        super.render(plantern, entityYaw, partialTicks, poseStack, buffer, packedLightIn);
    }

    @Override
    public ResourceLocation getTextureLocation(T sunflower) {
        return STATE0;
    }
}
