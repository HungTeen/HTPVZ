package com.hungteen.pvz.common.register;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.common.block.entity.PVZSignBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PVZBlockEntitys {
    //init
    public static Map<RegistryObject, BlockEntityRendererProvider> rendererMap = new HashMap<>();


    //registry
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITYS = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, PVZMod.MODID);
    public static final RegistryObject<BlockEntityType<PVZSignBlockEntity>> SIGN = bEntity("pvz_sign", PVZSignBlockEntity::new, SignRenderer::new);


    //definitions
    public static <T extends BlockEntity> RegistryObject<BlockEntityType<T>> bEntity(String name, BlockEntityType.BlockEntitySupplier<T> entityMethod, BlockEntityRendererProvider rendererMethod){
        RegistryObject<BlockEntityType<T>> entityObj = BLOCK_ENTITYS.register(name, () -> BlockEntityType.Builder.of(entityMethod, blocks(name)).build(null));
        if (rendererMethod != null){
            rendererMap.put(entityObj, rendererMethod);
        }
        return entityObj;
    }
    public static Block[] blocks(String name){
        List<Block> list = new ArrayList<>();
        if (PVZBlocks.blockEntityMap.containsKey(name)){
            for (RegistryObject<Block> i : PVZBlocks.blockEntityMap.get(name)){
                list.add(i.get());
            }
            return list.toArray(new Block[list.size()]);
        } else {
            return new Block[0];
        }
    }
}