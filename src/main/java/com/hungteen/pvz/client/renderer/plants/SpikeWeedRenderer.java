package com.hungteen.pvz.client.renderer.plants;

import com.hungteen.pvz.client.model.plants.SpikeWeedModel;
import com.hungteen.pvz.client.renderer.PVZLayerHandler;
import com.hungteen.pvz.common.entity.plants.SpikeWeed;
import com.hungteen.pvz.util.Util;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;


public class SpikeWeedRenderer<T extends SpikeWeed> extends MobRenderer<T, EntityModel<T>> {

    private static final ResourceLocation STATE0 = Util.prefix("textures/entity/plants/spike_weed/spike_weed.png");
    public SpikeWeedRenderer(EntityRendererProvider.Context context) {
        super(context, new SpikeWeedModel<>(context.bakeLayer(PVZLayerHandler.LayerLocationMap.get("spike_weed:main"))), 0F);
    }


    public void render(T spikeWeed, float p_115456_, float p_115457_, PoseStack poseStack, MultiBufferSource buffer, int p_115460_) {
        poseStack.pushPose();
        Vec3i vec = spikeWeed.getAttachFace().getNormal();
        poseStack.translate(- 0.5 * vec.getX(), 0.5 - 0.5 * vec.getY(), - 0.5 * vec.getZ());
        poseStack.mulPose(Vector3f.ZP.rotation((float) (Math.PI * (- vec.getX() + (vec.getY() != 0 ? (vec.getY() - 1) : 0)) / 2)));
        poseStack.mulPose(Vector3f.XP.rotation((float) (Math.PI * vec.getZ() / 2)));
        super.render(spikeWeed, p_115456_, p_115457_, poseStack, buffer, p_115460_);
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(T sunflower) {
        return STATE0;
    }

}
