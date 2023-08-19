package com.hungteen.pvz;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;

public class PVZConfig {
    private static Common COMMON_CONFIG;
    private static Client CLIENT_CONFIG;

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
        public Common(ForgeConfigSpec.Builder builder){

        }
    }
    public static class Client {

        //overlay settings

        public static ForgeConfigSpec.BooleanValue renderSunAsBar;
        public static ForgeConfigSpec.IntValue renderSunBarX;
        public static ForgeConfigSpec.IntValue renderSunBarY;
        public static ForgeConfigSpec.DoubleValue renderSunBarScale;


        public Client(ForgeConfigSpec.Builder builder){
            builder.comment("Overlay Settings").push("Overlay Settings");
            //overlay settings
            renderSunAsBar = builder
                    .comment("turn on to display sun amount as a bar.")
                    .translation("config.pvz.client.render_sun_as_bar")
                    .define("RenderSunAsBar", true);
            renderSunBarX = builder
                    .translation("config.pvz.client.render_sun_bar_x")
                    .comment("control x coordinate of displaying the sun amount bar. count from the right if set negative.")
                    .defineInRange("renderSunBarX", 0, -4000, 4000);
            renderSunBarY = builder
                    .translation("config.pvz.client.render_sun_bar_y")
                    .comment("control y coordinate of displaying the sun amount bar. count from the bottom if set negative.")
                    .defineInRange("renderSunBarY", 0, -4000, 4000);
            renderSunBarScale = builder
                    .translation("config.pvz.client.render_sun_bar_scale")
                    .comment("control scale of displaying the sun amount bar.")
                    .defineInRange("renderSunBarScale", 0.67, 0.1, 10);

        }
    }
    //overlay settings
    public static boolean renderSunAsBar(){
        return Client.renderSunAsBar.get();
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
}
