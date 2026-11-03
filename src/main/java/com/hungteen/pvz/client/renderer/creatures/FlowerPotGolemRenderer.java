package com.hungteen.pvz.client.renderer.creatures;

import com.hungteen.pvz.client.model.FlowerPotGolemModel;
import com.hungteen.pvz.client.renderer.PVZLayerHandler;
import com.hungteen.pvz.common.entity.creatures.FlowerPotGolem;
import com.hungteen.pvz.util.Util;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * @author PangTeen
 * @program HTPVZ
 * @create 2026/10/26 23:00
 **/
public class FlowerPotGolemRenderer extends MobRenderer<FlowerPotGolem, FlowerPotGolemModel<FlowerPotGolem>> {

    private static final ResourceLocation TEXTURE =
            Util.prefix("textures/entity/flower_pot_golem/flower_pot_golem.png");
    private static final ResourceLocation ACTIVATED_TEXTURE =
        Util.prefix("textures/entity/flower_pot_golem/flower_pot_golem_activated.png");
    private static final ResourceLocation CHARGED_TEXTURE =
        Util.prefix("textures/entity/flower_pot_golem/flower_pot_golem_charged.png");

    public FlowerPotGolemRenderer(EntityRendererProvider.Context context) {
        super(context, new FlowerPotGolemModel<>(
                context.bakeLayer(PVZLayerHandler.LayerLocationMap.get("flower_pot_golem:main"))), 0.4F);
    }

    @Override
    public void render(FlowerPotGolem p_115455_, float p_115456_, float p_115457_, PoseStack p_115458_,
        MultiBufferSource p_115459_, int p_115460_) {
        super.render(p_115455_, p_115456_, p_115457_, p_115458_, p_115459_, p_115460_);
        // TODO PT: 需要渲染一个植物卡，但不是手持
    }

    @Override
    public ResourceLocation getTextureLocation(FlowerPotGolem golem) {
        return TEXTURE;
    }
}
