package com.hungteen.pvz.client.renderer.plants;

import com.hungteen.pvz.client.layer.LightLayer;
import com.hungteen.pvz.client.model.plants.GatlingPeaModel;
import com.hungteen.pvz.client.renderer.PVZLayerHandler;
import com.hungteen.pvz.common.entity.plants.GatlingPea;
import com.hungteen.pvz.common.entity.plants.PeaShooter;
import com.hungteen.pvz.util.Util;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;


public class GatlingPeaRenderer<T extends GatlingPea> extends MobRenderer<T, EntityModel<T>> {

    private static final ResourceLocation TEXTURE = Util.prefix("textures/entity/plants/gatling_pea/gatling_pea.png");
    private static final ResourceLocation FIRE_TEXTURE = Util.prefix("textures/entity/plants/gatling_pea/fire_gatling_pea.png");
    public GatlingPeaRenderer(EntityRendererProvider.Context context) {
        super(context, new GatlingPeaModel<>(context.bakeLayer(PVZLayerHandler.LayerLocationMap.get("gatling_pea:main"))), 0.6F);
        this.addLayer(new LightLayer<>(this, this::getBarrelLightColor, Util.prefix("textures/entity/plants/gatling_pea/gatling_pea_light.png")));
    }

    private Vec3 getBarrelLightColor(T gatlingPea, float partialTicks, float ageInTicks) {
        float light = Math.max(0, ((float) gatlingPea.getOverheat() - 100) / (GatlingPea.MAX_OVERHEAT - 100));
        if (gatlingPea.hasSkill(PeaShooter.FIRE_SKILL_NAME)) {
            light = Math.min(light * 1.4F, 1);
            return new Vec3(0.7 - Math.max(0, light - 0.7) * 0.5
                    , (1 - light) * 0.2 + light * 0.8
                    , Math.max(0, light - 0.7) * 1.66 + light * 0.5);
        } else {
            if (gatlingPea.getOverheat() < 100) return Vec3.ZERO;
            return new Vec3(light * 0.7, light * 0.2, 0);
        }
    }

    public void render(T gatlingPea, float p_115456_, float p_115457_, PoseStack poseStack, MultiBufferSource buffer, int p_115460_) {
        super.render(gatlingPea, p_115456_, p_115457_, poseStack, buffer, p_115460_);
    }
    @Override
    public ResourceLocation getTextureLocation(T gatlingPea) {
        return gatlingPea.hasSkill(PeaShooter.FIRE_SKILL_NAME) ? FIRE_TEXTURE : TEXTURE;
    }

}
