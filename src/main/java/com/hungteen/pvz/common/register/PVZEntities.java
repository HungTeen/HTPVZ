package com.hungteen.pvz.common.register;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.client.model.MooBloomModel;
import com.hungteen.pvz.client.renderer.SimpleMobRenderer;
import com.hungteen.pvz.common.entity.MooBloom;
import com.hungteen.pvz.common.entity.PVZBoat;
import com.hungteen.pvz.common.entity.PVZChestBoat;
import com.hungteen.pvz.client.renderer.misc.PVZBoatRenderer;
import com.hungteen.pvz.client.renderer.PVZLayerHandler;
import com.hungteen.pvz.generator.loot.EntityLootGen;
import com.hungteen.pvz.utils.Util;
import com.mojang.datafixers.util.Pair;
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
    public static final PVZEntities reflector = new PVZEntities();
    //client
    @OnlyIn(Dist.CLIENT)
    public static Map<EntityType<? extends Entity>, List</*0:model, 1:layerDefinition, 2:shadowSize*/?>> simpleRenderedMap = new HashMap<>();
    @OnlyIn(Dist.CLIENT)
    public static Map<EntityType<? extends Entity>, ResourceLocation> simpleTextureLocationMap = new HashMap<>();
    //collision
    private static Pair<Float, Float> storedCollision = Pair.of(0.6F, 1.8F);
    //spawn egg
    private static Pair<Integer, Integer> storedSpawnEgg = null;
    public static Map<RegistryObject, Pair<Integer, Integer>> spawnEggMap = new HashMap<>();


    //registry
    public static final RegistryObject<EntityType<PVZBoat>> BOAT = collision(1.375F, 0.5625F).entity("pvz_boat", PVZBoat::new, MobCategory.MISC);
    public static final RegistryObject<EntityType<PVZChestBoat>> CHEST_BOAT = collision(1.375F, 0.5625F).entity("pvz_chest_boat", PVZChestBoat::new, MobCategory.MISC);
    public static final RegistryObject<EntityType<MooBloom>> MOOBLOOM = spawnEgg(0xffc100, 0x88b830).collision(0.9F, 1.4F).entity("moobloom", MooBloom::new, MobCategory.CREATURE);

    /**
     * for simply rendered entities, auto render at {@link PVZEntities#simpleRenderHandler()}
     * register renderer at {@link PVZEntities#registerRenderer(EntityRenderersEvent.RegisterRenderers)}
     * modelLayers and LayerDefinitions handled in {@link PVZLayerHandler#createModelDefinitions(EntityRenderersEvent.RegisterLayerDefinitions)}
     * lootTables gen in {@link EntityLootGen#addTables()}
     */
    public static void addEntityAttributes(EntityAttributeCreationEvent ev) {
        ev.put(MOOBLOOM.get(), MooBloom.createAttributes().build());
    }


    //client
    @OnlyIn(Dist.CLIENT)
    public static void simpleRenderHandler() {
        rS(MOOBLOOM, MooBloomModel::new, MooBloomModel::createBodyLayer, 0.7F);
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void registerRenderer(EntityRenderersEvent.RegisterRenderers e) {
        r(e, BOAT, (p_174094_) -> new PVZBoatRenderer(p_174094_, false));
        r(e, CHEST_BOAT, (p_174094_) -> new PVZBoatRenderer(p_174094_, true));
        //enter here

        //auto works
        rendererSimple(e);
    }


    //definitions
    private static <T extends Entity> RegistryObject<EntityType<T>> entity(String name, EntityType.EntityFactory<T> factory, MobCategory classification) {
        RegistryObject<EntityType<T>> entity = ENTITIES.register(name, () -> EntityType.Builder.of(factory, classification).sized(storedCollision.getFirst(), storedCollision.getSecond()).build(Util.prefix(name).toString()));
        storedCollision = Pair.of(0.6F, 1.8F);
        if (storedSpawnEgg != null){
            spawnEggMap.put(entity,storedSpawnEgg);
            storedSpawnEgg = null;
        }
        return entity;
    }

    private static PVZEntities collision(Float width, Float height){
        storedCollision = Pair.of(width, height);
        return reflector;
    }
    private static PVZEntities spawnEgg(Integer bgColor, Integer hlColor){
        storedSpawnEgg = Pair.of(bgColor, hlColor);
        return reflector;
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static <T extends Entity> void r(EntityRenderersEvent.RegisterRenderers event, RegistryObject<EntityType<T>> entity, EntityRendererProvider rendererMethod) {
        event.registerEntityRenderer(entity.get(), rendererMethod);
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void rendererSimple(EntityRenderersEvent.RegisterRenderers event) {
        for (EntityType<?> entity : simpleRenderedMap.keySet()) {
            event.registerEntityRenderer(entity, (context) -> new SimpleMobRenderer(context, entity));
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static <T extends Entity> void rS(RegistryObject<EntityType<T>> entity, Function<ModelPart, EntityModel<T>> model, Supplier<LayerDefinition> layer, float shadowSize, String textureDirectory) {
        simpleRenderedMap.put(entity.get(), List.of(model, layer, shadowSize));
        simpleTextureLocationMap.put(entity.get(), prefix(textureDirectory));
    }

    @OnlyIn(Dist.CLIENT)
    public static <T extends Entity> void rS(RegistryObject<EntityType<T>> entity, Function<ModelPart, EntityModel<T>> model, Supplier<LayerDefinition> layer, float shadowSize) {
        rS(entity, model, layer, shadowSize, "textures/entity/" + name(entity) + "/" + name(entity.get()) + ".png");
    }

    public static void release(){
        List.of(spawnEggMap).forEach(Map::clear);
    }
}
