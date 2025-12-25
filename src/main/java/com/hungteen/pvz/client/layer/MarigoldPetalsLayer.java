package com.hungteen.pvz.client.layer;

import com.hungteen.pvz.client.model.plants.MariGoldModel;
import com.hungteen.pvz.client.renderer.PVZLayerHandler;
import com.hungteen.pvz.common.entity.plants.MariGold;
import com.hungteen.pvz.util.Util;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;

public class MarigoldPetalsLayer<T extends MariGold> extends RenderLayer<T, MariGoldModel<T>> {

    private static final ResourceLocation PETALS = Util.prefix("textures/entity/plants/marigold/marigold_petals.png");
    RenderLayerParent<T, MariGoldModel<T>> renderer;

    public MarigoldPetalsLayer(RenderLayerParent<T, MariGoldModel<T>> layerParent) {
        super(layerParent);
        this.renderer = layerParent;
    }

    @Override
    public void render(PoseStack stack, MultiBufferSource bufferSource, int p_117351_, T marigold, float p_117353_, float p_117354_, float p_117355_, float p_117356_, float p_117357_, float p_117358_) {
        if (! marigold.isInvisible()) {
            float R, G, B;
            int color = marigold.getEntityData().get(MariGold.COLOR);
            R = (float) ((float) (color >> 16 & 255) * 0.6 + 0xff * 0.4) / 255;
            G = (float) ((float) (color >> 8 & 255) * 0.6 + 0xff * 0.4) / 255;
            B = (float) ((float) (color & 255) * 0.6 + 0xff * 0.4) / 255;
            coloredCutoutModelCopyLayerRender(this.getParentModel(), renderer.getModel(), PETALS,
                    stack, bufferSource, p_117351_, marigold, p_117353_, p_117354_, marigold.tickCount + p_117355_, p_117356_, p_117357_, p_117358_,
                    R, G, B);
        }
    }
}
