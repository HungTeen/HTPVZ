package com.hungteen.pvz.common.register;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.client.particle.SunParticle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class PVZParticles {
    public static Map<RegistryObject<SimpleParticleType>, ParticleEngine.SpriteParticleRegistration<SimpleParticleType>> particleMap = new HashMap<>();

    public static final DeferredRegister<ParticleType<?>> PARTICLES =  DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, PVZMod.MODID);


    public static final RegistryObject<SimpleParticleType> SUN = particle("sun", (sprite) -> new SunParticle.Provider(sprite));



    public static RegistryObject<SimpleParticleType> particle(String name, ParticleEngine.SpriteParticleRegistration<SimpleParticleType> supplier) {
        RegistryObject<SimpleParticleType> particle =  PARTICLES.register(name, () -> new SimpleParticleType(false));
        particleMap.put(particle, supplier);
        return particle;
    }

    @SubscribeEvent
    public static void registerParticles(RegisterParticleProvidersEvent event) {
        for (RegistryObject<SimpleParticleType> key : particleMap.keySet()){
            event.register(key.get(), particleMap.get(key));
        }
    }

}
