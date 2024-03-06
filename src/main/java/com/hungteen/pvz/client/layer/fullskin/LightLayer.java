package com.hungteen.pvz.client.layer.fullskin;

import com.hungteen.pvz.common.entity.plants.base.ProducerPlant;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

import java.util.function.Function;


public class LightLayer<T extends LivingEntity, M extends EntityModel<T>> extends PVZFullSkinLayer<T,M>{
	ResourceLocation res;
	Function<T, Boolean> condition;

	public LightLayer(RenderLayerParent<T, M> entityRendererIn, ResourceLocation lightPicture) {
		super(entityRendererIn);
		res = lightPicture;
		condition = this::defaultCondition;
	}
	public LightLayer(RenderLayerParent<T, M> entityRendererIn, ResourceLocation lightPicture, Function<T, Boolean> condition) {
		super(entityRendererIn);
		res = lightPicture;
		this.condition = condition;
	}
	@Override
	protected boolean canRender(T entity) {
		return condition.apply(entity);
	}

	public boolean defaultCondition(T entity) {
		if(entity.isInvisible()) return false;
		if(entity instanceof ProducerPlant) {
			return ((ProducerPlant) entity).isPlantInGen();
		}
		return true;
	}

	@Override
	protected ResourceLocation getResourceLocation(T entity) {
		return res;
	}

}
