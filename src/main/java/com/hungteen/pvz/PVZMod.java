package com.hungteen.pvz;

import com.hungteen.pvz.client.PVZKeyBindings;
import com.hungteen.pvz.client.gui.PVZOverlayHandler;
import com.hungteen.pvz.client.gui.screens.AlmanacScreen;
import com.hungteen.pvz.client.gui.screens.EssenceAltarScreen;
import com.hungteen.pvz.client.renderer.PVZLayerHandler;
import com.hungteen.pvz.common.capability.CapabilityHandler;
import com.hungteen.pvz.common.capability.entity.PVZEntityCapability;
import com.hungteen.pvz.common.capability.level.PVZFogCapability;
import com.hungteen.pvz.common.capability.level.PVZZombieEventCapability;
import com.hungteen.pvz.common.capability.player.PVZPlayerCapability;
import com.hungteen.pvz.common.command.*;
import com.hungteen.pvz.common.event.RegisterSproutsEvent;
import com.hungteen.pvz.common.network.ClientProxy;
import com.hungteen.pvz.common.network.CommonProxy;
import com.hungteen.pvz.common.network.PVZPacketHandler;
import com.hungteen.pvz.common.register.*;
import com.hungteen.pvz.common.world.PVZFog;
import com.hungteen.pvz.common.world.PVZSavedData;
import com.hungteen.pvz.common.world.PVZWorldEvents;
import com.hungteen.pvz.generator.DataGenHandler;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.logging.LogUtils;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import java.util.HashMap;

