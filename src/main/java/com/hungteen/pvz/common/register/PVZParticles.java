package com.hungteen.pvz.common.register;

import com.hungteen.pvz.PVZMod;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class PVZParticles {

    public static final DeferredRegister<ParticleType<?>> PARTICLES =  DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, PVZMod.MODID);

//    public static final RegistryObject<SimpleParticleType> NUT_FRAGMENT_0 = PARTICLES.register("nut_fragment_0", () -> new SimpleParticleType(false));

}
