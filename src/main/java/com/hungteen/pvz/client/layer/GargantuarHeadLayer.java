package com.hungteen.pvz.client.layer;

import com.hungteen.pvz.client.model.zombie.GargantuarModel;
import com.hungteen.pvz.common.entity.zombies.Gargantuar;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;

import javax.annotation.Nullable;
import java.util.Map;

public class GargantuarHeadLayer<T extends Gargantuar, M extends GargantuarModel<T>, A extends HumanoidModel> extends RenderLayer<T, M> {

    A armorModel;
    private final Map<SkullBlock.Type, SkullModelBase> skullModels;
    private final ItemInHandRenderer itemInHandRenderer;
    public GargantuarHeadLayer(RenderLayerParent<T, M> renderer, A armorModel, EntityModelSet modelSet, ItemInHandRenderer itemInHandRenderer) {
        super(renderer);
        this.armorModel = armorModel;
        armorModel.setAllVisible(false);
        armorModel.head.visible = true;
        armorModel.hat.visible = true;
        skullModels = SkullBlockRenderer.createSkullRenderers(modelSet);
        this.itemInHandRenderer = itemInHandRenderer;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int p_117351_, T gargantuar, float p_117353_, float p_117354_, float p_117355_, float p_117356_, float p_117357_, float p_117358_) {
        ItemStack itemStack = gargantuar.getItemBySlot(EquipmentSlot.HEAD);
        poseStack.pushPose();
        //xiexieni, majiang.
        this.getParentModel().gargantuar.translateAndRotate(poseStack);
        this.getParentModel().total.translateAndRotate(poseStack);
        this.getParentModel().body.translateAndRotate(poseStack);
        this.getParentModel().head.translateAndRotate(poseStack);
        poseStack.scale(1.25f, 1.25f, 1.25f);
        poseStack.translate(0, -0.8, -0.15625);
        if (itemStack.getItem() instanceof ArmorItem item) {
            if (item.getSlot() == EquipmentSlot.HEAD) {
                Model model = ForgeHooksClient.getArmorModel(gargantuar, itemStack, EquipmentSlot.HEAD, armorModel);
                boolean flag1 = itemStack.hasFoil();
                if (item instanceof DyeableLeatherItem item1) {
                    int i = item1.getColor(itemStack);
                    float f = (float)(i >> 16 & 255) / 255.0F;
                    float f1 = (float)(i >> 8 & 255) / 255.0F;
                    float f2 = (float)(i & 255) / 255.0F;
                    this.renderModel(poseStack, buffer, p_117351_, flag1, model, f, f1, f2, this.getArmorResource(gargantuar, itemStack, EquipmentSlot.HEAD, null));
                    this.renderModel(poseStack, buffer, p_117351_, flag1, model, 1.0F, 1.0F, 1.0F, this.getArmorResource(gargantuar, itemStack, EquipmentSlot.HEAD, "overlay"));
                } else {
                    this.renderModel(poseStack, buffer, p_117351_, flag1, model, 1.0F, 1.0F, 1.0F, this.getArmorResource(gargantuar, itemStack, EquipmentSlot.HEAD, null));
                }
            }
        } else if (! itemStack.isEmpty()) {
            Item item = itemStack.getItem();
            poseStack.pushPose();
            poseStack.scale(0.5F, -0.5F, -0.5F);
            poseStack.translate(0, -1.2F, 0);
            if (item instanceof BlockItem && ((BlockItem)item).getBlock() instanceof AbstractSkullBlock) {
                poseStack.scale(1.85F, 1.85F, 1.85F);
                poseStack.translate(0, -0.225F, 0);

                GameProfile gameprofile = null;
                if (itemStack.hasTag()) {
                    CompoundTag compoundtag = itemStack.getTag();
                    if (compoundtag.contains("SkullOwner", 10)) {
                        gameprofile = NbtUtils.readGameProfile(compoundtag.getCompound("SkullOwner"));
                    }
                }

                poseStack.translate(-0.5D, 0.0D, -0.5D);
                SkullBlock.Type skullblock$type = ((AbstractSkullBlock)((BlockItem)item).getBlock()).getType();
                SkullModelBase skullmodelbase = this.skullModels.get(skullblock$type);
                RenderType rendertype = SkullBlockRenderer.getRenderType(skullblock$type, gameprofile);
                SkullBlockRenderer.renderSkull(null, 180.0F, p_117353_, poseStack, buffer, p_117351_, skullmodelbase, rendertype);
            } else {
                poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0F));
                this.itemInHandRenderer.renderItem(gargantuar, itemStack, ItemTransforms.TransformType.HEAD, false, poseStack, buffer, p_117351_);
            }

            poseStack.popPose();
        }
        poseStack.popPose();
    }

    private void renderModel(PoseStack p_117107_, MultiBufferSource p_117108_, int p_117109_, boolean p_117111_, net.minecraft.client.model.Model p_117112_, float p_117114_, float p_117115_, float p_117116_, ResourceLocation armorResource) {
        VertexConsumer vertexconsumer = ItemRenderer.getArmorFoilBuffer(p_117108_, RenderType.armorCutoutNoCull(armorResource), false, p_117111_);
        p_117112_.renderToBuffer(p_117107_, vertexconsumer, p_117109_, OverlayTexture.NO_OVERLAY, p_117114_, p_117115_, p_117116_, 1.0F);
    }

    public ResourceLocation getArmorResource(net.minecraft.world.entity.Entity entity, ItemStack stack, EquipmentSlot slot, @Nullable String type) {
        ArmorItem item = (ArmorItem)stack.getItem();
        String texture = item.getMaterial().getName();
        String domain = "minecraft";
        int idx = texture.indexOf(':');
        if (idx != -1) {
            domain = texture.substring(0, idx);
            texture = texture.substring(idx + 1);
        }
        String s1 = String.format(java.util.Locale.ROOT, "%s:textures/models/armor/%s_layer_%d%s.png", domain, texture, 1, type == null ? "" : String.format(java.util.Locale.ROOT, "_%s", type));

        s1 = ForgeHooksClient.getArmorTexture(entity, stack, s1, slot, type);
        ResourceLocation resourcelocation = HumanoidArmorLayer.ARMOR_LOCATION_CACHE.get(s1);

        if (resourcelocation == null) {
            resourcelocation = new ResourceLocation(s1);
            HumanoidArmorLayer.ARMOR_LOCATION_CACHE.put(s1, resourcelocation);
        }

        return resourcelocation;
    }
}
