package com.hungteen.pvz.client.gui;

import com.hungteen.pvz.PVZConfig;
import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.common.capability.player.PVZPlayerCapNBT;
import com.hungteen.pvz.common.capability.player.PVZPlayerCapability;
import com.hungteen.pvz.common.event.PVZResourceEvent;
import com.hungteen.pvz.common.item.SeedPacketItem;
import com.hungteen.pvz.common.network.ClientProxy;
import com.hungteen.pvz.util.Util;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;

import static net.minecraft.util.Mth.ceil;


@Mod.EventBusSubscriber(modid = PVZMod.MODID, value = Dist.CLIENT)
@OnlyIn(Dist.CLIENT)
public class PVZOverlayHandler{

    private static int bufferSunAmount = 0;
    private static int bufferSunBarLength = 0;
    private static final Random random = new Random();
    public static float notEnoughHint = 0;
    public static ItemStack storedItemStack = null;
    public static boolean itemStackResourceIsSun = true;
    public static int itemStackCost = 0;
    public static boolean storedHaveCost = false;

    public static void tick(float tickTime){
        if (PVZPlayerCapability.getPlayerData(ClientProxy.getPlayer()).isPresent()) {
            float second = tickTime / 10;// accurate passed time in second.
            double tmp = Math.pow(0.9, second/0.04);
            int now = PVZPlayerCapability.getValue(ClientProxy.getPlayer(),  PVZPlayerCapNBT.SUN);
            int barLength = 94*bufferSunAmount/PVZPlayerCapability.getValueLimit(ClientProxy.getPlayer(), PVZPlayerCapNBT.SUN).getSecond();
            bufferSunAmount = (int)(now * (1 - tmp) + tmp * bufferSunAmount);
            bufferSunBarLength = (int)((barLength * (1 - tmp) + tmp * bufferSunBarLength));
            if (bufferSunAmount != now && Math.abs(bufferSunAmount - now) <= 10) {
                bufferSunAmount = now;
            }
            if (bufferSunBarLength != barLength && Math.abs(bufferSunBarLength - barLength) <= 10) {
                bufferSunBarLength = barLength;
            }
            if (notEnoughHint > 0) {
                notEnoughHint -= second;
                PVZMod.LOGGER.info(tickTime + " || " + notEnoughHint);
            }
        }
        ItemStack itemStack = getCameraPlayer() == null ? null : getCameraPlayer().getItemInHand(InteractionHand.MAIN_HAND);
        if ((itemStack != storedItemStack || (getCameraPlayer() != null && (PVZPlayerCapability.getValue(getCameraPlayer(), "plant_have_cost") == 1) != storedHaveCost)) &&
                itemStack != null && itemStack.getItem() instanceof SeedPacketItem<?>) {
            refreshItemStack(getCameraPlayer(), itemStack);
            storedHaveCost = (PVZPlayerCapability.getValue(getCameraPlayer(), "plant_have_cost") == 1);
        }
        storedItemStack = itemStack;
    }

