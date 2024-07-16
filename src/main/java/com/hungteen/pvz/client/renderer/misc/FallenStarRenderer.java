package com.hungteen.pvz.client.renderer.misc;

import com.hungteen.pvz.common.network.ClientProxy;
import com.hungteen.pvz.util.Util;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.ItemEntity;

/**
 * @program: pvzmod-1.18.x
 * @author: HungTeen
 * @create: 2022-03-11 11:44
 **/

public class FallenStarRenderer extends ItemEntityRenderer {

	private static final ResourceLocation LOCATION = Util.prefix("textures/entity/fallen_star/fallen_star.png");
	private static final RenderType RENDER_TYPE = RenderType.itemEntityTranslucentCull(LOCATION);

	public FallenStarRenderer(EntityRendererProvider.Context p_174110_) {
		super(p_174110_);
		this.shadowRadius = 0.15F;
		this.shadowStrength = 0.75F;
	}

	protected int getBlockLightLevel(ItemEntity p_114606_, BlockPos p_114607_) {
		return Math.min(super.getBlockLightLevel(p_114606_, p_114607_) + 7, 15);
	}

	@Override
	public void render(ItemEntity fallenStar, float p_114600_, float partialTicks, PoseStack stack, MultiBufferSource bufferSource, int p_114604_) {
		stack.pushPose();
		final float x = (float) 0.5;
		final float y = (float) 0.5;
		final float tick = ((float) fallenStar.tickCount + partialTicks) / 20.0F + 0.79F;

		int alpha = 159 - (int) ((Math.sin((float) ((fallenStar.tickCount + 5) / 3.183))) * 96);

		stack.translate(0.0D, 0.1F, 0.0D);
		stack.mulPose(this.entityRenderDispatcher.cameraOrientation());
		stack.mulPose(Vector3f.YP.rotationDegrees(180.0F));
		final float size = 0.4F * ((float) (ClientProxy.getPlayer() == null ? 1 : Math.max(1, 0.8 * Math.log10(fallenStar.distanceToSqr(ClientProxy.getPlayer())))));
		stack.scale(size, size, size);
		VertexConsumer vertexconsumer = bufferSource.getBuffer(RENDER_TYPE);
		PoseStack.Pose posestack$pose = stack.last();
		Matrix4f matrix4f = posestack$pose.pose();
		Matrix3f matrix3f = posestack$pose.normal();
		vertex(vertexconsumer, matrix4f, matrix3f, -1F, -1.0F, 255, 255, 255, x + 35F * Math.cos(tick) / 64, y + 35F * Math.sin(tick) / 64, p_114604_, alpha);
		vertex(vertexconsumer, matrix4f, matrix3f, 1F, -1.0F, 255, 255, 255, x + 35F * Math.cos(tick + 1.57F) / 64, y + 35F * Math.sin(tick + 1.57F) / 64, p_114604_, alpha);
		vertex(vertexconsumer, matrix4f, matrix3f, 1F, 1.0F, 255, 255, 255, x - 35F * Math.cos(tick) / 64, y - 35F * Math.sin(tick) / 64, p_114604_, alpha);
		vertex(vertexconsumer, matrix4f, matrix3f, -1F, 1.0F, 255, 255, 255, x - 35F * Math.cos(tick + 1.57F) / 64, y - 35F * Math.sin(tick + 1.57F) / 64, p_114604_, alpha);
		stack.popPose();
		super.render(fallenStar, p_114600_, partialTicks, stack, bufferSource, p_114604_);
	}

	private static void vertex(VertexConsumer p_114609_, Matrix4f p_114610_, Matrix3f p_114611_, float p_114612_, float p_114613_, int p_114614_, int p_114615_, int p_114616_, double p_114617_, double p_114618_, int p_114619_, int alpha) {
		p_114609_.vertex(p_114610_, p_114612_, p_114613_ + 0.5F, 0.0F).color(p_114614_, p_114615_, p_114616_, alpha).uv((float) p_114617_, (float) p_114618_).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(p_114619_).normal(p_114611_, 0.0F, 1.0F, 0.0F).endVertex();
	}

}