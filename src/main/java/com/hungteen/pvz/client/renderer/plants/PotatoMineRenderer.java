package com.hungteen.pvz.client.renderer.plants;

import com.hungteen.pvz.client.layer.DirtLayer;
import com.hungteen.pvz.client.layer.LightLayer;
import com.hungteen.pvz.client.model.plants.PotatoMineModel;
import com.hungteen.pvz.client.renderer.PVZLayerHandler;
import com.hungteen.pvz.common.entity.plants.PotatoMine;
import com.hungteen.pvz.common.network.ClientProxy;
import com.hungteen.pvz.util.EntityUtil;
import com.hungteen.pvz.util.Util;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import static com.hungteen.pvz.common.entity.plants.PotatoMine.EXPLODE_COUNT;

public class PotatoMineRenderer<T extends PotatoMine> extends MobRenderer<T, EntityModel<T>> {

    private static final ResourceLocation COMMON = Util.prefix("textures/entity/plants/potato_mine/potato_mine.png");
    private static final ResourceLocation POISON = Util.prefix("textures/entity/plants/potato_mine/poisonous_potato_mine.png");
    public PotatoMineRenderer(EntityRendererProvider.Context context) {
        super(context, new PotatoMineModel<>(context.bakeLayer(PVZLayerHandler.LayerLocationMap.get("potato_mine:main"))), 0.2F);
        this.addLayer(new LightLayer<>(this, Util.prefix("textures/entity/plants/potato_mine/potato_mine_light.png"),
                (potatoMine, partialTicks, ageInTicks) -> (
                        potatoMine.getEntityData().get(PotatoMine.PREPARE_COUNT) <= 0 && potatoMine.tickCount % 50 < 4) ? (
                                potatoMine.isInvisible() ? 0.25F : 1
                        ) : 0F));
        this.addLayer(new DirtLayer(this, context.getModelSet()));
    }

    @Override
    protected void scale(T wallNut, PoseStack p_114047_, float p_114048_) {
        float f = wallNut.getEntityData().get(EXPLODE_COUNT) < 0 ? 0 :
                (float) wallNut.getEntityData().get(EXPLODE_COUNT) / 10;
        float f1 = 1.0F + Mth.sin(f * 100.0F) * f * 0.01F;
        f = Mth.clamp(f, 0.0F, 1.0F);
        f *= f;
        f *= f;
        float f2 = (1.0F + f * 0.4F) * f1;
        p_114047_.scale(f2, f2, f2);
    }
    @Override
    public ResourceLocation getTextureLocation(T potatoMine) {
        return potatoMine.isPoisonous() ? POISON : COMMON;
    }

    @Nullable
    @Override
    protected RenderType getRenderType(T potatoMine, boolean p_115323_, boolean p_115324_, boolean p_115325_) {
        return super.getRenderType(potatoMine, p_115323_, p_115324_, p_115325_);
    }

    @Override
    public void render(T potatoMine, float p_115456_, float p_115457_, PoseStack poseStack, MultiBufferSource buffer, int p_115460_) {
        if (EntityUtil.isLeavingGround(potatoMine)) {
            if (potatoMine.tickCount >= 2 || !(this.entityRenderDispatcher.camera.getEntity().distanceToSqr(potatoMine) < 12.25D)) {
                poseStack.pushPose();
                ItemStack itemStack = (potatoMine.isPoisonous() ? Items.POISONOUS_POTATO : Items.POTATO).getDefaultInstance();
                poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
                poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0F));
                ClientProxy.MC.getItemRenderer().renderStatic(itemStack, ItemTransforms.TransformType.GROUND, p_115460_, OverlayTexture.NO_OVERLAY, poseStack, buffer, potatoMine.getId());
                poseStack.popPose();
                var renderNameTagEvent = new net.minecraftforge.client.event.RenderNameTagEvent(potatoMine, potatoMine.getDisplayName(), this, poseStack, buffer, p_115460_, p_115457_);
                net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(renderNameTagEvent);
                if (renderNameTagEvent.getResult() != net.minecraftforge.eventbus.api.Event.Result.DENY && (renderNameTagEvent.getResult() == net.minecraftforge.eventbus.api.Event.Result.ALLOW || this.shouldShowName(potatoMine))) {
                    this.renderNameTag(potatoMine, renderNameTagEvent.getContent(), poseStack, buffer, p_115460_);
                }
            }
            return;
        }
        super.render(potatoMine, p_115456_, p_115457_, poseStack, buffer, p_115460_);
    }

}