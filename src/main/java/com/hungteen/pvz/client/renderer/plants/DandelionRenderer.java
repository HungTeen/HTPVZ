package com.hungteen.pvz.client.renderer.plants;

import com.hungteen.pvz.client.model.plants.DandelionModel;
import com.hungteen.pvz.client.renderer.PVZLayerHandler;
import com.hungteen.pvz.common.entity.plants.Dandelion;
import com.hungteen.pvz.util.Util;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class DandelionRenderer<T extends Dandelion> extends MobRenderer<T, EntityModel<T>> {

    private static final ResourceLocation TEXTURE = Util.prefix("textures/entity/plants/dandelion/dandelion.png");
    private static final ResourceLocation CHINAWARE = Util.prefix("textures/entity/plants/flower_pot/flower_pot_chinaware.png");

    public DandelionRenderer(EntityRendererProvider.Context context) {
        super(context, new DandelionModel<>(context.bakeLayer(PVZLayerHandler.LayerLocationMap.get("dandelion:main"))), 0.5F);
    }


    public void render(T flowerPot, float p_115456_, float p_115457_, PoseStack poseStack, MultiBufferSource buffer, int p_115460_) {
        super.render(flowerPot, p_115456_, p_115457_, poseStack, buffer, p_115460_);
    }

    @Override
    public RenderType getRenderType(T entity, boolean p_115323_, boolean p_115324_, boolean p_115325_) {
        ResourceLocation resourcelocation = this.getTextureLocation(entity);
        if (p_115324_) {
            return RenderType.itemEntityTranslucentCull(resourcelocation);
        } else if (p_115323_) {
            return RenderType.entityTranslucent(resourcelocation);
        } else {
            return p_115325_ ? RenderType.outline(resourcelocation) : null;
        }
    }
    @Override
    public ResourceLocation getTextureLocation(T flowerPot) {
        return flowerPot.hasSkill(flowerPot, "skill.pvz.flower_pot.chinaware") ? CHINAWARE : TEXTURE;
    }

}