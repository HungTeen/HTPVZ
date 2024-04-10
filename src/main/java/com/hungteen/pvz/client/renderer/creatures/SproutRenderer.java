package com.hungteen.pvz.client.renderer.creatures;

import com.hungteen.pvz.client.layer.GardenRequirmentLayer;
import com.hungteen.pvz.client.model.SproutModel;
import com.hungteen.pvz.client.renderer.PVZLayerHandler;
import com.hungteen.pvz.common.entity.Sprout;
import com.hungteen.pvz.common.network.ClientProxy;
import com.hungteen.pvz.util.Util;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class SproutRenderer extends MobRenderer<Sprout, SproutModel<Sprout>> {
    private static final ResourceLocation MARIGOLD_LOCATION = Util.prefix("textures/entity/sprout/marigold_sprout.png");
    private static final ResourceLocation COMMON_LOCATION = Util.prefix("textures/entity/sprout/sprout.png");
    public SproutRenderer(EntityRendererProvider.Context p_234551_) {
        super(p_234551_, new SproutModel<>(p_234551_.bakeLayer(PVZLayerHandler.LayerLocationMap.get("sprout:main"))), 0.2F);
        this.addLayer(new GardenRequirmentLayer(this));
    }

    @Override
    public void render(Sprout sprout, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLightIn) {
        if (sprout.plant != null) {
            float scale = (float) sprout.getGrowLevel() > 1 ? 0.8F : 0.5F;
            poseStack.pushPose();
            poseStack.scale(scale, scale, scale);
            poseStack.mulPose(Vector3f.YP.rotationDegrees(sprout.yBodyRot));
            EntityRenderer<? super Entity> renderer = ClientProxy.MC.getEntityRenderDispatcher().getRenderer(sprout.plant);
            renderer.render(sprout.plant, entityYaw, partialTicks, poseStack, buffer, packedLightIn);
            poseStack.popPose();
        }
        super.render(sprout, entityYaw, partialTicks, poseStack, buffer, packedLightIn);
    }
    @Override
    public ResourceLocation getTextureLocation(Sprout sprout) {
        return sprout.isMarigold() ? MARIGOLD_LOCATION : COMMON_LOCATION;
    }
}
