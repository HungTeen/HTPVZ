package com.hungteen.pvz.client.renderer;

import com.hungteen.pvz.client.model.attached.DirtModel;
import com.hungteen.pvz.client.model.FloatEssenceBlockModel;
import com.hungteen.pvz.client.model.GrassCarpModel;
import com.hungteen.pvz.client.model.attached.BucketHelmetModel;
import com.hungteen.pvz.client.model.attached.ButterHeadModel;
import com.hungteen.pvz.client.model.attached.ConeHelmetModel;
import com.hungteen.pvz.client.model.attached.FrozenModel;
import com.hungteen.pvz.client.model.bullet.CommonBulletModel;
import com.hungteen.pvz.client.model.bullet.MelonBulletModel;
import com.hungteen.pvz.client.model.plants.*;
import com.hungteen.pvz.common.register.PVZEntities;
import com.hungteen.pvz.common.register.PVZItems;
import net.minecraft.client.model.geom.LayerDefinitions;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static com.hungteen.pvz.util.Util.name;
import static com.hungteen.pvz.util.Util.prefix;

@OnlyIn(Dist.CLIENT)
public class PVZLayerHandler {
    //both the function of ModelLayers and LayerDefinitions included here.
    public static Map</*entityTypeName*/String, ModelLayerLocation> LayerLocationMap = new HashMap<>();

    @SubscribeEvent
    public static void createModelDefinitions(EntityRenderersEvent.RegisterLayerDefinitions e) {
        PVZEntities.simpleRenderHandler();
        //enter here.
        L(e, PVZEntities.GRASSCARP, GrassCarpModel::createBodyLayer);
        L(e, PVZEntities.SUN_FLOWER, SunFlowerModel::createBodyLayer);
        L(e, PVZEntities.WALL_NUT, WallNutModel::createBodyLayer);
        L(e, PVZEntities.WALL_NUT, "armor", WallNutArmorModel::createBodyLayer);
        L(e, PVZEntities.POTATO_MINE, PotatoMineModel::createBodyLayer);
        L(e, PVZEntities.SNOW_PEA, SnowPeaModel::createBodyLayer);
        L(e, PVZEntities.CHOMPER, ChomperModel::createBodyLayer);
        L(e, PVZEntities.TANGLE_KELP, TangleKelpModel::createBodyLayer);
        L(e, PVZEntities.JALAPENO, JalapenoModel::createBodyLayer);
        L(e, PVZEntities.SPIKE_WEED, SpikeWeedModel::createBodyLayer);
        L(e, PVZEntities.TORCH_WOOD, TorchWoodModel::createBodyLayer);
        L(e, PVZEntities.TALL_NUT, TallNutModel::createBodyLayer);
        L(e, PVZEntities.TALL_NUT, "armor", TallNutArmorModel::createBodyLayer);
        L(e, PVZEntities.PLANTERN, PlanternModel::createBodyLayer);
        L(e, PVZEntities.PUMPKIN, PumpkinModel::createBodyLayer);
        L(e, PVZEntities.FLOWER_POT, FlowerPotModel::createBodyLayer);
        L(e, PVZEntities.MARIGOLD, MariGoldModel::createBodyLayer);
        L(e, "melon_bullet", MelonBulletModel::createBodyLayer);
        L(e, "common_bullet", CommonBulletModel::createBodyLayer);
        L(e, "floating_essence_block", FloatEssenceBlockModel::createBodyLayer);
        L(e, "dirt", DirtModel::createBodyLayer);
        L(e, "butter", ButterHeadModel::createBodyLayer);
        L(e, "ice", FrozenModel::createBodyLayer);
        L(e, PVZItems.CONE_HELMET, () -> ConeHelmetModel.createBodyLayer(LayerDefinitions.OUTER_ARMOR_DEFORMATION));
        L(e, PVZItems.BUCKET_HELMET, () -> BucketHelmetModel.createBodyLayer(LayerDefinitions.OUTER_ARMOR_DEFORMATION));

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
    private static void L(EntityRenderersEvent.RegisterLayerDefinitions event, RegistryObject<?> registryObject, String layerName, Supplier<LayerDefinition> provider){
        L(event, name(registryObject), layerName, provider);
    }
    private static void L(EntityRenderersEvent.RegisterLayerDefinitions event, RegistryObject<?> registryObject, Supplier<LayerDefinition> provider){
        L(event, name(registryObject), provider);
    }

    private static ModelLayerLocation createLayer(String name, String layerName) {
        if (LayerLocationMap.containsKey(name)) {
            throw new IllegalStateException("Duplicate registration for " + name);
        } else {
            return new ModelLayerLocation(prefix(name), layerName);
        }
    }
}