@Mod(PVZMod.MODID)
public class PVZMod
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "pvz";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    public static String ENEMY_TEAM = "team.pvz.enemy_team";
    public static String FRIENDLY_TEAM = "team.pvz.friendly_team";
    public static CommonProxy PROXY = DistExecutor.unsafeRunForDist(() -> ClientProxy::new, () -> CommonProxy::new);
    public static float clientTime = 0;
    public static int serverAverageTickTime = 0;

    public PVZMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::commonSetup);
        modBus.addListener(EventPriority.NORMAL, DataGenHandler::dataGen);

        PVZEntities.ENTITIES.register(modBus);
        modBus.addListener(EventPriority.NORMAL, PVZEntities::addEntityAttributes);
        modBus.addListener(EventPriority.NORMAL, PVZEntities::addSummonRules);
        modBus.addListener(EventPriority.NORMAL, CapabilityHandler::registerCapabilities);
        PVZMobEffects.EFFECTS.register(modBus);
        PVZMobEffects.POTIONS.register(modBus);
        PVZAttributes.ATTRIBUTE.register(modBus);
        modBus.addListener(PVZAttributes::addAttributes);

        PVZBiomeModifier.BIOME_MODIFIER.register(modBus);

        PVZItems.ITEMS.register(modBus);
        PVZEnchantments.ENCHANTMENTS.register(modBus);

        PVZBlocks.BLOCKS.register(modBus);
        PVZBannerPatterns.BANNERS.register(modBus);
        PVZBlockEntities.BLOCK_ENTITIES.register(modBus);

        PVZBiomes.BIOMES.register(modBus);
        PVZFeatures.FEATURES.register(modBus);
        PVZStructures.register(modBus);

        PVZParticles.PARTICLES.register(modBus);
        PVZSoundEvents.SOUNDS.register(modBus);

        PVZMenus.MENU_TYPES.register(modBus);

        PVZLootModifiers.LOOT_MODIFIERS.register(modBus);

        PVZZombieEvents.ZOMBIE_EVENTS.register(modBus);
        PVZStats.STATS.register(modBus);
        OtherRegisters.modBusRegister(modBus);
        modBus.addListener(PVZConfig.PVZGameRules::init);

        modBus.addListener(EventPriority.NORMAL, OtherRegisters::essenceFurnaceRecipeBookRegister);


        IEventBus forgeBus = MinecraftForge.EVENT_BUS;
        forgeBus.addGenericListener(Entity.class, CapabilityHandler::attachEntityCaps);
        forgeBus.addGenericListener(Level.class, CapabilityHandler::attachLevelCaps);
        forgeBus.addListener(PVZMod::registerCommands);
        forgeBus.addListener(PVZMod::onServerTick);
        forgeBus.addListener(PVZMod::onClientTick);
        PVZConfig.init();

        PROXY.addClientListeners(modBus, forgeBus);

        forgeBus.register(this);
    }


    private void commonSetup(final FMLCommonSetupEvent event)
    {

        PVZDimensions.register();
        PVZEnchantments.handleEnchantmentTypes();
        PVZSeedPackets.sortAndClear();

        event.enqueueWork(() -> {
            PVZBlocks.flammableMap.forEach((blockObj, pair) ->
                    ((FireBlock) Blocks.FIRE).setFlammable(blockObj.get(), pair.getFirst(), pair.getSecond())
            );
            PVZItems.composterMap.forEach((itemObj, chance) ->
                    ComposterBlock.add(chance, (ItemLike) itemObj.get())
            );
            ComposterBlock.add(0.1f, Items.ROTTEN_FLESH);
                    PVZBlocks.woodList.forEach((map) -> {
                AxeItem.STRIPPABLES = new HashMap<>(AxeItem.STRIPPABLES);
                AxeItem.STRIPPABLES.put(map.get(PVZBlocks.WoodSet.Log).get(), map.get(PVZBlocks.WoodSet.StLog).get());
                AxeItem.STRIPPABLES.put(map.get(PVZBlocks.WoodSet.Wood).get(), map.get(PVZBlocks.WoodSet.StWood).get());
            });
            PVZMobEffects.addMixs();
            PVZWorldEvents.commonBootstrap();
            //clear variables
            PVZBlocks.release();
            PVZItems.release();
            PVZEntities.release();
        });
        RegisterSproutsEvent sproutEvent = new RegisterSproutsEvent();
        MinecraftForge.EVENT_BUS.post(sproutEvent);

        //network
        PVZPacketHandler.init();
        PVZCriteriaTriggers.init();
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        PVZBiomes.checkFeatures();
        PVZWorldEvents.serverBootstrap();
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            event.enqueueWork(() -> {
                //register sign materials
                PVZBlocks.woodTypeList.forEach(Sheets::addWoodType);

                //registerScreens
                PVZMenus.registerScreens();
            });
            PVZItems.registerProperties();
            PVZKeyBindings.init();

            //clear variables
            PVZParticles.particleMap.clear();
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
        @SubscribeEvent
        public static void onRegisterParticles(RegisterParticleProvidersEvent event) {
            PVZParticles.registerParticles(event);
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent ev) {
        if (ev.phase == TickEvent.Phase.START) {
            //global playerTeam
            Scoreboard scoreboard = ev.getServer().getScoreboard();
            if (scoreboard.getPlayerTeam(ENEMY_TEAM) == null) {
                PlayerTeam playerteam = scoreboard.addPlayerTeam(ENEMY_TEAM);
                playerteam.setDisplayName(Component.translatable(ENEMY_TEAM));
                PVZSavedData.setEvil(ev.getServer().getScoreboard(), ENEMY_TEAM, true);
            }
            if (scoreboard.getPlayerTeam(FRIENDLY_TEAM) == null) {
                PlayerTeam playerteam = scoreboard.addPlayerTeam(FRIENDLY_TEAM);
                playerteam.setDisplayName(Component.translatable(FRIENDLY_TEAM));
            }
            //caps tick
            PVZPlayerCapability.tick(ev);
            PVZEntityCapability.tick(ev);
            PVZFogCapability.tick(ev);
            PVZZombieEventCapability.tick(ev);
            PVZWorldEvents.tick(ev);
            //scoreboard tick
            PVZSavedData.tick();
            //server stress releasing
            serverAverageTickTime = Math.round(ev.getServer().getAverageTickTime());
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent ev) {
        if (ev.phase == TickEvent.Phase.START) {
            //counts
            if (ClientProxy.getPlayer() != null) {
                PVZOverlayHandler.tick(0.05F);
            }
            if (! ClientProxy.MC.isPaused()) {
                clientTime += 0.05F;
                AlmanacScreen.tick ++;
                if (clientTime > 10000) {
                    clientTime -= 10000;
                }
                EssenceAltarScreen.nameRollTime += 0.05F;
                if (EssenceAltarScreen.nameRollTime > 20) {
                    EssenceAltarScreen.nameRollTime -= 20;
                }
                PVZFog.clientTick(0.05F);
            }
            //caps tick
            PVZEntityCapability.clientTick(ev);
            PVZPlayerCapability.clientTick(ev);
        }
    }

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent ev) {
        CommandDispatcher<CommandSourceStack> dispatcher = ev.getDispatcher();
        ItemCoolDownCommand.register(dispatcher, ev.getBuildContext());
        ZombieEventCommands.register(dispatcher);
        PlayerStatsCommand.register(dispatcher);
        OwnCommand.register(dispatcher);
        PVZFogCommand.register(dispatcher);
        TeamSetEvilCommand.register(dispatcher);
        ZombieEventDataCommand.register(dispatcher);
    }
}
