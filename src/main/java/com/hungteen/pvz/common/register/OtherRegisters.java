package com.hungteen.pvz.common.register;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.world.GlowBerryDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class OtherRegisters {
    public static final DeferredRegister<TreeDecoratorType<?>> TREE_DECORATORS = DeferredRegister.create(ForgeRegistries.TREE_DECORATOR_TYPES, PVZMod.MODID);
    public static final RegistryObject<TreeDecoratorType<GlowBerryDecorator>> GLOW_BERRY_DECORATOR = TREE_DECORATORS.register("glow_berry", () -> new TreeDecoratorType<>(GlowBerryDecorator.CODEC));


    public static void register(IEventBus bus){
        TREE_DECORATORS.register(bus);
    }
}
