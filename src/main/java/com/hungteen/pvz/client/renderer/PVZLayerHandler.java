package com.hungteen.pvz.client.renderer;

import com.hungteen.pvz.common.register.PVZEntities;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static com.hungteen.pvz.utils.Util.name;
import static com.hungteen.pvz.utils.Util.prefix;

@OnlyIn(Dist.CLIENT)
public class PVZLayerHandler {
    //both the function of ModelLayers and LayerDefinitions included here.
    public static Map</*entityTypeName*/String, ModelLayerLocation> LayerLocationMap = new HashMap<>();

    @SubscribeEvent
    public static void createModelDefinitions(EntityRenderersEvent.RegisterLayerDefinitions e){
        PVZEntities.simpleRenderHandler();
        //enter here.

        //simple rendered entities
        for (EntityType<? extends Entity> entity: PVZEntities.simpleRenderedMap.keySet()){
            L(e, name(entity), (Supplier<LayerDefinition>) PVZEntities.simpleRenderedMap.get(entity).get(1));
        }
    }

    //definitions
    private static void L(EntityRenderersEvent.RegisterLayerDefinitions event, String name, String layerName, Supplier<LayerDefinition> provider){
        ModelLayerLocation location = createLayer(name, layerName);
        event.registerLayerDefinition(location, provider);
        LayerLocationMap.put(name+":"+layerName, location);
    }
    private static void L(EntityRenderersEvent.RegisterLayerDefinitions event, String name, Supplier<LayerDefinition> provider){
        L(event, name, "main", provider);
    }

    private static ModelLayerLocation createLayer(String name, String layerName) {
        if (LayerLocationMap.containsKey(name)) {
            throw new IllegalStateException("Duplicate registration for " + name);
        } else {
            return new ModelLayerLocation(prefix(name), layerName);
        }
    }
}
