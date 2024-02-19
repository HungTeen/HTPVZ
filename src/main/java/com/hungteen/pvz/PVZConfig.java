package com.hungteen.pvz;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;

public class PVZConfig {
//    private static Common COMMON_CONFIG;
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
//        {
//            final Pair<Common, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Common::new);
//            ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, specPair.getRight());
//            PVZConfig.COMMON_CONFIG = specPair.getLeft();
//        }
        {
            final Pair<PVZConfig.Client, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Client::new);
            ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, specPair.getRight());
            PVZConfig.CLIENT_CONFIG = specPair.getLeft();
        }
    }
//
//    public static class Common {
//        public Common(ForgeConfigSpec.Builder builder){
//
//        }
//    }
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
        }
    }
}
