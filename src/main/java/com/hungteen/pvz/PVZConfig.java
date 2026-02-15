package com.hungteen.pvz;

import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

public class PVZConfig {
    private static Common COMMON_CONFIG;
    private static Client CLIENT_CONFIG;

    //overlay settings
    public static boolean renderSunAsNumber(){
        return Client.renderSunAsNumber.get();
    }
    public static boolean renderCoolDownValue(){
        return Client.renderCoolDownValue.get();
    }
    public static int renderSunBarX(){
        return Client.renderSunBarX.get();
    }
    public static int renderSunBarY(){
        return Client.renderSunBarY.get();
    }
    public static double renderOverlayScale(){
        return Client.renderOverlayScale.get();
    }
    public static boolean renderSeparateArmorBar(){
        return Client.renderSeparateArmorBar.get();
    }
    public static boolean renderPVZTypeInvasionBar(){
        return Client.renderPVZTypeInvasionBar.get();
    }

    //model settings
    public static boolean renderBulletAsModel(){
        return Client.renderBulletAsModel.get();
    }
    public static boolean zombieDropParts(){
        return Client.zombiesDropParts.get();
    }
    public static boolean renderButterOnHead(){
        return Client.renderButterOnHead.get();
    }
    public static boolean renderZombieStuckArrows(){
        return Client.zombieRenderStuckArrows.get();
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
        public static Map<String, ForgeConfigSpec.ConfigValue<Integer>> pvzIntRules = new HashMap<>();
        public static ForgeConfigSpec.ConfigValue<Boolean> shovelPermission;
        public static ForgeConfigSpec.ConfigValue<Boolean> sunDisappear;
        public static ForgeConfigSpec.ConfigValue<Boolean> teamBattle;
        public static ForgeConfigSpec.ConfigValue<Boolean> killWisdomTree;
        public static ForgeConfigSpec.ConfigValue<Boolean> canCanCanKelp;
        public static ForgeConfigSpec.ConfigValue<Boolean> jackInTheBoxGriefing;
        public static ForgeConfigSpec.ConfigValue<Boolean> dynamicSunRule;
        public static ForgeConfigSpec.ConfigValue<Boolean> showInvasionDetails;
        public static ForgeConfigSpec.ConfigValue<Boolean> joinDefaultTeam;
        public static ForgeConfigSpec.ConfigValue<Boolean> plantNeedsDurability;
        public static ForgeConfigSpec.ConfigValue<Boolean> dyeMarigold;
        public static ForgeConfigSpec.ConfigValue<Boolean> gardenBorder;
        public static ForgeConfigSpec.ConfigValue<Boolean> gardenForEveryOne;
        public static ForgeConfigSpec.ConfigValue<Boolean> gardenOnlySprouts;
        public static ForgeConfigSpec.ConfigValue<Integer> naturallySpawnInvasionsInterval;
        public static ForgeConfigSpec.ConfigValue<Integer> naturallySpawnSunInterval;
        public static ForgeConfigSpec.ConfigValue<Integer> naturallySpawnFallenStarInterval;
        public static ForgeConfigSpec.ConfigValue<Integer> naturallyRegainSunInterval;
        public static ForgeConfigSpec.ConfigValue<Integer> marigoldGrowTime;
        public static ForgeConfigSpec.ConfigValue<Integer> sproutGrowTime;
        public static ForgeConfigSpec.ConfigValue<Integer> invasionDifficultyFactorK;
        public static ForgeConfigSpec.ConfigValue<Integer> invasionDifficultyFactorB;
        public static ForgeConfigSpec.ConfigValue<Integer> advancedPlantExtraCostRange;
        public static ForgeConfigSpec.ConfigValue<Integer> plantDamageDatum;
        public static ForgeConfigSpec.ConfigValue<Integer> sunProductionDatum;
        public static ForgeConfigSpec.ConfigValue<Integer> plantDisappearDatum;
        public Common(ForgeConfigSpec.Builder builder) {
            builder.comment("All these configs are the default values of pvz game rules and are only effective in server.")
                    .comment("In the game you can also modify them separately for each world with /gamerule command and the rules are started with \"pvz:\".")
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
                    .comment("when on, wisdom trees wilts itself and not grows."),
                    "killWisdomTree", false);
            canCanCanKelp = add(builder
                    .translation("config.pvz.common.can_can_can_kelp")
                    .comment("when on, players can interact with Tangle Kelps using buckets to bucket them."),
                    "canCanCanKelp", true);
            jackInTheBoxGriefing = add(builder
                            .translation("config.pvz.common.jack_in_the_box_griefing")
                            .comment("when on, Jack-in-the-box breaks blocks when explode."),
                    "jackInTheBoxGriefing", true);
            dynamicSunRule = add(builder
                            .translation("config.pvz.common.dynamic_sun_rule")
                            .comment("when on, player's max sun changes dynamically based on the number of sunflowers in the surrounding area."),
                    "dynamicSunRule", true);
            plantNeedsDurability = add(builder
                            .translation("config.pvz.common.plant_needs_durability")
                            .comment("when on, when player plants a plant the seed packet it use lost 1 durability."),
                    "plantNeedsDurability", true);
            dyeMarigold = add(builder
                            .translation("config.pvz.common.dye_marigold")
                            .comment("when on, player can dye marigold with dye."),
                    "dyeMarigold", false);
            showInvasionDetails = add(builder
                            .translation("config.pvz.common.show_invasion_details")
                            .comment("when on, there will be particles and outputs showing how invasion searches positions mob can spawn."),
                    "showInvasionDetails", false);
            joinDefaultTeam = add(builder
                            .translation("config.pvz.common.join_default_team")
                            .comment("when on, players without a team will automatically join a default player team of pvz mod, to prevent sweeping damage from hurting plants."),
                    "joinDefaultTeam", true);
            gardenForEveryOne = add(builder
                            .translation("config.pvz.common.garden_for_everyone")
                            .comment("when on, pvz mod transports everyone in the same server to separated zen garden island. or players in a team will share the same zen garden."),
                    "gardenForEveryone", false);
            gardenBorder = add(builder
                            .translation("config.pvz.common.garden_border")
                            .comment("when on, pvz mod prevents players from leaving the island it is on."),
                    "gardenBorder", true);
            gardenOnlySprouts = add(builder
                            .translation("config.pvz.common.garden_only_sprouts")
                            .comment("when on, sprouts can only be planted in the Zen Garden."),
                    "gardenOnlySprouts", true);
            naturallySpawnInvasionsInterval = add(builder
                            .translation("config.pvz.common.naturally_spawn_invasions_interval")
                            .comment("invasion teams will spawn from time to time near players at this interval. set to 0 to turn off natural invasion spawn."),
                    "naturallySpawnInvasionsInterval", 300, 0, 10000);
            naturallySpawnSunInterval = add(builder
                            .translation("config.pvz.common.naturally_spawn_sun_interval")
                            .comment("sun naturally spawn by players in the sky when skylight matches condition at this interval. set to 0 to turn off natural sun spawn."),
                    "naturallySpawnSunInterval", 300, 0, 10000);
            naturallySpawnFallenStarInterval = add(builder
                            .translation("config.pvz.common.naturally_spawn_fallen_star_interval")
                            .comment("fallen stars naturally spawn by players in the sky when skylight matches condition at this interval. set to 0 to turn off natural fallen star spawn."),
                    "naturallySpawnFallenStarInterval", 1000, 0, 10000);
            naturallyRegainSunInterval = add(builder
                            .translation("config.pvz.common.naturally_regain_sun_interval")
                            .comment("players regain sun naturally at this interval. set to 0 to turn off natural sun regain."),
                    "naturallyRegainSunInterval", 60, 0, 10000);
            marigoldGrowTime = add(builder
                            .translation("config.pvz.common.marigold_grow_time")
                            .comment("ticks marigolds should stay in after being fertilized before they grow to next level."),
                    "marigoldGrowTime", 12000, 100, 1000000);
            sproutGrowTime = add(builder
                            .translation("config.pvz.common.sprout_grow_time")
                            .comment("ticks sprouts should stay in after being fertilized before they grow to next level."),
                    "sproutGrowTime", 24000, 100, 1000000);
            invasionDifficultyFactorK = add(builder
                            .translation("config.pvz.common.invasion_difficulty_factor_k")
                            .comment("the general factor about the rate difficulty of invasions grows."),
                    "invasionDifficultyFactorK", 400, 0, 1000000);
            invasionDifficultyFactorB = add(builder
                            .translation("config.pvz.common.invasion_difficulty_factor_k")
                            .comment("the general factor about the difficulty of invasions when it starts."),
                    "invasionDifficultyFactorB", 100, 0, 1000000);
            advancedPlantExtraCostRange = add(builder
                            .translation("config.pvz.common.advanced_plant_extra_cost_range")
                            .comment("when planting advanced plants, plants of the same type in this range will be included for calculation of extra cost. set to -1 to disable extra sun cost."),
                    "advancedPlantExtraCostRange", 30, -1, 500);
            plantDamageDatum = add(builder
                            .translation("config.pvz.common.plant_damage_datum")
                            .comment("gives a damage multiplier for pvz plants. the value should be the health of a common zombie."),
                    "plantDamageDatum", 20, 0, 10000);
            sunProductionDatum = add(builder
                            .translation("config.pvz.common.sun_production_datum")
                            .comment("gives a production speed multiplier for pvz plants. the value should be the production interval of sunflowers in second."),
                    "sunProductionDatum", 20, 0, 10000);
            plantDisappearDatum = add(builder
                            .translation("config.pvz.common.plant_disappear_datum")
                            .comment("probability to natural disappear of plants relative to the default value. Set to 0 to disable natural disappear."),
                    "plantDisappearDatum", 100, 0, 10000);
            builder.pop();
        }

