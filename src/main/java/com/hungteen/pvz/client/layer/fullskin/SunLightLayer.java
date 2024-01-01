package com.hungteen.pvz.client.layer.fullskin;

import com.hungteen.pvz.common.entity.plants.base.PlantProducerEntity;
import com.hungteen.pvz.util.Util;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;


public class SunLightLayer<T extends LivingEntity, M extends EntityModel<T>> extends PVZFullSkinLayer<T,M>{

	public SunLightLayer(RenderLayerParent<T, M> entityRendererIn) {
		super(entityRendererIn);
	}

	@Override
	protected boolean canRender(T entity) {
		if(entity.isInvisible()) return false;
		if(entity instanceof PlantProducerEntity) {
			return ((PlantProducerEntity) entity).isPlantInGen();
		}
		return false;
	}

	@Override
	protected ResourceLocation getResourceLocation(T entity) {
		return Util.prefix("textures/entity/layer/sun_light.png");
	}

}
