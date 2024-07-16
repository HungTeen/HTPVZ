package com.hungteen.pvz.client.gui;

import com.hungteen.pvz.PVZConfig;
import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.common.capability.level.PVZZombieEventCapability;
import com.hungteen.pvz.common.capability.player.PVZPlayerCapNBT;
import com.hungteen.pvz.common.capability.player.PVZPlayerCapability;
import com.hungteen.pvz.common.entity.plants.GatlingPea;
import com.hungteen.pvz.api.events.PVZResourceEvent;
import com.hungteen.pvz.common.item.ExtraHealthArmorItem;
import com.hungteen.pvz.common.item.SeedPacketItem;
import com.hungteen.pvz.common.network.ClientProxy;
import com.hungteen.pvz.common.register.PVZMobEffects;
import com.hungteen.pvz.common.world.invasion.Invasion;
import com.hungteen.pvz.util.Util;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;

import static net.minecraft.util.Mth.ceil;


@Mod.EventBusSubscriber(modid = PVZMod.MODID, value = Dist.CLIENT)
@OnlyIn(Dist.CLIENT)
public class PVZOverlayHandler{

    private static double bufferSunAmount = 0;
    private static int bufferSunBarLength = 0;
    private static final Random random = new Random();
    public static float notEnoughHint = 0;
    private static ItemStack storedMainHandItemStack = null;
    private static boolean mainHandResourceIsSun = true;
    private static int mainHandStackCost = 0;
    private static ItemStack storedOffHandItemStack = null;
    private static boolean offHandResourceIsSun = true;
    private static int offHandStackCost = 0;
    private static boolean storedHaveCost = false;
    private static boolean renderArmorInSingleBar = false;
    private static Set<ZombieEventBarInformation> invasionBars = new HashSet<>();

    public static void tick(float tickTime) {
        if (PVZPlayerCapability.getPlayerData(ClientProxy.getPlayer()).isPresent()) {
            double tmp = Math.pow(0.95, tickTime / 0.01);
            int now = PVZPlayerCapability.getValue(ClientProxy.getPlayer(),  PVZPlayerCapNBT.SUN);
            int barLength = (int) (94 * bufferSunAmount / PVZPlayerCapability.getValueLimit(ClientProxy.getPlayer(), PVZPlayerCapNBT.SUN).getSecond());
            bufferSunAmount = now * (1 - tmp) + tmp * bufferSunAmount;
            bufferSunBarLength = (int) Math.min(94, (barLength * (1 - tmp) + tmp * bufferSunBarLength));
            if (bufferSunBarLength != barLength && Math.abs(bufferSunBarLength - barLength) <= 10) {
                bufferSunBarLength = barLength;
            }
            if (notEnoughHint > 0) {
                notEnoughHint -= tickTime;
            }
        }
        boolean needCost = getCameraPlayer() != null && (PVZPlayerCapability.getValue(getCameraPlayer(), "plant_have_cost") == 1) != storedHaveCost;
        ItemStack itemStack = getCameraPlayer() == null ? null : getCameraPlayer().getItemInHand(InteractionHand.MAIN_HAND);
        if ((itemStack != storedMainHandItemStack || needCost) && itemStack != null && itemStack.getItem() instanceof SeedPacketItem<?>) {
            refreshMainHandItemStack(getCameraPlayer());
            storedHaveCost = (PVZPlayerCapability.getValue(getCameraPlayer(), "plant_have_cost") == 1);
        }
        storedMainHandItemStack = itemStack;
        itemStack = getCameraPlayer() == null ? null : getCameraPlayer().getItemInHand(InteractionHand.OFF_HAND);
        if ((itemStack != storedOffHandItemStack || needCost) && itemStack != null && itemStack.getItem() instanceof SeedPacketItem<?>) {
            refreshOffHandItemStack(getCameraPlayer());
            storedHaveCost = (PVZPlayerCapability.getValue(getCameraPlayer(), "plant_have_cost") == 1);
        }
        storedOffHandItemStack = itemStack;
    }

