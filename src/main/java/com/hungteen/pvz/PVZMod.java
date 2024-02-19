package com.hungteen.pvz;

import com.hungteen.pvz.client.gui.PVZOverlayHandler;
import com.hungteen.pvz.client.gui.components.ClientSunImageToolTipComponent;
import com.hungteen.pvz.client.gui.screens.EssenceAltarScreen;
import com.hungteen.pvz.client.renderer.PVZLayerHandler;
import com.hungteen.pvz.client.renderer.blockentity.EssenceAltarRenderer;
import com.hungteen.pvz.common.capability.CapabilityHandler;
import com.hungteen.pvz.common.capability.owned.PVZOwnedCapability;
import com.hungteen.pvz.common.capability.player.PVZPlayerCapability;
import com.hungteen.pvz.common.command.OwnCommand;
import com.hungteen.pvz.common.command.PVZFogCommand;
import com.hungteen.pvz.common.command.PVZRulesCommand;
import com.hungteen.pvz.common.command.PlayerStatsCommand;
import com.hungteen.pvz.common.entity.ai.goal.ServerStressReleaseGoals;
import com.hungteen.pvz.common.network.ClientProxy;
import com.hungteen.pvz.common.network.CommonProxy;
import com.hungteen.pvz.common.network.PVZPacketHandler;
import com.hungteen.pvz.common.register.*;
import com.hungteen.pvz.common.world.PVZFog;
import com.hungteen.pvz.common.world.zen_garden.ZenGardenEffects;
import com.hungteen.pvz.generator.DataGenHandler;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
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
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.slf4j.Logger;

import java.util.HashMap;

@Mod(PVZMod.MODID)
public class PVZMod
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "pvz";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    public static String PLAYER_TEAM = "pvzmod.playerTeam";
    public static String ENEMY_TEAM = "pvzmod.enemyTeam";
    public static CommonProxy PROXY = DistExecutor.unsafeRunForDist(() -> ClientProxy::new, () -> CommonProxy::new);
    public PVZMod()
    {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::commonSetup);
        modBus.addListener(EventPriority.NORMAL, DataGenHandler::dataGen);

        PVZEntities.ENTITIES.register(modBus);
        modBus.addListener(EventPriority.NORMAL, PVZEntities::addEntityAttributes);
        modBus.addListener(EventPriority.NORMAL, PVZEntities::addSummonRules);
        modBus.addListener(EventPriority.NORMAL, CapabilityHandler::registerCapabilities);
        PVZMobEffects.EFFECTS.register(modBus);

        PVZBiomeModifier.BIOME_MODIFIER.register(modBus);

        PVZItems.ITEMS.register(modBus);
        PVZEnchantments.ENCHANTMENTS.register(modBus);

        PVZBlocks.BLOCKS.register(modBus);
        PVZBlockEntities.BLOCK_ENTITIES.register(modBus);

        PVZBiomes.BIOMES.register(modBus);
        PVZFeatures.FEATURES.register(modBus);
        PVZParticles.PARTICLES.register(modBus);

        PVZMenus.MENU_TYPES.register(modBus);

        OtherRegisters.modBusRegister(modBus);
        modBus.addListener(EventPriority.NORMAL, OtherRegisters::essenceFurnaceRecipeBookRegister);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            modBus.addListener(PVZOverlayHandler::registerOverlay);
            modBus.addListener(ClientSunImageToolTipComponent::register);
            modBus.addListener(ZenGardenEffects::register);
        }


        IEventBus forgeBus = MinecraftForge.EVENT_BUS;
        forgeBus.addGenericListener(Entity.class, CapabilityHandler::attachCapabilities);
        forgeBus.addGenericListener(Level.class, CapabilityHandler::initPVZRules);
        forgeBus.addListener(PVZMod::registerCommands);
        forgeBus.addListener(PVZMod::onServerTick);
        forgeBus.addListener(PVZMod::onRenderTick);
        PVZConfig.init();

        forgeBus.register(this);
    }





    private void commonSetup(final FMLCommonSetupEvent event)
    {
        LOGGER.info("----------COMMON SETUP----------");

        PVZDimensions.register();
        PVZEnchantments.handleEnchantmentTypes();

        event.enqueueWork(() ->{
            PVZBlocks.flammableMap.forEach((blockObj, pair) ->
                    ((FireBlock) Blocks.FIRE).setFlammable(blockObj.get(), pair.getFirst(), pair.getSecond())
            );
            PVZBlocks.woodList.forEach((map) -> {
                AxeItem.STRIPPABLES = new HashMap<>(AxeItem.STRIPPABLES);
                AxeItem.STRIPPABLES.put(map.get(PVZBlocks.WoodSet.Log).get(), map.get(PVZBlocks.WoodSet.StLog).get());
                AxeItem.STRIPPABLES.put(map.get(PVZBlocks.WoodSet.Wood).get(), map.get(PVZBlocks.WoodSet.StWood).get());
            });
            PVZBlocks.queueRelease();
        });

        //clear variables
        PVZBlocks.release();
        PVZItems.release();
        PVZEntities.release();

        //network
        PVZPacketHandler.init();
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        LOGGER.info("----------server starting----------");

        PVZBiomes.checkFeatures();
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            LOGGER.info("----------CLIENT SETUP----------");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());

            event.enqueueWork(() -> {
                //register sign materials
                PVZBlocks.woodTypeList.forEach(Sheets::addWoodType);

                //registerScreens
                PVZMenus.registerScreens();
            });

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
            if (scoreboard.getPlayerTeam(PLAYER_TEAM) == null) {
                PlayerTeam playerteam = scoreboard.addPlayerTeam(PLAYER_TEAM);
                playerteam.setDisplayName(Component.literal(PLAYER_TEAM));
            }
            if (scoreboard.getPlayerTeam(ENEMY_TEAM) == null) {
                PlayerTeam playerteam = scoreboard.addPlayerTeam(ENEMY_TEAM);
                playerteam.setDisplayName(Component.literal(ENEMY_TEAM));
            }
            //caps tick
            PVZPlayerCapability.tick(ev);
            PVZOwnedCapability.tick(ev);
            //server stress releasing
            ServerStressReleaseGoals.averageTickTime = Math.round(ev.getServer().getAverageTickTime());
        }
    }

    @SubscribeEvent
    public static void onRenderTick(TickEvent.RenderTickEvent ev) {
        if (ClientProxy.getPlayer() != null){
            PVZOverlayHandler.tick(ev.renderTickTime);
        }
        EssenceAltarRenderer.time += Minecraft.getInstance().isPaused() ? 0 : ev.renderTickTime;
        if (EssenceAltarRenderer.time > 1500) {
            EssenceAltarRenderer.time -= 1500;
        }
        EssenceAltarScreen.nameRollTime += ev.renderTickTime;
        if (EssenceAltarScreen.nameRollTime > 400) {
            EssenceAltarScreen.nameRollTime -= 400;
        }
        if (! Minecraft.getInstance().isPaused()) {
            PVZFog.fogsTick(ev.renderTickTime);
        }
    }

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent ev){
        CommandDispatcher<CommandSourceStack> dispatcher = ev.getDispatcher();
        PlayerStatsCommand.register(dispatcher);
        OwnCommand.register(dispatcher);
        PVZRulesCommand.register(dispatcher);
        PVZFogCommand.register(dispatcher);
    }
}
