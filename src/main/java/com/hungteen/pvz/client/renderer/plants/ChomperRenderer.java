package com.hungteen.pvz.client.renderer.plants;

import com.hungteen.pvz.client.layer.LightLayer;
import com.hungteen.pvz.client.model.plants.ChomperModel;
import com.hungteen.pvz.client.renderer.PVZLayerHandler;
import com.hungteen.pvz.common.entity.plants.Chomper;
import com.hungteen.pvz.util.EntityUtil;
import com.hungteen.pvz.util.Util;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Pose;


public class ChomperRenderer<T extends Chomper> extends MobRenderer<T, EntityModel<T>> {

    private static final ResourceLocation TEXTURE = Util.prefix("textures/entity/plants/chomper/chomper.png");
    public ChomperRenderer(EntityRendererProvider.Context context) {
        super(context, new ChomperModel<>(context.bakeLayer(PVZLayerHandler.LayerLocationMap.get("chomper:main"))), 0.8F);
        this.addLayer(new LightLayer<>(this, Util.prefix("textures/entity/plants/chomper/chomper_sculk.png"),
                (chomper, partialTicks, ageInTicks) -> EntityUtil.isSculk(chomper) && ! chomper.hasSkill(Chomper.SUN_SKILL_NAME) ?
                        (float) Math.sin(ageInTicks / 20) * 0.2F + 0.4F : 0));
        this.addLayer(new LightLayer<>(this, Util.prefix("textures/entity/plants/chomper/chomper_light.png"),
                (chomper, partialTicks, ageInTicks) ->
                        chomper.hasSkill("skill.pvz.chomper.energy_transduction") && chomper.getPose() == Pose.CROAKING ?
                                Math.min((float) Math.min(chomper.animTick , Math.min(60 - chomper.animTick, 5)) / 3, 1) : 0F));
    }


    public void render(T chomper, float p_115456_, float p_115457_, PoseStack poseStack, MultiBufferSource buffer, int p_115460_) {
        super.render(chomper, p_115456_, p_115457_, poseStack, buffer, p_115460_);
    }

    @Override
    public ResourceLocation getTextureLocation(T chomper) {
        return TEXTURE;
    }

}
