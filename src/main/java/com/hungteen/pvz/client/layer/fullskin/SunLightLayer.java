package com.hungteen.pvz.client.layer.fullskin;

import com.hungteen.pvz.common.entity.plants.base.ProducerPlant;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;


public class SunLightLayer<T extends LivingEntity, M extends EntityModel<T>> extends PVZFullSkinLayer<T,M>{
	ResourceLocation res;

	public SunLightLayer(RenderLayerParent<T, M> entityRendererIn, ResourceLocation lightPicture) {
		super(entityRendererIn);
		res = lightPicture;
	}

	@Override
	protected boolean canRender(T entity) {
		if(entity.isInvisible()) return false;
		if(entity instanceof ProducerPlant) {
			return ((ProducerPlant) entity).isPlantInGen();
		}
		return false;
	}

	@Override
	protected ResourceLocation getResourceLocation(T entity) {
		return res;
	}

}
