package com.hungteen.pvz.common.register;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.client.model.MooBloomModel;
import com.hungteen.pvz.client.renderer.SimpleMobRenderer;
import com.hungteen.pvz.common.entity.MooBloom;
import com.hungteen.pvz.common.entity.PVZBoat;
import com.hungteen.pvz.common.entity.PVZChestBoat;
import com.hungteen.pvz.client.renderer.misc.PVZBoatRenderer;
import com.hungteen.pvz.utils.Util;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.hungteen.pvz.utils.Util.name;
import static com.hungteen.pvz.utils.Util.prefix;

public class PVZEntities {
    //init
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, PVZMod.MODID);
    @OnlyIn(Dist.CLIENT) public static Map<EntityType<? extends Entity>, List<?/*0:model, 1:layerDefinition, 2:layerSize*/>> simpleRenderedMap = new HashMap<>();
    @OnlyIn(Dist.CLIENT) public static Map<EntityType<? extends Entity>, ResourceLocation> SimpleTextureLocationMap = new HashMap<>();

    //registry
    public static final RegistryObject<EntityType<PVZBoat>> BOAT = entity("pvz_boat", PVZBoat::new, MobCategory.MISC, 1.375F, 0.5625F);
    public static final RegistryObject<EntityType<PVZChestBoat>> CHEST_BOAT = entity("pvz_chest_boat", PVZChestBoat::new, MobCategory.MISC, 1.375F, 0.5625F);
    public static final RegistryObject<EntityType<MooBloom>> MOOBLOOM = entity("moobloom", MooBloom::new, MobCategory.CREATURE, 0.9F, 1.4F);
    /**
     * for simply rendered entities, auto render at {@link PVZEntities#rendererDistributor()}
     * register renderer at {@link PVZEntities#registerRenderer(EntityRenderersEvent.RegisterRenderers)}
     * modelLayers and LayerDefinitions handled in {@link com.hungteen.pvz.client.renderer.PVZLayerHandler}
     */
    public static void addEntityAttributes(EntityAttributeCreationEvent ev) {
        ev.put(MOOBLOOM.get(), MooBloom.createAttributes().build());
    }

    //client
    @OnlyIn(Dist.CLIENT)
    public static void rendererDistributor(){
        rS(MOOBLOOM, MooBloomModel::new, MooBloomModel::createBodyLayer, 0.7F);
    }


    //definitions
    private static <T extends Entity> RegistryObject<EntityType<T>> entity(String name, EntityType.EntityFactory<T> factory, MobCategory classification, float width, float height){
        return ENTITIES.register(name, () -> EntityType.Builder.of(factory, classification).sized(width, height).build(Util.prefix(name).toString()));
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void registerRenderer(EntityRenderersEvent.RegisterRenderers e){
        r(e, BOAT, (p_174094_) -> new PVZBoatRenderer(p_174094_, false));
        r(e, CHEST_BOAT, (p_174094_) -> new PVZBoatRenderer(p_174094_, true));
        //auto works
        rendererSimple(e);
    }
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static <T extends Entity> void r(EntityRenderersEvent.RegisterRenderers event, RegistryObject<EntityType<T>> entity, EntityRendererProvider rendererMethod){
        event.registerEntityRenderer(entity.get(), rendererMethod);
    }
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void rendererSimple(EntityRenderersEvent.RegisterRenderers event){
        for (EntityType<?> entity: simpleRenderedMap.keySet()){
            event.registerEntityRenderer(entity, (context) -> new SimpleMobRenderer(context, entity));
        }
    }
    @OnlyIn(Dist.CLIENT)
    public static <T extends Entity> void rS(RegistryObject<EntityType<T>> entity, Function<ModelPart, EntityModel<T>> model, Supplier<LayerDefinition> layer, float shadowSize, String textureDirectory){
        simpleRenderedMap.put(entity.get(), List.of(model, layer, shadowSize));
        SimpleTextureLocationMap.put(entity.get(), prefix(textureDirectory));
    }
    @OnlyIn(Dist.CLIENT)
    public static <T extends Entity> void rS(RegistryObject<EntityType<T>> entity, Function<ModelPart, EntityModel<T>> model, Supplier<LayerDefinition> layer, float shadowSize){
        rS(entity, model, layer, shadowSize, "textures/entity/"+name(entity.get())+"/"+name(entity.get())+".png");
    }
}
