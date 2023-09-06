package com.hungteen.pvz.client.gui;

import com.hungteen.pvz.PVZConfig;
import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.Util;
import com.hungteen.pvz.common.capability.player.PVZPlayerCapNBT;
import com.hungteen.pvz.common.capability.player.PVZPlayerCapability;
import com.hungteen.pvz.common.network.ClientProxy;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Objects;
import java.util.Optional;
import java.util.Random;


@Mod.EventBusSubscriber(modid = PVZMod.MODID, value = Dist.CLIENT)
@OnlyIn(Dist.CLIENT)
public class PVZOverlayHandler{

    private static int bufferSunAmount = 0;
    private static int bufferSunBarLength = 0;
    private static Random random = new Random();
    public static float notEnoughHint = 0;

    @SubscribeEvent
    public static void onPostRenderOverlay(RenderGuiOverlayEvent.Post ev){
        if(!ClientProxy.MC.options.hideGui && ClientProxy.MC.screen == null && ClientProxy.MC.player != null && ! ClientProxy.MC.player.isSpectator()){
            if(!ClientProxy.MC.options.renderDebug && PVZConfig.renderSunAsBar()){
                // render sun as bar on the screen
                PVZOverlayHandler.renderSunAsBar(ev.getPoseStack(), ev.getWindow().getGuiScaledWidth(), ev.getWindow().getGuiScaledHeight());
            }
        }
    }

    public static void tick(float tickTime){
        if (PVZPlayerCapability.getPlayerData(ClientProxy.getPlayer()).isPresent()) {
            float speed = 0.1F;//TODO count animation with time?
            int now = PVZPlayerCapability.getValue(ClientProxy.getPlayer(),  PVZPlayerCapNBT.SUN);
            int barLength = 94*bufferSunAmount/PVZPlayerCapability.getValueLimit(ClientProxy.getPlayer(), PVZPlayerCapNBT.SUN).getSecond();
            bufferSunAmount = (int)(now*speed + bufferSunAmount*(1-speed));
            bufferSunBarLength = (int)(barLength*speed + bufferSunBarLength*(1-speed));
            if (Math.abs(bufferSunAmount - now) <= 10) {
                bufferSunAmount = now;
            }
            if (Math.abs(bufferSunBarLength - barLength) <= 10) {
                bufferSunBarLength = barLength;
            }
            if (notEnoughHint > 0) {
                notEnoughHint -= tickTime / 10;
                PVZMod.LOGGER.info(tickTime + " || " + notEnoughHint);
            }
        }
    }

    private static void renderSunAsBar(PoseStack stack, int width, int height){
        stack.pushPose();
        RenderSystem.enableBlend();
        int x = PVZConfig.renderSunBarX();
        int y = PVZConfig.renderSunBarY();
        double scale = PVZConfig.renderSunBarScale();

        stack.scale((float) (0.5F*scale), (float) (0.5F*scale), (float) (0.5F*scale));
        Util.setTexture(Util.prefix("textures/gui/overlay/icons.png"));

        int drawX = x >= 0 ? x + 35 : (int) (width/scale) - 134 + x;
        int drawY = y >= 0 ? y : (int) (height/scale) - 15 + y;
        Util.GuiBiltScaled(stack, drawX, drawY, 28, 112, 100, 16, 2);
        Util.GuiBiltScaled(stack, drawX + 3, drawY, 31, 96, bufferSunBarLength, 16, 2);
        Util.GuiBiltScaled(stack, x >= 0 ? x : (int) (width/scale) - 33 + x, y >= 0 ? y : (int) (height/scale) - 33 + y, 94, 62, 34, 34, 2);


        if ((notEnoughHint * 1.5) % 2 >= 1) {
            Util.drawCenteredScaledString(stack, ClientProxy.MC.font, bufferSunAmount + "", (x >= 0 ? x + 86 : (int) (width / scale) - 84 + x) * 2, (y >= 0 ? y + 5 : (int) (height / scale) - 11 + y) * 2, 0xEF1010, 2f);
        } else {
            Util.drawCenteredScaledString(stack, ClientProxy.MC.font, bufferSunAmount + "", (x >= 0 ? x + 86 : (int) (width / scale) - 84 + x) * 2, (y >= 0 ? y + 5 : (int) (height / scale) - 11 + y) * 2, 0x663600, 2f);
        }
        Util.drawCenteredScaledString(stack, ClientProxy.MC.font, bufferSunAmount + "", (x >= 0 ? x + 85 : (int) (width/scale) - 85 + x)*2, (y >= 0 ? y + 4 : (int) (height/scale) - 12 + y)*2, 0xFFFFFF, 2f);

        RenderSystem.disableBlend();
        stack.popPose();
        stack.scale(1, 1, 1);
    }

