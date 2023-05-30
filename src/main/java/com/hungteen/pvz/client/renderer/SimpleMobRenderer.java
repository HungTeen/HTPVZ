package com.hungteen.pvz.client.renderer;

import com.hungteen.pvz.common.register.PVZEntities;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;

import java.util.function.Function;

import static com.hungteen.pvz.utils.Util.name;


public class SimpleMobRenderer<T extends Mob> extends MobRenderer<T, EntityModel<T>> {
    public SimpleMobRenderer(EntityRendererProvider.Context context, EntityType<T> entityType) {
        super(context, /*model*/((Function<ModelPart,EntityModel<T>>) PVZEntities.simpleRenderedMap.get(entityType).get(0))
                .apply(context.bakeLayer(PVZLayerHandler.LayerLocationMap.get(name(entityType)).get(0))),
                /*shadowSize*/ (float) PVZEntities.simpleRenderedMap.get(entityType).get(2));
    }
    public ResourceLocation getTextureLocation(Mob mob) {
        return PVZEntities.SimpleTextureLocationMap.get(mob.getType());
    }
}
