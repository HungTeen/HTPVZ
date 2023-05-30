package com.hungteen.pvz;

import com.hungteen.pvz.client.renderer.PVZLayerHandler;
import com.hungteen.pvz.common.register.*;
import com.hungteen.pvz.generator.DataGenHandler;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import java.util.HashMap;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(PVZMod.MODID)
public class PVZMod
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "pvz";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    public PVZMod()
    {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::commonSetup);
        modBus.addListener(EventPriority.NORMAL, DataGenHandler::dataGen);
        modBus.addListener(EventPriority.NORMAL, PVZEntities::addEntityAttributes);
        PVZItems.ITEMS.register(modBus);
        PVZBlocks.BLOCKS.register(modBus);
        PVZEntities.ENTITIES.register(modBus);
        PVZBlockEntities.BLOCK_ENTITIES.register(modBus);

        OtherRegisters.register(modBus);


        IEventBus forgeBus = MinecraftForge.EVENT_BUS;
        forgeBus.register(this);
    }





    private void commonSetup(final FMLCommonSetupEvent event)
    {
        // Some common setup code
        LOGGER.info("----------COMMON SETUP----------");
//        LOGGER.info("DIRT BLOCK >> {}", ForgeRegistries.BLOCKS.getKey(Blocks.DIRT));

        event.enqueueWork(() ->{
            PVZBlocks.flammableMap.forEach((blockObj, pair) ->
                    ((FireBlock) Blocks.FIRE).setFlammable(blockObj.get(), pair.getFirst(), pair.getSecond())
            );
            PVZBlocks.woodList.forEach((map) -> {
                AxeItem.STRIPPABLES = new HashMap<>(AxeItem.STRIPPABLES);
                AxeItem.STRIPPABLES.put(map.get(PVZBlocks.WoodSet.Log).get(), map.get(PVZBlocks.WoodSet.StLog).get());
                AxeItem.STRIPPABLES.put(map.get(PVZBlocks.WoodSet.Wood).get(), map.get(PVZBlocks.WoodSet.StWood).get());
            });
        });
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        // Do something when the server starts
        LOGGER.info("----------server starting----------");
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            LOGGER.info("----------CLIENT SETUP----------");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        //blocks
            event.enqueueWork(() ->
                    PVZBlocks.woodTypeList.forEach(Sheets::addWoodType)
            );
        }

        //entities & blockEntities
        @SubscribeEvent
        public static void onRegisterLayers(EntityRenderersEvent.RegisterLayerDefinitions event){
            PVZLayerHandler.createModelDefinitions(event);
        }
        @SubscribeEvent
        public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
            PVZBlockEntities.registerRenderer(event);
            PVZEntities.registerRenderer(event);
        }
    }
}
