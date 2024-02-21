package com.hungteen.pvz.client.renderer;

import com.hungteen.pvz.client.model.armor.ButterHeadModel;
import com.hungteen.pvz.common.network.ClientProxy;
import com.hungteen.pvz.common.register.PVZMobEffects;
import com.hungteen.pvz.util.Util;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.Attributes;

import static net.minecraft.client.renderer.entity.LivingEntityRenderer.isEntityUpsideDown;

public class PVZEntityRenderHandler {

	@SuppressWarnings({"rawtypes"})
	public static void checkAndRenderButter(LivingEntityRenderer renderer, LivingEntity entity, PoseStack poseStack, MultiBufferSource buffer, int light) {
		if (entity.getAttribute(Attributes.MOVEMENT_SPEED).getModifier(PVZMobEffects.BUTTER_EFFECT_UUID) != null) {
			ButterHeadModel model = new ButterHeadModel(ClientProxy.MC.getEntityModels().bakeLayer(PVZLayerHandler.LayerLocationMap.get("butter:main")));
			poseStack.pushPose();
			VertexConsumer vertexConsumer = buffer.getBuffer(model.renderType(Util.prefix("textures/models/butter/butter_head.png")));
			model.setupAnim(entity, 0F, 0F, 0F, 0F, 0F);

			float f = Mth.rotLerp(0, entity.yBodyRotO, entity.yBodyRot);
			float f1 = Mth.rotLerp(0, entity.yHeadRotO, entity.yHeadRot);
			float f2 = f1 - f;
			if (entity.shouldRiderSit() && entity.getVehicle() instanceof LivingEntity) {
				LivingEntity livingentity = (LivingEntity)entity.getVehicle();
				f = Mth.rotLerp(0, livingentity.yBodyRotO, livingentity.yBodyRot);
				f2 = f1 - f;
				float f3 = Mth.wrapDegrees(f2);
				if (f3 < -85.0F) {
					f3 = -85.0F;
				}

				if (f3 >= 85.0F) {
					f3 = 85.0F;
				}

				f = f1 - f3;
				if (f3 * f3 > 2500.0F) {
					f += f3 * 0.2F;
				}

				f2 = f1 - f;
			}

			float f6 = Mth.lerp(0, entity.xRotO, entity.getXRot());
			if (isEntityUpsideDown(entity)) {
				f6 *= -1.0F;
				f2 *= -1.0F;
			}

			if (entity.hasPose(Pose.SLEEPING)) {
				Direction direction = entity.getBedOrientation();
				if (direction != null) {
					float f4 = entity.getEyeHeight(Pose.STANDING) - 0.1F;
					poseStack.translate((-direction.getStepX()) * f4, 0.0D, (-direction.getStepZ()) * f4);
				}
			}

			float f7 = entity.tickCount;
			setupRotations(renderer, entity, poseStack, f7, f, 0);

			poseStack.scale(-1, -1, 1);
			poseStack.translate(0, -1.5, 0);
			model.renderToBuffer(poseStack, vertexConsumer, light, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
			poseStack.popPose();
		}
	}


	//methods
	public static void setupRotations(LivingEntityRenderer renderer, LivingEntity p_115317_, PoseStack p_115318_, float p_115319_, float p_115320_, float p_115321_) {
		if (p_115317_.isFullyFrozen()) {
			p_115320_ += (float)(Math.cos((double)p_115317_.tickCount * 3.25D) * Math.PI * (double)0.4F);
		}

		if (!p_115317_.hasPose(Pose.SLEEPING)) {
			p_115318_.mulPose(Vector3f.YP.rotationDegrees(180.0F - p_115320_));
		}

		if (p_115317_.deathTime > 0) {
			float f = ((float)p_115317_.deathTime + p_115321_ - 1.0F) / 20.0F * 1.6F;
			f = Mth.sqrt(f);
			if (f > 1.0F) {
				f = 1.0F;
			}

			p_115318_.mulPose(Vector3f.ZP.rotationDegrees(f * 90F));
		} else if (p_115317_.isAutoSpinAttack()) {
			p_115318_.mulPose(Vector3f.XP.rotationDegrees(-90.0F - p_115317_.getXRot()));
			p_115318_.mulPose(Vector3f.YP.rotationDegrees(((float)p_115317_.tickCount + p_115321_) * -75.0F));
		} else if (p_115317_.hasPose(Pose.SLEEPING)) {
			Direction direction = p_115317_.getBedOrientation();
			float f1 = direction != null ? sleepDirectionToRotation(direction) : p_115320_;
			p_115318_.mulPose(Vector3f.YP.rotationDegrees(f1));
			p_115318_.mulPose(Vector3f.ZP.rotationDegrees(90F));
			p_115318_.mulPose(Vector3f.YP.rotationDegrees(270.0F));
		} else if (isEntityUpsideDown(p_115317_)) {
			p_115318_.translate(0.0D, p_115317_.getBbHeight() + 0.1F, 0.0D);
			p_115318_.mulPose(Vector3f.ZP.rotationDegrees(180.0F));
		}

	}
	private static float sleepDirectionToRotation(Direction p_115329_) {
		return switch (p_115329_) {
			case SOUTH -> 90.0F;
			case NORTH -> 270.0F;
			case EAST -> 180.0F;
			default -> 0.0F;
		};
	}
}
