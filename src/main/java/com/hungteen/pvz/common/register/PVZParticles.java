package com.hungteen.pvz.common.register;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.client.particle.*;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = PVZMod.MODID)
public class PVZParticles {
    @Deprecated // will be cleared after register.
    public static Map<RegistryObject<SimpleParticleType>, ParticleEngine.SpriteParticleRegistration<SimpleParticleType>> particleMap = new HashMap<>();

    public static final DeferredRegister<ParticleType<?>> PARTICLES =  DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, PVZMod.MODID);


    public static final RegistryObject<SimpleParticleType> SUN = particle("sun");
    public static final RegistryObject<SimpleParticleType> FOG = particle("fog");
    public static final RegistryObject<SimpleParticleType> MASHED_POTATO = particle("mashed_potato");
    public static final RegistryObject<SimpleParticleType> Z = particle("z");



    public static RegistryObject<SimpleParticleType> particle(String name) {
        return PARTICLES.register(name, () -> new SimpleParticleType(false));
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void registerParticles(RegisterParticleProvidersEvent event) {
        Map<RegistryObject<SimpleParticleType>, ParticleEngine.SpriteParticleRegistration<SimpleParticleType>> particleMap = new HashMap<>();
        particleMap.put(SUN, SunParticle.Provider::new);
        particleMap.put(FOG, FogParticle.Provider::new);
        particleMap.put(MASHED_POTATO, MashedPotatoParticle.Provider::new);
        particleMap.put(Z, ZParticle.Provider::new);
        for (RegistryObject<SimpleParticleType> key : particleMap.keySet()){
            event.register(key.get(), particleMap.get(key));
        }
    }

}
