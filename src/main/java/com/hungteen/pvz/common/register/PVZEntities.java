package com.hungteen.pvz.common.register;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.common.entity.PVZBoat;
import com.hungteen.pvz.common.entity.PVZChestBoat;
import com.hungteen.pvz.client.PVZBoatRenderer;
import com.hungteen.pvz.utils.Util;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class PVZEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, PVZMod.MODID);
    public static final RegistryObject<EntityType<PVZBoat>> BOAT = entity("pvz_boat", PVZBoat::new, MobCategory.MISC);
    public static final RegistryObject<EntityType<PVZChestBoat>> CHEST_BOAT = entity("pvz_chest_boat", PVZChestBoat::new, MobCategory.MISC);


    //definitions
    private static <T extends Entity> RegistryObject<EntityType<T>> entity(String name, EntityType.EntityFactory<T> factory, MobCategory classification){
        return ENTITIES.register(name, () -> EntityType.Builder.of(factory, classification).build(Util.prefix(name).toString()));
    }
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void registerRenderer(EntityRenderersEvent.RegisterRenderers e){
        r(e, BOAT, (p_174094_) -> new PVZBoatRenderer(p_174094_, false));
        r(e, CHEST_BOAT, (p_174094_) -> new PVZBoatRenderer(p_174094_, true));
    }
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static <T extends Entity> void r(EntityRenderersEvent.RegisterRenderers event, RegistryObject<EntityType<T>> entity, EntityRendererProvider rendererMethod){
        event.registerEntityRenderer(entity.get(), rendererMethod);
    }
}
