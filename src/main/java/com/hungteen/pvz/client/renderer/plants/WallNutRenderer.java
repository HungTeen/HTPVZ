package com.hungteen.pvz.client.renderer.plants;

import com.hungteen.pvz.client.layer.WallNutArmorLayer;
import com.hungteen.pvz.client.layer.WallNutColorLayer;
import com.hungteen.pvz.client.model.plants.WallNutModel;
import com.hungteen.pvz.client.renderer.PVZLayerHandler;
import com.hungteen.pvz.common.entity.plants.WallNut;
import com.hungteen.pvz.util.Util;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import static com.hungteen.pvz.common.entity.plants.WallNut.EXPLODE_COUNT;


public class WallNutRenderer<T extends WallNut> extends MobRenderer<T, EntityModel<T>> {

    private static final ResourceLocation STATE0 = Util.prefix("textures/entity/plants/wall_nut/wall_nut_0.png");
    private static final ResourceLocation STATE1 = Util.prefix("textures/entity/plants/wall_nut/wall_nut_1.png");
    private static final ResourceLocation STATE2 = Util.prefix("textures/entity/plants/wall_nut/wall_nut_2.png");

    public WallNutRenderer(EntityRendererProvider.Context context) {
        super(context, new WallNutModel<>(context.bakeLayer(PVZLayerHandler.LayerLocationMap.get("wall_nut:main"))), 0.6F);
        this.addLayer(new WallNutColorLayer(this, context.getModelSet()));
        this.addLayer(new WallNutArmorLayer(this, context.getModelSet()));
    }


    public void render(T wallNut, float p_115456_, float p_115457_, PoseStack poseStack, MultiBufferSource buffer, int p_115460_) {
        super.render(wallNut, p_115456_, p_115457_, poseStack, buffer, p_115460_);
    }

    @Override
    protected void scale(T wallNut, PoseStack p_114047_, float p_114048_) {
        float f = wallNut.hasSkill(this, "skill.pvz.wall_nut.explode") ? wallNut.getEntityData().get(EXPLODE_COUNT) < 20 ? 0 :
                (float) wallNut.getEntityData().get(EXPLODE_COUNT) / 20 - 1 : 0;
        float f1 = 1.0F + Mth.sin(f * 100.0F) * f * 0.01F;
        f = Mth.clamp(f, 0.0F, 1.0F);
        f *= f;
        f *= f;
        float f2 = (1.0F + f * 0.4F) * f1;
        p_114047_.scale(f2, f2, f2);
    }

    @Override
    public ResourceLocation getTextureLocation(T wallNut) {
        float healthPercent = wallNut.getHealth()/wallNut.getMaxHealth();
        return healthPercent > 0.67 ? STATE0 : (healthPercent > 0.33 ? STATE1 : STATE2);
    }

}