    private static void renderSunAsBar(ForgeGui gui, PoseStack stack, float partialTick, int width, int height){
        if (PVZConfig.renderSunAsBar() && !gui.getMinecraft().options.hideGui && gui.shouldDrawSurvivalElements()) {
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
    }

    private static void renderSunAsStats(ForgeGui gui, PoseStack stack, float partialTick, int width, int height){
        if (!PVZConfig.renderSunAsBar() && !gui.getMinecraft().options.hideGui && gui.shouldDrawSurvivalElements()) {
            Minecraft mc = gui.getMinecraft();
            mc.getProfiler().push("sun");

            Player player = getCameraPlayer();
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
                int v = player.hasEffect(MobEffects.DARKNESS) ? 10 : 0;

                if (gui.getGuiTicks() % 10 <= 3 && (levelActual <= 0 || v == 10)) {
                    y = top + (random.nextInt(3) - 1);
                }

                if (levelActual > 1000 && levelShow > 1000) {
                    int idx = 1000 + i * 200 - 1;
                    if (idx + 200 < levelShow) {
                        blit(stack, x, y, 80, v, 9, 9);
                    } else if (idx + 150 < levelShow) {
                        blit(stack, x, y, 70, v, 9, 9);
                    } else if (idx + 100 < levelShow) {
                        blit(stack, x, y, 60, v, 9, 9);
                    } else if (idx + 50 < levelShow) {
                        blit(stack, x, y, 50, v, 9, 9);
                    } else {
                        blit(stack, x, y, 40, v, 9, 9);
                    }
                } else {
                    int idx = i * 100 - 1;
                    if (idx + 100 < levelShow) {
                        blit(stack, x, y, 40, v, 9, 9);
                    } else if (idx + 75 < levelShow) {
                        blit(stack, x, y, 30, v, 9, 9);
                    } else if (idx + 50 < levelShow) {
                        blit(stack, x, y, 20, v, 9, 9);
                    } else if (idx + 25 < levelShow) {
                        blit(stack, x, y, 10, v, 9, 9);
                    } else {
                        blit(stack, x, y, 0, v, 9, 9);
                    }
                }

                if (v == 10) {
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

    private static void renderCostOfCards(ForgeGui gui, PoseStack stack, float partialTick, int width, int height){
        Player player = getCameraPlayer();
        if (player != null && PVZPlayerCapability.getValue(player, "plant_have_cost") != 0 && storedItemStack.getItem() instanceof SeedPacketItem<?>) {
            Util.setTexture(Util.prefix("textures/gui/overlay/icons.png"));
            RenderSystem.enableBlend();
            int x = player.getInventory().selected * 20 + width / 2 - 80;
            int y = height - 32;
            int w;
            if (itemStackResourceIsSun) {
                if (PVZConfig.renderSunAsBar()) {
                    w = ClientProxy.MC.font.width(itemStackCost + "") + 10;
                    blit(stack, x - w / 2, y, 40, 0, 9, 9);
                    ClientProxy.MC.font.draw(stack, itemStackCost + "", x - (float) w / 2 + 11, y + 2, 0x663600);
                    ClientProxy.MC.font.draw(stack, itemStackCost + "", x - (float) w / 2 + 10, y + 1, 0xFFFFFF);
                } else {
                    int tmp = itemStackCost;
                    int h = -2 * ceil((float)tmp/500);
                    while (tmp > 0){
                        w = tmp > 500 ? 40 : ceil((float)tmp/100) * 8;
                        for (int i = 0; i < w / 8; i ++){
                            if (tmp > 100) {
                                blit(stack, x - w / 2 + 8 * i, y + h + 4, 40, 0, 9, 9);
                            } else {
                                blit(stack, x - w / 2 + 8 * i, y + h + 4, ceil((float)tmp / 25) * 10, 0, 9, 9);
                            }
                            tmp -= 100;
                        }
                        h += 2;
                    }
                }
            } else {
                w = ClientProxy.MC.font.width(itemStackCost + "");
                ClientProxy.MC.font.draw(stack, itemStackCost + "", x - (float) w / 2 + 1, y + 2, 0x663600);
                ClientProxy.MC.font.draw(stack, itemStackCost + "", x - (float) w / 2, y + 1, 0xFFFFFF);
            }
            RenderSystem.disableBlend();
            ClientProxy.MC.getProfiler().pop();
        }
    }

    public static void blit(PoseStack stack,int x, int y, float u, float v, int width, int height) {
        GuiComponent.blit(stack,x,y,0,u,v,width,height,128,128);
    }

    private static Player getCameraPlayer() {
        return !(ClientProxy.MC.getCameraEntity() instanceof Player) ? null : ClientProxy.getPlayer();
    }

    public static void refreshItemStack(Player player, ItemStack itemStack){
        PVZResourceEvent.CheckResourceEvent event = new PVZResourceEvent.CheckResourceEvent(player, itemStack);
        MinecraftForge.EVENT_BUS.post(event);
        itemStackResourceIsSun = event.resource.equals(PVZPlayerCapNBT.SUN);
        itemStackCost = event.cost;
    }

    @SubscribeEvent
    public static void registerOverlay(RegisterGuiOverlaysEvent ev){
        ev.registerBelow(VanillaGuiOverlay.AIR_LEVEL.id(), "sun_level", PVZOverlayHandler::renderSunAsStats);
        ev.registerBelow(VanillaGuiOverlay.AIR_LEVEL.id(), "sun_bar", PVZOverlayHandler::renderSunAsBar);
        ev.registerAbove(VanillaGuiOverlay.EXPERIENCE_BAR.id(), "card_cost", PVZOverlayHandler::renderCostOfCards);
    }

}