    @SubscribeEvent
    public static void banVanillaArmorBar(RenderGuiOverlayEvent.Pre ev) {
        int armorHealth = 0;
        Player player = getCameraPlayer();
        if (player == null) return;
        for (ItemStack itemStack : player.getArmorSlots()) {
            if (itemStack.getItem() instanceof ExtraHealthArmorItem) {
                armorHealth += itemStack.getMaxDamage() - itemStack.getDamageValue();
            }
        }
        if (armorHealth == 0) return;

        if (PVZConfig.renderSeparateArmorBar() && ev.getOverlay().id().equals(new ResourceLocation("armor_level"))) {
            ev.setCanceled(true);
            renderArmorInSingleBar = true;
        }
    }
    private static void renderArmorAsSingleBar(ForgeGui gui, PoseStack stack, float partialTick, int width, int height) {
        if (! renderArmorInSingleBar) return;
        renderArmorInSingleBar = false;
        if (!gui.getMinecraft().options.hideGui && gui.shouldDrawSurvivalElements()) {
            Player player = getCameraPlayer();
            if (player == null) return;
            Minecraft mc = gui.getMinecraft();
            mc.getProfiler().push("pvz_armor");

            int armorHealth = 0;
            for (ItemStack itemStack : player.getArmorSlots()) {
                if (itemStack.getItem() instanceof ExtraHealthArmorItem) {
                    armorHealth += itemStack.getMaxDamage() - itemStack.getDamageValue();
                }
            }

            if (armorHealth == 0) return;

            Util.setTexture(Util.prefix("textures/gui/overlay/icons.png"));
            RenderSystem.enableBlend();

            armorHealth /= 5; // 5 durability equals to 1 health.

            int armorRows = (int) Math.ceil((float) armorHealth / 5.0F / 10.0F);
            int rowHeight = Math.max(6 - armorRows, 3);
            int left = width / 2 - 100;
            int top = height - gui.leftHeight;
            int draws = Mth.ceil((float) armorHealth / 5.0F);
            for (int i = draws; armorHealth > 0; -- i) {
                int x = left + ((i - 1) % 10) * 8 + 9;
                int y = top - ((i - 1) / 10) * rowHeight;
                if (i == draws && Math.round((float) armorHealth / 5.0F) != draws) {
                    blit(stack, x, y, 100, 10, 9, 9);
                } else {
                    blit(stack, x, y, 90, 10, 9, 9);
                }
                armorHealth -= 5;
            }
            RenderSystem.disableBlend();


            gui.leftHeight += 10;
            mc.getProfiler().pop();
        }
    }

    private static void renderArmorOnHealthBar(ForgeGui gui, PoseStack stack, float partialTick, int width, int height) {
        if (PVZConfig.renderSeparateArmorBar()) return;
        if (!gui.getMinecraft().options.hideGui && gui.shouldDrawSurvivalElements()) {
            Player player = getCameraPlayer();
            if (player == null) return;
            Minecraft mc = gui.getMinecraft();
            mc.getProfiler().push("pvz_armor");

            int armorHealth = 0;
            for (ItemStack itemStack : player.getArmorSlots()) {
                if (itemStack.getItem() instanceof ExtraHealthArmorItem) {
                    armorHealth += itemStack.getMaxDamage() - itemStack.getDamageValue();
                }
            }
            int health = (int) (player.getMaxHealth() + Mth.ceil(player.getAbsorptionAmount()));
            Util.setTexture(Util.prefix("textures/gui/overlay/icons.png"));
            RenderSystem.enableBlend();

            armorHealth /= 5; // 5 durability equals to 1 health.

            int healthRows = Mth.ceil((health) / 2.0F / 10.0F);
            int rowHeight = Math.max(12 - healthRows, 3);
            int left = width / 2 - 100;
            int top = height - gui.leftHeight + healthRows * rowHeight + (rowHeight != 10 ? 10 - rowHeight : 0);

            for (int i = 0; armorHealth > 0 && health > 0; ++i) {
                int x = left + (i % 10) * 8 + 9;
                int y = top - (i / 10) * rowHeight;
                health -= 2;
                if (2 <= armorHealth) {
                    blit(stack, x, y, 90, 0, 9, 9);
                    armorHealth -= 2;
                } else {
                    blit(stack, x, y, 100, 0, 9, 9);
                    armorHealth -= 1;
                }
            }
            RenderSystem.disableBlend();
            mc.getProfiler().pop();
        }
    }

    private static void renderGatlingOverheat(ForgeGui gui, PoseStack stack, float partialTick, int width, int height) {
        Player player = getCameraPlayer();
        if (player != null && player.getVehicle() instanceof GatlingPea gatlingPea) {
            Minecraft mc = gui.getMinecraft();
            mc.getProfiler().push("gatling_overheat");

            Util.setTexture(Util.prefix("textures/gui/overlay/icons.png"));
            int j = (int) (gatlingPea.getOverheat() * 182.0F / GatlingPea.MAX_OVERHEAT);
            int k = height - 32 + 3;
            blit(stack, width / 2 - 91, k, 0, 30, 182, 5);
            if (j > 0) {
                blit(stack, width / 2 - 91, k, 0, 35, j, 5);
            }
            mc.getProfiler().pop();
        }
    }

