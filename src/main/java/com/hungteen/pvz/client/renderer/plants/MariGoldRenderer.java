package com.hungteen.pvz.client.renderer.plants;

import com.hungteen.pvz.client.layer.GardenRequirementLayer;
import com.hungteen.pvz.client.layer.MarigoldPetalsLayer;
import com.hungteen.pvz.client.model.plants.MariGoldModel;
import com.hungteen.pvz.client.renderer.PVZLayerHandler;
import com.hungteen.pvz.common.entity.plants.MariGold;
import com.hungteen.pvz.util.Util;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class MariGoldRenderer<T extends MariGold> extends MobRenderer<T, EntityModel<T>> {

    private static final ResourceLocation STATE0 = Util.prefix("textures/entity/plants/marigold/marigold.png");
    public MariGoldRenderer(EntityRendererProvider.Context context) {
        super(context, new MariGoldModel<>(context.bakeLayer(PVZLayerHandler.LayerLocationMap.get("marigold:main"))), 0.45F);
        this.addLayer(new MarigoldPetalsLayer(this));
        this.addLayer(new GardenRequirementLayer(this));
    }

    public void render(T marigold, float p_115456_, float p_115457_, PoseStack poseStack, MultiBufferSource buffer, int p_115460_) {
        int level = marigold.getGrowLevel();
        this.shadowRadius = (float) (0.45 * (level >= 3 ? 1 : Math.min(1, (float) ((level + 1) * 0.3 + 0.2))));
        super.render(marigold, p_115456_, p_115457_, poseStack, buffer, p_115460_);
    }

    @Override
    public ResourceLocation getTextureLocation(T marigold) {
        return STATE0;
    }

}
