package com.hungteen.pvz.client.layer;

import com.hungteen.pvz.common.register.PVZMobEffects;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;

public class EntityHypnotizedLayer<T extends LivingEntity, M extends EntityModel<T>> extends LightLayer<T, M> {
    private final LivingEntityRenderer<T, M> renderer;
    protected static final RenderStateShard.WriteMaskStateShard COLOR_WRITE = new RenderStateShard.WriteMaskStateShard(true, false);
    protected static final RenderStateShard.TransparencyStateShard TRANSLUCENT_TRANSPARENCY = new RenderStateShard.TransparencyStateShard("translucent_transparency", () -> {
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
    }, () -> {
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    });

    public EntityHypnotizedLayer(RenderLayerParent<T, M> layerParent) {
        super(layerParent, EntityHypnotizedLayer::hypnotisedColorFunction, null);
        renderer = (LivingEntityRenderer<T, M>) layerParent;
    }

    public static Vec3 hypnotisedColorFunction(LivingEntity entity, Float partialTick, Float ageInTicks) {
        return new Vec3(0.8F + 0.2F * Math.sin(partialTick / 15),
                0.3F + 0.3F * Math.sin(partialTick / 23),
                0.4F + 0.4F * Math.sin(partialTick / 10));
    }
    @Override
    public void render(PoseStack stack, MultiBufferSource bufferSource, int packedLightIn, T entity,
                       float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        this.res = renderer.getTextureLocation(entity);
        alpha = (float) Math.sin(ageInTicks / 20) * 0.2F + 0.4F;
        if (entity.getAttribute(Attributes.ARMOR).getModifier(PVZMobEffects.HYPNOTIZED_EFFECT_UUID) != null && entity.hurtTime <= 0 && entity.deathTime <= 0) {
            super.render(stack, bufferSource, packedLightIn, entity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
        }
    }

    public RenderType renderType(ResourceLocation p_110437_, float p_110438_, float p_110439_) {
        return RenderType.create("pvz_hypnotized_light", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, false, true,
                RenderType.CompositeState.builder()
                        .setShaderState(RENDERTYPE_ENERGY_SWIRL_SHADER)
                        .setTextureState(new RenderStateShard.TextureStateShard(p_110437_, false, false))
                        .setTexturingState(new RenderStateShard.OffsetTexturingStateShard(p_110438_, p_110439_))
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .setCullState(NO_CULL)
                        .setWriteMaskState(COLOR_WRITE)
                        .setOverlayState(OVERLAY)
                        .createCompositeState(false));
    }
}