    private static void renderButter(ForgeGui gui, PoseStack stack, float partialTick, int width, int height) {
        if (ClientProxy.MC.getCameraEntity() instanceof LivingEntity entity &&
                entity.getAttribute(Attributes.MOVEMENT_SPEED).getModifier(PVZMobEffects.BUTTER_EFFECT_UUID) != null)
        {
            gui.renderTextureOverlay(Util.prefix("textures/gui/butter_outline.png"),
                    entity.hasEffect(PVZMobEffects.BUTTER.get()) ? Math.min(1, (float) entity.getEffect(PVZMobEffects.BUTTER.get()).getDuration() / 60) : 0);
        }
    }

    private static void renderSunAsBar(ForgeGui gui, PoseStack stack, float partialTick, int width, int height) {
        if (PVZConfig.renderSunAsNumber() && !gui.getMinecraft().options.hideGui && gui.shouldDrawSurvivalElements()) {
            stack.pushPose();
            RenderSystem.enableBlend();
            int x = PVZConfig.renderSunBarX();
            int y = PVZConfig.renderSunBarY();
            double scale = PVZConfig.renderOverlayScale();
            stack.scale((float) (scale), (float) (scale), (float) (scale));
            Util.setTexture(Util.prefix("textures/gui/overlay/icons.png"));

            int drawX = x >= 0 ? x + 35 : (int) (width/scale) - 134 + x;
            int drawY = y >= 0 ? y : (int) (height/scale) - 15 + y;
            Util.GuiBiltScaled(stack, drawX, drawY, 156, 240, 100, 16, 1);
            Util.GuiBiltScaled(stack, drawX + 3, drawY, 159, 224, bufferSunBarLength, 16, 1);
            Util.GuiBiltScaled(stack, x >= 0 ? x : (int) (width/scale) - 33 + x, y >= 0 ? y : (int) (height/scale) - 33 + y, 222, 190, 34, 34, 1);

            Util.drawCenteredScaledString(stack, ClientProxy.MC.font, (int) Math.round(bufferSunAmount) + "", (x >= 0 ? x + 86 : (int) (width / scale) - 84 + x), (y >= 0 ? y + 5 : (int) (height / scale) - 11 + y), (notEnoughHint * 3) % 2 >= 1 ? 0xEF1010 : 0x663600, 1f);
            Util.drawCenteredScaledString(stack, ClientProxy.MC.font, (int) Math.round(bufferSunAmount) + "", (x >= 0 ? x + 85 : (int) (width / scale) - 85 + x), (y >= 0 ? y + 4 : (int) (height/scale) - 12 + y), 0xFFFFFF, 1f);

            RenderSystem.disableBlend();
            stack.popPose();
            stack.scale(1, 1, 1);
        }
    }

    private static void renderSunAsStats(ForgeGui gui, PoseStack stack, float partialTick, int width, int height) {
        if (!PVZConfig.renderSunAsNumber() && !gui.getMinecraft().options.hideGui && gui.shouldDrawSurvivalElements()) {
            Minecraft mc = gui.getMinecraft();
            mc.getProfiler().push("sun");

            Player player = getCameraPlayer();
            Util.setTexture(Util.prefix("textures/gui/overlay/icons.png"));
            RenderSystem.enableBlend();

            int left = width / 2 + 91;
            int top = height - gui.rightHeight;
            gui.rightHeight += 10;

            int levelShow = (int) Math.round(bufferSunAmount);
            int levelMax = PVZPlayerCapability.getValueLimit(player, PVZPlayerCapNBT.SUN).getSecond();
            int levelActual = PVZPlayerCapability.getValue(player, PVZPlayerCapNBT.SUN);

            for (int i = 0; i < (Math.min((float) levelMax / 100, 10)); ++i) {
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
                if (Math.abs(levelActual - levelShow) > 5) { //avoid natural sun regaining keeping stat bar showing white.
                    blit(stack, x, y, 40, 20, 9, 9);
                }
                if ((notEnoughHint * 3) % 2 >= 1) {
                    blit(stack, x, y, 50, 20, 9, 9);
                }

            }
            RenderSystem.disableBlend();
            mc.getProfiler().pop();
        }
    }

