package com.hungteen.pvz.client.renderer.misc;

import com.hungteen.pvz.common.entity.Sun;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

/**
 * @program: pvzmod-1.18.x
 * @author: HungTeen
 * @create: 2022-03-11 11:44
 **/

public class SunRenderer extends EntityRenderer<Sun> {

	private static final ResourceLocation SUN_LOCATION = new ResourceLocation("pvz", "textures/entity/sun/sun.png");
	private static final RenderType RENDER_TYPE = RenderType.itemEntityTranslucentCull(SUN_LOCATION);

	public SunRenderer(EntityRendererProvider.Context p_174110_) {
		super(p_174110_);
		this.shadowRadius = 0.15F;
		this.shadowStrength = 0.75F;
	}

	protected int getBlockLightLevel(Sun p_114606_, BlockPos p_114607_) {
		return Math.min(super.getBlockLightLevel(p_114606_, p_114607_) + 7, 15);
	}

	@Override
	public void render(Sun sun, float p_114600_, float partialTicks, PoseStack stack, MultiBufferSource bufferSource, int p_114604_) {
		//TODO matrix not big enough. Fix that.
		stack.pushPose();
		final int i = sun.getIcon();
		final float x = (float) (i % 2 * 32 + 16) / 64.0F;
		final float y = (float) (i / 2 * 32 + 16) / 64.0F;
		final float tick = ((float) sun.tickCount + partialTicks) / 20.0F + 0.79F;

		int red;
		int blue;
		int green;
		int alpha;
		red = (int) (sun.ColorBase.x + Math.sin(tick) * sun.ColorChange.x);
		green = (int) (sun.ColorBase.y + Math.sin(tick) * sun.ColorChange.y);
		blue = (int) (sun.ColorBase.z + Math.sin(tick) * sun.ColorChange.z);
		if (sun.getMaxLiveTick() > -1) {
			int tmp = sun.getMaxLiveTick() - sun.getLiveTick();
			if (tmp >= 90) {
				alpha = 255;
			} else if (tmp > 10) {
				alpha = 159 - (int) ((Math.sin((float) ((tmp + 5) / 3.183))) * 96);
			} else {
				alpha = 127 - (int) ((Math.sin((float) ((tmp + 5) / 3.183))) * 127);
			}
		} else {
			alpha = 255;
		}

		stack.translate(0.0D, (double) 0.1F, 0.0D);
		stack.mulPose(this.entityRenderDispatcher.cameraOrientation());
		stack.mulPose(Vector3f.YP.rotationDegrees(180.0F));
		final float size = 0.4F;
		stack.scale(size, size, size);
		VertexConsumer vertexconsumer = bufferSource.getBuffer(RENDER_TYPE);
		PoseStack.Pose posestack$pose = stack.last();
		Matrix4f matrix4f = posestack$pose.pose();
		Matrix3f matrix3f = posestack$pose.normal();
		vertex(vertexconsumer, matrix4f, matrix3f, -1F, -1.0F, red, green, blue, x + 12.01F * Math.cos(tick) / 64, y + 12.01F * Math.sin(tick) / 64, p_114604_, alpha);
		vertex(vertexconsumer, matrix4f, matrix3f, 1F, -1.0F, red, green, blue, x + 12.01F * Math.cos(tick + 1.57F) / 64, y + 12.01F * Math.sin(tick + 1.57F) / 64, p_114604_, alpha);
		vertex(vertexconsumer, matrix4f, matrix3f, 1F, 1.0F, red, green, blue, x - 12.01F * Math.cos(tick) / 64, y - 12.01F * Math.sin(tick) / 64, p_114604_, alpha);
		vertex(vertexconsumer, matrix4f, matrix3f, -1F, 1.0F, red, green, blue, x - 12.01F * Math.cos(tick + 1.57F) / 64, y - 12.01F * Math.sin(tick + 1.57F) / 64, p_114604_, alpha);
		stack.popPose();
		super.render(sun, p_114600_, partialTicks, stack, bufferSource, p_114604_);
	}

	private static void vertex(VertexConsumer p_114609_, Matrix4f p_114610_, Matrix3f p_114611_, float p_114612_, float p_114613_, int p_114614_, int p_114615_, int p_114616_, double p_114617_, double p_114618_, int p_114619_, int alpha) {
		p_114609_.vertex(p_114610_, p_114612_, p_114613_ + 0.5F, 0.0F).color(p_114614_, p_114615_, p_114616_, alpha).uv((float) p_114617_, (float) p_114618_).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(p_114619_).normal(p_114611_, 0.0F, 1.0F, 0.0F).endVertex();
	}

	public ResourceLocation getTextureLocation(Sun p_114597_) {
		return SUN_LOCATION;
	}

}