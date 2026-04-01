package com.hungteen.pvz.common.register;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.client.renderer.blockentity.EssenceAltarRenderer;
import com.hungteen.pvz.client.renderer.blockentity.SilverSwordOrnamentRenderer;
import com.hungteen.pvz.common.block.entity.*;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;

public class PVZBlockEntities {


    //registry
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, PVZMod.MODID);
    public static final RegistryObject<BlockEntityType<PVZSignBlockEntity>> SIGN = bEntity("pvz_sign", PVZSignBlockEntity::new);
    public static final RegistryObject<BlockEntityType<EssenceAltarBlockEntity>> ESSENCE_ALTAR = bEntity("essence_altar", EssenceAltarBlockEntity::new);
    public static final RegistryObject<BlockEntityType<InvasionSpawnerBlockEntity>> INVASION_SPAWNER = bEntity("invasion_spawner", InvasionSpawnerBlockEntity::new);
    public static final RegistryObject<BlockEntityType<EssenceFurnaceBlockEntity>> ESSENCE_FURNACE = bEntity("essence_furnace", EssenceFurnaceBlockEntity::new);
    public static final RegistryObject<BlockEntityType<EntityLightBlockEntity>> ENTITY_LIGHT = bEntity("entity_light", EntityLightBlockEntity::new);
    public static final RegistryObject<BlockEntityType<SilverSwordOrnamentBlockEntity>> SILVER_SWORD_SCULPTURE = bEntity("silver_sword_ornament", SilverSwordOrnamentBlockEntity::new);
    /**binding renderer at {@link PVZBlockEntities#registerRenderer(EntityRenderersEvent.RegisterRenderers)}
     <br>use {@link PVZBlocks#blockEntity(String)} to attach blockEntity to block.*/


    //definitions
    public static <T extends BlockEntity> RegistryObject<BlockEntityType<T>> bEntity(String name, BlockEntityType.BlockEntitySupplier<T> entityMethod){
        return BLOCK_ENTITIES.register(name, () -> BlockEntityType.Builder.of(entityMethod, blocks(name)).build(null));
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
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void registerRenderer(EntityRenderersEvent.RegisterRenderers e) {
        r(e, SIGN, SignRenderer::new);
        r(e, ESSENCE_ALTAR, EssenceAltarRenderer::new);
        r(e, SILVER_SWORD_SCULPTURE, SilverSwordOrnamentRenderer::new);
    }
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static <T extends BlockEntity> void r(EntityRenderersEvent.RegisterRenderers event, RegistryObject<BlockEntityType<T>> blockEntity, BlockEntityRendererProvider rendererMethod){
        event.registerBlockEntityRenderer(blockEntity.get(), rendererMethod);
    }
}