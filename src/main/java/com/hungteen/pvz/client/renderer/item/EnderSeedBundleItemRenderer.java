package com.hungteen.pvz.client.renderer.item;

import com.hungteen.pvz.client.PVZKeyBindings;
import com.hungteen.pvz.client.gui.PVZOverlayHandler;
import com.hungteen.pvz.common.capability.player.PVZPlayerCapability;
import com.hungteen.pvz.common.item.EnderSeedBundleItem;
import com.hungteen.pvz.common.item.SeedPacketItem;
import com.hungteen.pvz.common.network.ClientProxy;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Vector3f;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.IItemDecorator;

public class EnderSeedBundleItemRenderer extends BlockEntityWithoutLevelRenderer {
    public static EnderSeedBundleItemRenderer INASTANCE = new EnderSeedBundleItemRenderer(ClientProxy.MC.getBlockEntityRenderDispatcher(), ClientProxy.MC.getEntityModels());

    public EnderSeedBundleItemRenderer(BlockEntityRenderDispatcher p_172550_, EntityModelSet p_172551_) {
        super(p_172550_, p_172551_);
    }

    public static ItemStack getChosenBundleItem(ItemStack itemStack) {
        if (ClientProxy.getPlayer() == null) {
            return ItemStack.EMPTY;
        } else {
            boolean shouldRender = EnderSeedBundleItem.isInInventory(ClientProxy.getPlayer(), itemStack);
            ItemStack itemStack1 = ItemStack.EMPTY;
            if (shouldRender) {
                if (itemStack.getItem() instanceof EnderSeedBundleItem item) {
                    itemStack1 =  PVZPlayerCapability.getEnderSeedBundleSlot(ClientProxy.getPlayer(), item.getPointer(itemStack));
                } else itemStack1 =  ItemStack.EMPTY;
            }
            if (itemStack1.getItem() instanceof EnderSeedBundleItem) return ItemStack.EMPTY;
            return itemStack1;
        }
    }

    @Override
    public void renderByItem(ItemStack itemStack, ItemTransforms.TransformType transformType, PoseStack poseStack
            , MultiBufferSource multiBufferSource, int p_108834_, int p_108835_) {
        ItemRenderer renderer = ClientProxy.MC.getItemRenderer();
        poseStack.pushPose();
        BakedModel filledModel = renderer.getItemModelShaper().getModelManager()
                .getModel(new ModelResourceLocation("pvz:ender_seed_bundle_filled#inventory"));
        BakedModel emptyModel = renderer.getItemModelShaper().getModelManager()
                .getModel(new ModelResourceLocation("pvz:ender_seed_bundle_closed#inventory"));
        boolean opened = ClientProxy.MC.screen instanceof AbstractContainerScreen<?> screen
                ? screen.getMenu().getCarried().getItem() instanceof SeedPacketItem<?> || screen.getMenu().getCarried() == itemStack
                : PVZKeyBindings.keyEnderSeedBundle.isDown();
        emptyModel = opened ? renderer.getItemModelShaper().getModelManager()
                .getModel(new ModelResourceLocation("pvz:ender_seed_bundle_opened#inventory")) : emptyModel;
        if (transformType == ItemTransforms.TransformType.GUI) {
            ItemStack itemStack1 = getChosenBundleItem(itemStack);
            if (! itemStack1.isEmpty()) renderGuiItem(itemStack1, renderer.getItemModelShaper().getItemModel(itemStack1), poseStack, multiBufferSource, renderer);
            renderGuiItem(itemStack, itemStack1.isEmpty() ? emptyModel : filledModel, poseStack, multiBufferSource, renderer);
        } else if (transformType == ItemTransforms.TransformType.FIRST_PERSON_LEFT_HAND
                || transformType == ItemTransforms.TransformType.FIRST_PERSON_RIGHT_HAND) {
            boolean leftHand = transformType == ItemTransforms.TransformType.FIRST_PERSON_LEFT_HAND;
            ItemStack itemStack1 = getChosenBundleItem(itemStack);
            if (itemStack1.getItem() instanceof EnderSeedBundleItem) itemStack1 = ItemStack.EMPTY;
            if (itemStack1.isEmpty()) {
                if (leftHand) {
                    poseStack.translate(-0.35, 0.2, 0.8);
                    poseStack.mulPose(Vector3f.YP.rotationDegrees(90));
                    poseStack.mulPose(Vector3f.ZP.rotationDegrees(-20));
                } else {
                    poseStack.translate(1.25, -0.1, 0);
                    poseStack.mulPose(Vector3f.YP.rotationDegrees(-90));
                    poseStack.mulPose(Vector3f.ZP.rotationDegrees(20));
                }
                VertexConsumer vertexconsumer = ItemRenderer.getFoilBufferDirect(multiBufferSource
                        , Sheets.translucentCullBlockSheet(), true, itemStack.hasFoil());
                renderer.renderModelLists(emptyModel, itemStack, p_108834_, p_108835_, poseStack, vertexconsumer);
            } else {
                poseStack.translate(0.5, 0.5, 0.5);
                if (transformType == ItemTransforms.TransformType.FIRST_PERSON_LEFT_HAND) {
                    poseStack.mulPose(Vector3f.YP.rotationDegrees(180));
                    poseStack.mulPose(Vector3f.XP.rotationDegrees(50));
                    poseStack.translate(0, -0.13, -0.27);
                }
                renderer.render(itemStack1, transformType, false, poseStack, multiBufferSource, p_108834_, p_108835_
                        , renderer.getItemModelShaper().getItemModel(itemStack1));
            }
        } else {
            if (transformType == ItemTransforms.TransformType.THIRD_PERSON_RIGHT_HAND
            || transformType == ItemTransforms.TransformType.THIRD_PERSON_LEFT_HAND) {
                poseStack.translate(0.19, 0.45, 0.287);
                poseStack.scale(0.55f, 0.55f, 0.55f);
            } else if (transformType == ItemTransforms.TransformType.GROUND) {
                poseStack.translate(0.25F, 0.25F, 0.25F);
                poseStack.scale(0.5F, 0.5F, 0.5F);
            } else if (transformType == ItemTransforms.TransformType.HEAD) {
                poseStack.translate(0, 0.5F, 0);
            }
            VertexConsumer vertexconsumer = ItemRenderer.getFoilBufferDirect(multiBufferSource
                    , Sheets.translucentCullBlockSheet(), true, itemStack.hasFoil());
            renderer.renderModelLists(emptyModel, itemStack, p_108834_, p_108835_, poseStack, vertexconsumer);
        }
        poseStack.popPose();

    }

