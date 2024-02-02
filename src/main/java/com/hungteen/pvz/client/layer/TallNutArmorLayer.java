package com.hungteen.pvz.client.layer;

import com.hungteen.pvz.client.model.plants.TallNutArmorModel;
import com.hungteen.pvz.client.renderer.PVZLayerHandler;
import com.hungteen.pvz.common.entity.plants.TallNut;
import com.hungteen.pvz.util.Util;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class TallNutArmorLayer<T extends TallNut> extends RenderLayer<T, TallNutArmorModel<T>> {

    private static final ResourceLocation STATE0 = Util.prefix("textures/entity/plants/tall_nut/tall_nut_armor_0.png");
    private static final ResourceLocation STATE1 = Util.prefix("textures/entity/plants/tall_nut/tall_nut_armor_1.png");
    private static final ResourceLocation STATE2 = Util.prefix("textures/entity/plants/tall_nut/tall_nut_armor_2.png");
    private final TallNutArmorModel<T> model;
    public TallNutArmorLayer(RenderLayerParent<T, TallNutArmorModel<T>> p_117346_, EntityModelSet modelSet) {
        super(p_117346_);
        this.model = new TallNutArmorModel<>(modelSet.bakeLayer(PVZLayerHandler.LayerLocationMap.get("tall_nut:armor")));
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int p_117351_, T tallNut, float p_117353_, float p_117354_, float p_117355_, float p_117356_, float p_117357_, float p_117358_) {
        if (tallNut.hasIronArmor()) {
            float armorHealth = tallNut.getIronArmor() / tallNut.getMaxIronArmor();
            VertexConsumer vertexconsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(
                    armorHealth > 0.67 ? STATE0 : (armorHealth > 0.33 ? STATE1 : STATE2)));
            this.model.renderToBuffer(poseStack, vertexconsumer, p_117351_, OverlayTexture.NO_OVERLAY, 1F, 1F, 1F, 1.0F);
        }
    }


}
