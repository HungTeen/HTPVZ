package com.hungteen.pvz.client.renderer.plants;

import com.hungteen.pvz.client.model.plants.PeaShooterModel;
import com.hungteen.pvz.client.renderer.PVZLayerHandler;
import com.hungteen.pvz.common.entity.plants.PeaShooter;
import com.hungteen.pvz.util.Util;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;


public class PeaShooterRenderer<T extends PeaShooter> extends MobRenderer<T, EntityModel<T>> {

    private static final ResourceLocation STATE0 = Util.prefix("textures/entity/plants/pea_shooter/pea_shooter.png");
    public PeaShooterRenderer(EntityRendererProvider.Context context) {
        super(context, new PeaShooterModel<>(context.bakeLayer(PVZLayerHandler.LayerLocationMap.get("pea_shooter:main"))), 0.6F);
    }


    public void render(T entity, float p_115456_, float p_115457_, PoseStack poseStack, MultiBufferSource buffer, int p_115460_) {
        super.render(entity, p_115456_, p_115457_, poseStack, buffer, p_115460_);
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return STATE0;
    }

}
