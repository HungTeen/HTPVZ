package com.hungteen.pvz;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
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
        public static Map<String, ForgeConfigSpec.ConfigValue<Boolean>> pvzRules = new HashMap<>();
        public static ForgeConfigSpec.ConfigValue<Boolean> shovelPermission;
        public static ForgeConfigSpec.ConfigValue<Boolean> sunDisappear;
        public static ForgeConfigSpec.ConfigValue<Boolean> teamBattle;
        public static ForgeConfigSpec.ConfigValue<Boolean> killWisdomTree;
        public static ForgeConfigSpec.ConfigValue<Boolean> canCanCanKelp;
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
            builder.pop();
        }

        public ForgeConfigSpec.ConfigValue<Boolean> add(ForgeConfigSpec.Builder builder, String name, Boolean defaultValue) {
            ForgeConfigSpec.ConfigValue<Boolean> value = builder.define(name, defaultValue);
            pvzRules.put(name, value);
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
}