    private static void renderSunAsStats(ForgeGui gui, PoseStack stack, float partialTick, int width, int height){
        if (!PVZConfig.renderSunAsBar() && !gui.getMinecraft().options.hideGui && gui.shouldDrawSurvivalElements()) {
            Minecraft mc = gui.getMinecraft();
            mc.getProfiler().push("sun");

            Player player = (Player) ClientProxy.MC.getCameraEntity();
            Util.setTexture(Util.prefix("textures/gui/overlay/icons.png"));
            RenderSystem.enableBlend();

            int left = width / 2 + 91;
            int top = height - gui.rightHeight;
            gui.rightHeight += 10;

            int levelShow = bufferSunAmount;
            int levelMax = PVZPlayerCapability.getValueLimit(player, PVZPlayerCapNBT.SUN).getSecond();
            int levelActual = PVZPlayerCapability.getValue(player, PVZPlayerCapNBT.SUN);

            for (int i = 0; i < (Math.min(levelMax / 100, 10)); ++i) {
                int x = left - i * 8 - 9;
                int y = top;
                int icon = player.hasEffect(MobEffects.DARKNESS) ? 10 : 0;

                if (gui.getGuiTicks() % 10 <= 3 && (levelActual <= 0 || icon == 10)) {
                    y = top + (random.nextInt(3) - 1);
                }

                if (levelActual > 1000 && levelShow > 1000) {
                    int idx = 1000 + i * 200 - 1;
                    if (idx + 200 < levelShow) {
                        blit(stack, x, y, 80, icon, 9, 9);
                    } else if (idx + 150 < levelShow) {
                        blit(stack, x, y, 70, icon, 9, 9);
                    } else if (idx + 100 < levelShow) {
                        blit(stack, x, y, 60, icon, 9, 9);
                    } else if (idx + 50 < levelShow) {
                        blit(stack, x, y, 50, icon, 9, 9);
                    } else {
                        blit(stack, x, y, 40, icon, 9, 9);
                    }
                } else {
                    int idx = i * 100 - 1;
                    if (idx + 100 < levelShow) {
                        blit(stack, x, y, 40, icon, 9, 9);
                    } else if (idx + 75 < levelShow) {
                        blit(stack, x, y, 30, icon, 9, 9);
                    } else if (idx + 50 < levelShow) {
                        blit(stack, x, y, 20, icon, 9, 9);
                    } else if (idx + 25 < levelShow) {
                        blit(stack, x, y, 10, icon, 9, 9);
                    } else {
                        blit(stack, x, y, 0, icon, 9, 9);
                    }
                }

                if (icon == 10) {
                    int tmp = ((gui.getGuiTicks() + i * 4) % 30) / 2 - 12;
                    if (tmp >= 0) {
                        blit(stack, x, y, 10 * tmp, 20, 9, 9);
                    }
                }
                if (levelActual != levelShow) {
                    blit(stack, x, y, 40, 20, 9, 9);
                }
                if ((notEnoughHint * 1.5) % 2 >= 1) {
                    blit(stack, x, y, 50, 20, 9, 9);
                }

            }
            RenderSystem.disableBlend();
            mc.getProfiler().pop();
        }
    }

    public static void blit(PoseStack stack,int x, int y, float u, float v, int width, int height) {
        GuiComponent.blit(stack,x,y,0,u,v,width,height,128,128);
    }

    @SubscribeEvent
    public static void registerOverlay(RegisterGuiOverlaysEvent ev){
        ev.registerBelow(VanillaGuiOverlay.AIR_LEVEL.id(), "sun_level", PVZOverlayHandler::renderSunAsStats);
    }

}
