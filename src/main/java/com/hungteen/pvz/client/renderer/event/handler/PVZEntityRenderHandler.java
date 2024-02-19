package com.hungteen.pvz.client.renderer.event.handler;

import com.hungteen.pvz.common.entity.bullet.ButterBullet;
import com.hungteen.pvz.common.register.PVZEntities;
import com.hungteen.pvz.util.EntityUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;

public class PVZEntityRenderHandler {
	
	@SuppressWarnings({ "deprecation", "rawtypes" })
	public static void checkAndRenderButter(LivingEntityRenderer r, LivingEntity entity, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
		if(! EntityUtil.isEntityValid(entity) || ! EntityUtil.isEntityButter(entity)) return ;
		ButterBullet butterBullet = PVZEntities.BUTTER.get().create(entity.level);
		matrixStackIn.pushPose();
		float scale = 1.5F;
		matrixStackIn.scale(scale, scale, scale);
		Minecraft.getInstance().getEntityRenderDispatcher().render(butterBullet,0,entity.getBbHeight() / scale,0,1,1,matrixStackIn, bufferIn, packedLightIn);
		matrixStackIn.popPose();
	}
}
