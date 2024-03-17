package com.hungteen.pvz;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PVZConfig {
    private static Common COMMON_CONFIG;
    private static Client CLIENT_CONFIG;

    //overlay settings
    public static boolean renderSunAsNumber(){
        return Client.renderSunAsNumber.get();
    }
    public static int renderSunBarX(){
        return Client.renderSunBarX.get();
    }
    public static int renderSunBarY(){
        return Client.renderSunBarY.get();
    }
    public static double renderSunBarScale(){
        return Client.renderSunBarScale.get();
    }

    //model settings
    public static boolean renderBulletAsModel(){
        return Client.renderBulletAsModel.get();
    }
    public static boolean zombieDropParts(){
        return Client.zombiesDropParts.get();
    }

    public static void init(){
        {
            final Pair<Common, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Common::new);
            ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, specPair.getRight());
            PVZConfig.COMMON_CONFIG = specPair.getLeft();
        }
        {
            final Pair<PVZConfig.Client, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Client::new);
            ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, specPair.getRight());
            PVZConfig.CLIENT_CONFIG = specPair.getLeft();
        }
    }

    public static class Common {
        public static Map<String, ForgeConfigSpec.ConfigValue<Boolean>> pvzBooleanRules = new HashMap<>();
        public static ForgeConfigSpec.ConfigValue<Boolean> shovelPermission;
        public static ForgeConfigSpec.ConfigValue<Boolean> sunDisappear;
        public static ForgeConfigSpec.ConfigValue<Boolean> teamBattle;
        public static ForgeConfigSpec.ConfigValue<Boolean> killWisdomTree;
        public static ForgeConfigSpec.ConfigValue<Boolean> canCanCanKelp;
        public static ForgeConfigSpec.ConfigValue<Boolean> testSunRule;
        public static ForgeConfigSpec.ConfigValue<Boolean> naturallySpawnSun;
        public Common(ForgeConfigSpec.Builder builder){
            builder.comment("All these configs are the default values of pvz rules.")
                    .comment("In the game you can also modify them separately for each world with /pvzrule command.")
                    .comment("All these configs are only effective in server.")
                    .push("PVZ Rules");
            shovelPermission = add(builder
                    .translation("config.pvz.common.shovel_permission")
                    .comment("whether a player in the same team can shovel a plant if the player is NOT the owner of the plant."),
                    "shovelPermission", true);
            sunDisappear = add(builder
                    .translation("config.pvz.common.sun_disappear")
                    .comment("whether sun disappear after a while it is generated."),
                    "sunDisappear", true);
            teamBattle = add(builder
                    .translation("config.pvz.common.team_battle")
                    .comment("whether plants in different teams regard each other as enemy."),
                    "teamBattle", false);
            killWisdomTree = add(builder
                    .translation("config.pvz.common.kill_wisdom_tree")
                    .comment("if on, wisdom trees wilts itself and not grows."),
                    "killWisdomTree", false);
            canCanCanKelp = add(builder
                    .translation("config.pvz.common.can_can_can_kelp")
                    .comment("if on, players can interact with Tangle Kelps using buckets to bucket them."),
                    "canCanCanKelp", false);
            testSunRule = add(builder
                            .translation("config.pvz.common.dynamic_sun_rule")
                            .comment("if on, player's max sun changes dynamically based on the number of sunflowers in the surrounding area."),
                    "dynamicSunRule", false);
            naturallySpawnSun = add(builder
                            .translation("config.pvz.common.naturally_spawn_sun")
                            .comment("if on, sun will naturally spawn by players in the sky when skylight matches the condition."),
                    "naturallySpawnSun", true);
            builder.pop();
        }

        public ForgeConfigSpec.ConfigValue<Boolean> add(ForgeConfigSpec.Builder builder, String name, Boolean defaultValue) {
            ForgeConfigSpec.ConfigValue<Boolean> value = builder.define(name, defaultValue);
            pvzBooleanRules.put(name, value);
            return value;
        }
    }
    public static class Client {

        //overlay settings

        public static ForgeConfigSpec.BooleanValue renderSunAsNumber;
        public static ForgeConfigSpec.IntValue renderSunBarX;
        public static ForgeConfigSpec.IntValue renderSunBarY;
        public static ForgeConfigSpec.DoubleValue renderSunBarScale;
        public static ForgeConfigSpec.BooleanValue renderBulletAsModel;
        public static ForgeConfigSpec.BooleanValue zombiesDropParts;


        public Client(ForgeConfigSpec.Builder builder){
            builder.comment("Settings about GUI rendering").push("Overlay Settings");
            //overlay settings
            renderSunAsNumber = builder
                    .translation("config.pvz.client.render_sun_as_number")
                    .comment("turn on to display sun amount as number, or else display as icons.")
                    .define("RenderSunAsNumber", false);
            renderSunBarX = builder
                    .translation("config.pvz.client.render_sun_bar_x")
                    .comment("control x coordinate of displaying the sun amount bar. count from the right if set negative.")
                    .defineInRange("renderSunBarX", 0, -10000, 10000);
            renderSunBarY = builder
                    .translation("config.pvz.client.render_sun_bar_y")
                    .comment("control y coordinate of displaying the sun amount bar. count from the bottom if set negative.")
                    .defineInRange("renderSunBarY", 0, -10000, 10000);
            renderSunBarScale = builder
                    .translation("config.pvz.client.render_sun_bar_scale")
                    .comment("control scale of displaying the sun amount bar.")
                    .defineInRange("renderSunBarScale", 0.75, 0.1, 10);
            builder.pop();
            builder.comment("Settings about models").push("Model Settings");
            //model settings
            renderBulletAsModel = builder
                    .translation("config.pvz.client.render_bullet_as_model")
                    .comment("turn on to display bullet as 3D model, or else display as item model.")
                    .define("RenderBulletAsModel", true);
            zombiesDropParts = builder
                    .translation("config.pvz.client.zombies_drop_parts")
                    .comment("if on, zombies will drop arms and heads when taking damage.")
                    .define("ZombiesDropParts", true);
            builder.pop();
        }
    }
    @Mod.EventBusSubscriber(modid = PVZMod.MODID)
    public static class PVZGameRules {
        //rules.
        public Map<String, GameRules.Key<GameRules.BooleanValue>> booleanMap;
        public List<String> dirtyList;

        private static PVZGameRules instance;


        public PVZGameRules() {
            booleanMap = initBooleanMap();
            dirtyList = new ArrayList<>();
        }

        public static HashMap<String, GameRules.Key<GameRules.BooleanValue>> initBooleanMap() {
            HashMap<String, GameRules.Key<GameRules.BooleanValue>> map = new HashMap<>();
            for (String name : Common.pvzBooleanRules.keySet()) {
                GameRules.Key<GameRules.BooleanValue> key = GameRules.register(PVZMod.MODID + ":" + name, GameRules.Category.MISC, GameRules.BooleanValue.create(Common.pvzBooleanRules.get(name).get()));
                map.put(name, key);
            }
            return map;
        }
        public static PVZGameRules createInstance() {
            instance = new PVZGameRules();
            return instance;
        }

        @SubscribeEvent
        public static void init(LevelEvent.Load ev) {
            PVZGameRules pvzRules = instance == null ? PVZGameRules.createInstance() : instance;
            if (ev.getLevel() instanceof ServerLevel level && level.getLevelData() instanceof PrimaryLevelData data) {
                data.getGameRules().rules = pvzRules.loadMissingRules(data.getGameRules());
            }
        }

        public Map<GameRules.Key<?>, GameRules.Value<?>> loadMissingRules(GameRules rules) {
            HashMap<GameRules.Key<?>, GameRules.Value<?>> ruleMap = new HashMap<>(rules.rules);
            for (Map.Entry<GameRules.Key<?>, GameRules.Type<?>> pair: GameRules.GAME_RULE_TYPES.entrySet()) {
                if (! ruleMap.containsKey(pair.getKey())) {
                    ruleMap.put(pair.getKey(), pair.getValue().createRule());
                }
            }
            return ImmutableMap.copyOf(ruleMap);
        }

        public static boolean getBoolean(Level level, String name) {
            return level.getGameRules().getBoolean(instance.booleanMap.get(name));
        }
    }
}
