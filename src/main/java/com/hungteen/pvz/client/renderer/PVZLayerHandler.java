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
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static com.hungteen.pvz.utils.Util.name;
import static com.hungteen.pvz.utils.Util.prefix;

@OnlyIn(Dist.CLIENT)
public class PVZLayerHandler {
    //both the function of ModelLayers and LayerDefinitions included here.
    public static Map</*entityName*/String, List<ModelLayerLocation>> LayerLocationMap = new HashMap<>();



    public static void createLayerLocations(){
        //enter here.

        simpleLayers();
    }
    @SubscribeEvent
    public static void createModelDefinitions(EntityRenderersEvent.RegisterLayerDefinitions e){
        PVZEntities.rendererDistributor();
        createLayerLocations();
        simpleModelDefinitions(e);
    }



    //modelLayer definitions
    private static void L(String name, String... layerNames){
        List<ModelLayerLocation> layers = new ArrayList<>();
        for (String layerName: layerNames){
            layers.add(createLayer(name, layerName));
        }
        LayerLocationMap.put(name, layers);
    }
    private static void L(RegistryObject<?> obj, String... layerNames){
        String name = name((EntityType<? extends Entity>) obj.get());
        List<ModelLayerLocation> layers = new ArrayList<>();
        for (String layerName: layerNames){
            layers.add(createLayer(name, layerName));
        }
        LayerLocationMap.put(name, layers);
    }
    private static void L(RegistryObject<?> obj){
        L(obj, "main");
    }
    private static void L(String name){
        L(name, "main");
    }
    private static ModelLayerLocation createLayer(String name) {
        return createLayer(name, "main");
    }
    private static ModelLayerLocation createLayer(String name, String layerName) {
        if (LayerLocationMap.containsKey(name)) {
            throw new IllegalStateException("Duplicate registration for " + name);
        } else {
            return new ModelLayerLocation(prefix(name), layerName);
        }
    }
    private static void simpleLayers(){
        for (EntityType<? extends Entity> entity: PVZEntities.simpleRenderedMap.keySet()){
            LayerLocationMap.put(name(entity), List.of(createLayer(name(entity))));
        }
    }


    //LayerDefinitions definitions
    @SubscribeEvent
    public static void simpleModelDefinitions(EntityRenderersEvent.RegisterLayerDefinitions e){
        //enter here.

        for (EntityType<? extends Entity> entity: PVZEntities.simpleRenderedMap.keySet()){
            registerModel(e, LayerLocationMap.get(name(entity)).get(0), (Supplier<LayerDefinition>) PVZEntities.simpleRenderedMap.get(entity).get(1));
        }
    }
    @SubscribeEvent
    public static void registerModel(EntityRenderersEvent.RegisterLayerDefinitions event, ModelLayerLocation location, Supplier<LayerDefinition> provider) {
        event.registerLayerDefinition(location, provider);
    }
}