    protected void renderGuiItem(ItemStack itemStack, BakedModel bakedModel
            , PoseStack poseStack, MultiBufferSource multiBufferSource, ItemRenderer renderer) {
        if (! (multiBufferSource instanceof MultiBufferSource.BufferSource)) return;
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.applyModelViewMatrix();
        Lighting.setupForFlatItems();
        VertexConsumer vertexconsumer = ItemRenderer.getFoilBufferDirect(multiBufferSource, Sheets.translucentCullBlockSheet(), true, itemStack.hasFoil());
        renderer.renderModelLists(bakedModel, itemStack, 15728880, OverlayTexture.NO_OVERLAY, poseStack, vertexconsumer);
        ((MultiBufferSource.BufferSource) multiBufferSource).endBatch();
        RenderSystem.enableDepthTest();
        Lighting.setupFor3DItems();
        RenderSystem.applyModelViewMatrix();
    }

    public static class EnderSeedBundleItemDecorator implements IItemDecorator {
        @Override
        public boolean render(Font font, ItemStack itemStack, int xOffset, int yOffset, float blitOffset) {
            ItemRenderer renderer = ClientProxy.MC.getItemRenderer();
            if (itemStack.getItem() instanceof EnderSeedBundleItem) {
                ItemStack itemStack1 = getChosenBundleItem(itemStack);
                if (itemStack1.getItem() instanceof EnderSeedBundleItem) itemStack1 = ItemStack.EMPTY;
                if (! itemStack1.isEmpty()) {
                    renderer.renderGuiItemDecorations(font, itemStack1, xOffset, yOffset);
                    Player player = ClientProxy.getPlayer();
                    if (player != null && (player.getItemBySlot(EquipmentSlot.MAINHAND) == itemStack || player.getItemBySlot(EquipmentSlot.OFFHAND) == itemStack)) {
                        PVZOverlayHandler.itemsToDrawCost.put(itemStack1.copy(), Pair.of(xOffset, yOffset));
                    }
                }
            }
            return false;
        }
    }
}