        public ForgeConfigSpec.ConfigValue<Boolean> add(ForgeConfigSpec.Builder builder, String name, Boolean defaultValue) {
            ForgeConfigSpec.ConfigValue<Boolean> value = builder.define(name, defaultValue);
            pvzBooleanRules.put(name, value);
            return value;
        }

        public ForgeConfigSpec.ConfigValue<Integer> add(ForgeConfigSpec.Builder builder, String name, int defaultValue, int min, int max) {
            ForgeConfigSpec.ConfigValue<Integer> value = builder.defineInRange(name, defaultValue, min, max);
            pvzIntRules.put(name, value);
            return value;
        }
    }
    public static class Client {

        //overlay settings

        public static ForgeConfigSpec.BooleanValue renderPVZTypeInvasionBar;
        public static ForgeConfigSpec.BooleanValue renderSunAsNumber;
        public static ForgeConfigSpec.IntValue renderSunBarX;
        public static ForgeConfigSpec.IntValue renderSunBarY;;
        public static ForgeConfigSpec.BooleanValue renderCoolDownValue;
        public static ForgeConfigSpec.DoubleValue renderOverlayScale;
        public static ForgeConfigSpec.BooleanValue renderSeparateArmorBar;
        public static ForgeConfigSpec.BooleanValue renderBulletAsModel;
        public static ForgeConfigSpec.BooleanValue zombiesDropParts;
        public static ForgeConfigSpec.BooleanValue renderButterOnHead;
        public static ForgeConfigSpec.BooleanValue zombieRenderStuckArrows;


        public Client(ForgeConfigSpec.Builder builder){
            builder.comment("Settings about GUI rendering. Some settings activates after restarting the game.").push("Overlay Settings");
            //overlay settings
            renderOverlayScale = builder
                    .translation("config.pvz.client.render_overlay_scale")
                    .comment("control scale of displaying UI that keeps on the screen like the sun amount bar and invasion bar.")
                    .defineInRange("renderOverlayScale", 0.75, 0.1, 10);
            renderSunAsNumber = builder
                    .translation("config.pvz.client.render_sun_as_number")
                    .comment("turn on to display sun amount as number, or else display as icons.")
                    .define("renderSunAsNumber", false);
            renderSunBarX = builder
                    .translation("config.pvz.client.render_sun_bar_x")
                    .comment("control x coordinate of displaying the sun amount bar. count from the right if set negative.")
                    .defineInRange("renderSunBarX", 0, -10000, 10000);
            renderSunBarY = builder
                    .translation("config.pvz.client.render_sun_bar_y")
                    .comment("control y coordinate of displaying the sun amount bar. count from the bottom if set negative.")
                    .defineInRange("renderSunBarY", 0, -10000, 10000);
            renderCoolDownValue = builder
                    .translation("config.pvz.client.render_cool_down_value")
                    .comment("turn on to display the exact time value of cool down of your items in the hot bar.")
                    .define("renderCoolDownValue", true);
            renderSeparateArmorBar = builder
                    .translation("config.pvz.client.render_separate_armor_bar")
                    .comment("turn on to display armor amount on health bar, or else display as a single bar and hide valina armor display.")
                    .define("renderSeparateArmorBar", true);
            renderPVZTypeInvasionBar = builder
                    .translation("config.pvz.client.render_pvz_type_invasion_bar")
                    .comment("turn on to display invasion progress in the lower right corner of the screen with a zombie head.")
                    .define("renderPVZTypeInvasionBar", false);
            builder.pop();
            builder.comment("Settings about models").push("Model Settings");
            //model settings
            renderBulletAsModel = builder
                    .translation("config.pvz.client.render_bullet_as_model")
                    .comment("turn on to display bullet as 3D model, or else display as item model.")
                    .define("renderBulletAsModel", true);
            zombiesDropParts = builder
                    .translation("config.pvz.client.zombies_drop_parts")
                    .comment("when on, zombies will drop arms and heads when taking damage.")
                    .define("zombiesDropParts", true);
            renderButterOnHead = builder
                    .translation("config.pvz.client.render_butter_on_head")
                    .comment("Render butter on heads of entities. This Option can lead to some rendering bug, especially when the model of the target entity is rescaled.")
                    .define("renderButterOnHead", true);
            zombieRenderStuckArrows = builder
                    .translation("config.pvz.client.zombie_render_stuck_arrows")
                    .comment("Whether pvz zombies render stuck arrows on them when they got shoot by arrows.")
                    .define("zombieRenderStuckArrows", false);
            builder.pop();
        }
    }

    public static class PVZGameRules {
        //rules.
        public Map<String, GameRules.Key<GameRules.BooleanValue>> booleanMap;
        public Map<String, GameRules.Key<GameRules.IntegerValue>> intMap;
        public List<String> dirtyList;

        private static PVZGameRules instance;


        public PVZGameRules() {
            booleanMap = initBooleanMap();
            intMap = initIntMap();
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
        public static HashMap<String, GameRules.Key<GameRules.IntegerValue>> initIntMap() {
            HashMap<String, GameRules.Key<GameRules.IntegerValue>> map = new HashMap<>();
            for (String name : Common.pvzIntRules.keySet()) {
                GameRules.Key<GameRules.IntegerValue> key = GameRules.register(PVZMod.MODID + ":" + name, GameRules.Category.MISC, GameRules.IntegerValue.create(Common.pvzIntRules.get(name).get()));
                map.put(name, key);
            }
            return map;
        }

        public static void initRules() {
            instance = new PVZGameRules();
        }

        public static void init(final FMLLoadCompleteEvent ev) {
            PVZConfig.PVZGameRules.initRules();
        }

        public static boolean getBoolean(Level level, String name) {
            return level.getGameRules().getBoolean(instance.booleanMap.get(name));
        }
        public static boolean getBoolean(Level level, ForgeConfigSpec.ConfigValue<Boolean> value) {
            return getBoolean(level, value.getPath().get(value.getPath().size() - 1));
        }
        public static int getInt(Level level, String name) {
            return level.getGameRules().getInt(instance.intMap.get(name));
        }
        public static int getInt(Level level, ForgeConfigSpec.ConfigValue<Integer> value) {
            return getInt(level, value.getPath().get(value.getPath().size() - 1));
        }
    }
}
