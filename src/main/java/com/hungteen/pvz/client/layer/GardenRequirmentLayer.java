package com.hungteen.pvz.client.layer;

import com.hungteen.pvz.api.interfaces.IGardenPlant;
import com.hungteen.pvz.common.network.ClientProxy;
import com.hungteen.pvz.common.register.PVZItems;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class GardenRequirmentLayer<T extends LivingEntity> extends RenderLayer<T, EntityModel<T>> {

    public GardenRequirmentLayer(RenderLayerParent<T, EntityModel<T>> p_117346_) {
        super(p_117346_);
    }

    @Override
    public void render(PoseStack stack, MultiBufferSource bufferSource, int p_117351_, T entity, float p_117353_, float p_117354_, float p_117355_, float p_117356_, float p_117357_, float p_117358_) {
        if (entity instanceof IGardenPlant plant && entity.isAlive()) {
            ItemStack itemStack = null;
            if (plant.isRequiringWater()) {
                itemStack = new ItemStack(PVZItems.WATERING_POT.get());
            } else if (plant.isRequiringFertilizer()) {
                itemStack = new ItemStack(PVZItems.FERTILIZER.get());
            }
            if (itemStack != null && ClientProxy.getPlayer() != null) {
                stack.pushPose();
                stack.mulPose(Vector3f.XP.rotationDegrees(180.0F));
                stack.translate(0, -1 + entity.getBbHeight(), 0);
                ClientProxy.MC.getItemRenderer().renderStatic(
                        entity, itemStack, ItemTransforms.TransformType.GROUND, false,
                        stack, bufferSource, entity.level, p_117351_, OverlayTexture.NO_OVERLAY,
                        entity.getId() + ItemTransforms.TransformType.GROUND.ordinal());
                stack.popPose();
            }
        }
    }
}
