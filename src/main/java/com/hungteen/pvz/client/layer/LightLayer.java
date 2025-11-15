package com.hungteen.pvz.client.layer;

import com.hungteen.pvz.common.entity.plants.base.ProducerPlant;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.function.TriFunction;


public class LightLayer<T extends Entity, M extends EntityModel<T>> extends RenderLayer<T,M> {

	protected static final RenderStateShard.ShaderStateShard RENDERTYPE_ENERGY_SWIRL_SHADER = new RenderStateShard.ShaderStateShard(GameRenderer::getRendertypeEnergySwirlShader);
	protected static final RenderStateShard.TransparencyStateShard LIGHTNING_TRANSPARENCY = new RenderStateShard.TransparencyStateShard("lightning_transparency", () -> {
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
	}, () -> {
		RenderSystem.disableBlend();
		RenderSystem.defaultBlendFunc();
	});
	protected static final RenderStateShard.CullStateShard NO_CULL = new RenderStateShard.CullStateShard(false);
	protected static final RenderStateShard.LightmapStateShard LIGHTMAP = new RenderStateShard.LightmapStateShard(true);
	protected static final RenderStateShard.OverlayStateShard OVERLAY = new RenderStateShard.OverlayStateShard(true);
	protected ResourceLocation res;
	private final TriFunction<T, Float, Float, Vec3> colorFunction;
	public float alpha = 1; //as long as renderType() is not changed, black acts the same as 0 alpha.

	public LightLayer(RenderLayerParent<T, M> entityRendererIn, ResourceLocation texture) {
		this(entityRendererIn, texture, LightLayer::defaultAlphaFunction);
	}
	public LightLayer(RenderLayerParent<T, M> entityRendererIn, ResourceLocation texture, TriFunction<T, Float, Float, Float> alphaFunction) {
		this(entityRendererIn, (entity, partialTicks, ageInTicks) -> {
			float tmp = alphaFunction.apply(entity, partialTicks, ageInTicks);
			return new Vec3(tmp, tmp, tmp);
		}, texture);
	}
	public LightLayer(RenderLayerParent<T, M> entityRendererIn, TriFunction<T, Float, Float, Vec3> colorFunction, ResourceLocation texture) {
		super(entityRendererIn);
		this.res = texture;
		this.colorFunction = colorFunction;
	}

	public static float defaultAlphaFunction(Entity entity, Float partialTick, Float ageInTicks) {
		if(entity.isInvisible()) return 0;
		if(entity instanceof ProducerPlant producer) {
			return producer.isPlantInGen() ? (float) Math.min(producer.getAttackTime() , Math.min(producer.getGenerateAnimLength() - producer.getAttackTime(), 4)) / 4 : 0;
		}
		return 1;
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource bufferIn, int packedLightIn, T entity,
					   float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw,
					   float headPitch) {
		Vec3 color = this.colorFunction.apply(entity, partialTicks, ageInTicks);
		if (color.lengthSqr() > 0) {
			poseStack.pushPose();
			VertexConsumer iVertexBuilder = bufferIn.getBuffer(renderType(res, 0, 0));
			getParentModel().renderToBuffer(poseStack, iVertexBuilder, packedLightIn, OverlayTexture.NO_OVERLAY,
					(float) color.x, (float) color.y, (float) color.z, alpha);
		poseStack.popPose();
		}
	}

	public RenderType renderType(ResourceLocation p_110437_, float p_110438_, float p_110439_) {
		return RenderType.create("pvz_light", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, false, true,
				RenderType.CompositeState.builder()
						.setShaderState(RENDERTYPE_ENERGY_SWIRL_SHADER)
						.setTextureState(new RenderStateShard.TextureStateShard(p_110437_, false, false))
						.setTexturingState(new RenderStateShard.OffsetTexturingStateShard(p_110438_, p_110439_))
						.setTransparencyState(LIGHTNING_TRANSPARENCY)
						.setCullState(NO_CULL)
						.setLightmapState(LIGHTMAP)
						.setOverlayState(OVERLAY)
						.createCompositeState(false));
	}
}