    private static void renderCostOfSeeds(ForgeGui gui, PoseStack stack, float partialTick, int width, int height) {
        Player player = getCameraPlayer();
        if (!gui.getMinecraft().options.hideGui && gui.shouldDrawSurvivalElements() && player != null && PVZPlayerCapability.getValue(player, "plant_have_cost") != 0) {
            Util.setTexture(Util.prefix("textures/gui/overlay/icons.png"));
            RenderSystem.enableBlend();
            int x;
            int y = height - 32;
            int w;
            if (storedMainHandItemStack.getItem() instanceof SeedPacketItem<?>) {
                x = player.getInventory().selected * 20 + width / 2 - 80;
                if (mainHandResourceIsSun) {
                    if (PVZConfig.renderSunAsNumber()) {
                        w = ClientProxy.MC.font.width(mainHandStackCost + "") + 10;
                        blit(stack, x - w / 2, y, 40, 0, 9, 9);
                        ClientProxy.MC.font.draw(stack, mainHandStackCost + "", x - (float) w / 2 + 11, y + 2, 0x663600);
                        ClientProxy.MC.font.draw(stack, mainHandStackCost + "", x - (float) w / 2 + 10, y + 1, 0xFFFFFF);
                    } else {
                        int tmp = mainHandStackCost;
                        int h = -2 * ceil((float)tmp / 500);
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
                    w = ClientProxy.MC.font.width(mainHandStackCost + "");
                    ClientProxy.MC.font.draw(stack, mainHandStackCost + "", x - (float) w / 2 + 1, y + 2, 0x663600);
                    ClientProxy.MC.font.draw(stack, mainHandStackCost + "", x - (float) w / 2, y + 1, 0xFFFFFF);
                }
            }
            Util.setTexture(Util.prefix("textures/gui/overlay/icons.png"));
            if (storedOffHandItemStack.getItem() instanceof SeedPacketItem<?>) {
                x = width / 2 - 110;
                if (offHandResourceIsSun) {
                    if (PVZConfig.renderSunAsNumber()) {
                        w = ClientProxy.MC.font.width(offHandStackCost + "") + 10;
                        blit(stack, x - w / 2, y, 40, 0, 9, 9);
                        ClientProxy.MC.font.draw(stack, offHandStackCost + "", x - (float) w / 2 + 11, y + 2, 0x663600);
                        ClientProxy.MC.font.draw(stack, offHandStackCost + "", x - (float) w / 2 + 10, y + 1, 0xFFFFFF);
                    } else {
                        int tmp = offHandStackCost;
                        int h = -2 * ceil((float)tmp / 500);
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
                    w = ClientProxy.MC.font.width(offHandStackCost + "");
                    ClientProxy.MC.font.draw(stack, offHandStackCost + "", x - (float) w / 2 + 1, y + 2, 0x663600);
                    ClientProxy.MC.font.draw(stack, offHandStackCost + "", x - (float) w / 2, y + 1, 0xFFFFFF);
                }
                RenderSystem.disableBlend();
                ClientProxy.MC.getProfiler().pop();
            }
        }
    }

    @SubscribeEvent
    public static void getInvasionBars(CustomizeGuiOverlayEvent.BossEventProgress event) {
        ComponentContents contents = event.getBossEvent().getName().getContents();
        if (contents instanceof TranslatableContents tc && tc.getKey().contains("event.pvz.invasion")) {
            event.setIncrement(PVZConfig.renderPVZTypeInvasionBar() ? 0 : event.getIncrement() + 5);
            event.setCanceled(true);
            List<Object> list = Arrays.stream(tc.getArgs()).toList();
            invasionBars.add(new ZombieEventBarInformation((UUID) list.get(list.size() - 1), event.getX(), event.getY(), event.getBossEvent()));
        }
    }

    private static void renderInvasionBars(ForgeGui gui, PoseStack stack, float partialTick, int width, int height) {
        PVZZombieEventCapability cap = ClientProxy.getLevel().getCapability(PVZZombieEventCapability.CAP).orElse(null);
        if (cap == null) {
            return;
        }
        RenderSystem.enableBlend();
        int renderHeight = 24;
        for (ZombieEventBarInformation information : invasionBars) {
            Util.setTexture(Util.prefix("textures/gui/overlay/icons.png"));
            Invasion invasion = (Invasion) cap.getEvent(information.uuid);
            if (invasion == null) {
                continue;
            }
            if (PVZConfig.renderPVZTypeInvasionBar()) {
                //when drawing at right bottom.
                stack.pushPose();
                double scale = PVZConfig.renderOverlayScale();
                stack.scale((float) scale, (float) scale, (float) scale);
                width = (int) (width / scale);
                height = (int) (height / scale);
                blit(stack, width - 160, height - renderHeight, 0, 54, 158, 21);
                blit(stack, (int) (width - 9 - 144 * (information.event.getProgress())), height - renderHeight, 7 + (int) (144 * (1 - information.event.getProgress())), 75, (int) (144 * (information.event.getProgress())), 21);
                for (int i = 0; i < invasion.waves.size(); i ++) {
                    Invasion.Wave wave = invasion.waves.get(i);
                    if (wave.isBigWave) {
                        blit(stack, (int) (width - 23 - 130 * ((float) i / (invasion.waves.size() - 1))),
                                height - renderHeight + (invasion.currentWave >= i ? - 2 : + 3), 242, wave.isGivenUp ? 26 : 11, 14, (invasion.currentWave >= i ? 15 : 10));
                    }
                }
                blit(stack, (int) (width - 9 - 144 * (information.event.getProgress())), height - renderHeight + 3, 241, 41, 15, 12);
                renderHeight += 25;
                stack.popPose();
            } else {
                //when drawing at top, not affected by overlay scale.
                blit(stack, information.x() - 2, information.y() + 2, 0, 40, 186, 9);
                blit(stack, information.x(), information.y() + 4, 0, 49, (int) (182 * information.event.getProgress()), 5);
                for (int i = 0; i < invasion.waves.size(); i ++) {
                    Invasion.Wave wave = invasion.waves.get(i);
                    if (wave.isBigWave) {
                        blit(stack, (int) (information.x() + 170 * ((float) (i + 1) / invasion.waves.size())),
                                information.y() + (invasion.currentWave >= i ? - 2 : + 1), wave.isGivenUp ? 245 : 234, 0, 11, (invasion.currentWave >= i ? 11 : 8));
                    }
                }
                Component component = information.event.getName();
                int l = ClientProxy.MC.font.width(component);
                int i1 = width / 2 - l / 2;
                int j1 = information.y() - 9;
                ClientProxy.MC.font.drawShadow(stack, component, (float)i1, (float)j1, 0xffffff);
            }
        }
        RenderSystem.disableBlend();
        invasionBars.clear();
    }

    public static void blit(PoseStack stack,int x, int y, float u, float v, int width, int height) {
        GuiComponent.blit(stack,x,y,0,u,v,width,height,256,256);
    }

    private static Player getCameraPlayer() {
        return !(ClientProxy.MC.getCameraEntity() instanceof Player) ? null : ClientProxy.getPlayer();
    }

    public static void refreshMainHandItemStack(Player player) {
        PVZResourceEvent.CheckResourceEvent event = Util.checkPlantResourceEvent(player, player.getItemInHand(InteractionHand.MAIN_HAND));
        MinecraftForge.EVENT_BUS.post(event);
        mainHandResourceIsSun = event.resource.equals(PVZPlayerCapNBT.SUN);
        mainHandStackCost = event.cost;
    }
    public static void refreshOffHandItemStack(Player player) {
        PVZResourceEvent.CheckResourceEvent event = Util.checkPlantResourceEvent(player, player.getItemInHand(InteractionHand.OFF_HAND));
        MinecraftForge.EVENT_BUS.post(event);
        offHandResourceIsSun = event.resource.equals(PVZPlayerCapNBT.SUN);
        offHandStackCost = event.cost;
    }

    @SubscribeEvent
    public static void registerOverlay(RegisterGuiOverlaysEvent ev) {
        ev.registerBelow(VanillaGuiOverlay.AIR_LEVEL.id(), "sun_level", PVZOverlayHandler::renderSunAsStats);
        ev.registerBelow(VanillaGuiOverlay.AIR_LEVEL.id(), "sun_bar", PVZOverlayHandler::renderSunAsBar);
        ev.registerBelow(VanillaGuiOverlay.EXPERIENCE_BAR.id(), "gatling_overheat", PVZOverlayHandler::renderGatlingOverheat);
        ev.registerAbove(VanillaGuiOverlay.EXPERIENCE_BAR.id(), "card_cost", PVZOverlayHandler::renderCostOfSeeds);
        ev.registerAbove(VanillaGuiOverlay.PLAYER_HEALTH.id(), "pvz_armor_on_health", PVZOverlayHandler::renderArmorOnHealthBar);
        ev.registerAbove(VanillaGuiOverlay.ARMOR_LEVEL.id(), "pvz_armor_bar", PVZOverlayHandler::renderArmorAsSingleBar);
        ev.registerAbove(VanillaGuiOverlay.FROSTBITE.id(), "butter", PVZOverlayHandler::renderButter);
        ev.registerAbove(VanillaGuiOverlay.BOSS_EVENT_PROGRESS.id(), "invasion", PVZOverlayHandler::renderInvasionBars);
    }

    public record ZombieEventBarInformation(UUID uuid, int x, int y, BossEvent event) {};
}
