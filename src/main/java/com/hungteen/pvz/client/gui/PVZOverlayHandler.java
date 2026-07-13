package com.hungteen.pvz.client.gui;

import com.hungteen.pvz.PVZConfig;
import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.api.events.PVZResourceEvent;
import com.hungteen.pvz.client.PVZKeyBindings;
import com.hungteen.pvz.common.capability.level.PVZZombieEventCapability;
import com.hungteen.pvz.common.capability.player.PVZPlayerCapStats;
import com.hungteen.pvz.common.capability.player.PVZPlayerCapability;
import com.hungteen.pvz.common.entity.plants.GatlingPea;
import com.hungteen.pvz.common.item.EnderSeedBundleItem;
import com.hungteen.pvz.common.item.ExtraHealthArmorItem;
import com.hungteen.pvz.common.item.SeedPacketItem;
import com.hungteen.pvz.common.network.ClientProxy;
import com.hungteen.pvz.common.register.PVZMobEffects;
import com.hungteen.pvz.common.tags.PVZItemTags;
import com.hungteen.pvz.common.world.invasion.Invasion;
import com.hungteen.pvz.util.EntityUtil;
import com.hungteen.pvz.util.Util;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
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

import javax.annotation.Nullable;
import java.util.*;

import static net.minecraft.util.Mth.ceil;


@Mod.EventBusSubscriber(modid = PVZMod.MODID, value = Dist.CLIENT)
@OnlyIn(Dist.CLIENT)
public class PVZOverlayHandler {
    private static double bufferSunAmount = 0;
    private static int bufferSunBarLength = 0;
    private static final Random random = new Random();
    public static float notEnoughHint = 0;
    private static boolean renderArmorInSingleBar = false;
    private static Set<ZombieEventBarInformation> invasionBars = new HashSet<>();
    public static Map<ItemStack, Pair<Integer, Integer>> itemsToDrawCost = new HashMap<>();
    private static double enderSeedBundleAnim = 0;


    public static void tick(float tickTime) {
        Player player = getCameraPlayer();
        if (player != null && PVZPlayerCapability.getPlayerData(player).isPresent()) {
            double tmp = Math.pow(0.95, tickTime * 100);
            int now = PVZPlayerCapability.getValue(player,  PVZPlayerCapStats.SUN);
            int barLength = (int) (94 * bufferSunAmount / PVZPlayerCapability.getValueLimit(player, PVZPlayerCapStats.SUN).getSecond());
            bufferSunAmount = now * (1 - tmp) + tmp * bufferSunAmount;
            bufferSunBarLength = (int) Math.min(94, (barLength * (1 - tmp) + tmp * bufferSunBarLength));
            if (bufferSunBarLength != barLength && Math.abs(bufferSunBarLength - barLength) <= 10) {
                bufferSunBarLength = barLength;
            }
            if (notEnoughHint > 0) {
                notEnoughHint -= tickTime;
            }
            boolean renderOn = PVZKeyBindings.keyEnderSeedBundle.isDown()
                    && !EnderSeedBundleItem.getHoldingEnderSeedBundle(player).isEmpty()
                    && !(ClientProxy.MC.screen instanceof AbstractContainerScreen<?>);
            enderSeedBundleAnim += ((renderOn ? 6 : 0) - enderSeedBundleAnim) / 30 / tickTime;
            if (enderSeedBundleAnim < 0.5) enderSeedBundleAnim = 0;
            if (enderSeedBundleAnim > 5) enderSeedBundleAnim = 5;
        }
    }

    @SubscribeEvent
    public static void banVanillaArmorBar(RenderGuiOverlayEvent.Pre ev) {
        Player player = getCameraPlayer();
        if (player == null) return;
        if (EntityUtil.getExtraArmorHealth(player) <= 0) return;
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
            int ironArmorHealth = 0;
            for (ItemStack itemStack : player.getArmorSlots()) {
                if (itemStack.getItem() instanceof ExtraHealthArmorItem) {
                    int extra = itemStack.getMaxDamage() - itemStack.getDamageValue();
                    armorHealth += extra;
                    if (itemStack.is(PVZItemTags.IRON)) {
                        ironArmorHealth += extra;
                    }
                }
            }
            if (armorHealth == 0) return;
            armorHealth /= 5; // 5 durability equals to 1 health.
            ironArmorHealth /= 5;

            int armorRows = (int) Math.ceil((float) armorHealth / 5.0F / 10.0F);
            int rowHeight = Math.max(6 - armorRows, 3);
            int left = width / 2 - 100;
            int top = height - gui.leftHeight;
            int totalDrawTimes = Mth.ceil((float) armorHealth / 5.0F);

            Util.setTexture(Util.prefix("textures/gui/overlay/icons.png"));
            RenderSystem.enableBlend();
            for (int i = totalDrawTimes; i > 0; -- i) {
                int x = left + ((i - 1) % 10) * 8 + 9;
                int y = top - ((i - 1) / 10) * rowHeight;
                if (i == totalDrawTimes && Math.round((float) armorHealth / 5.0F) != Math.ceil((float) armorHealth / 5.0F)) {
                    blit(stack, x, y, 100, 10, 9, 9);
                } else {
                    blit(stack, x, y, 90, 10, 9, 9);
                }
                if (ironArmorHealth > (i - 1) * 5) {
                    if (i > Math.floor((float) ironArmorHealth / 5.0F) && Math.round((float) ironArmorHealth / 5.0F) != Math.ceil((float) ironArmorHealth / 5.0F)) {
                        blit(stack, x, y, 120, 10, 9, 9);
                    } else {
                        blit(stack, x, y, 110, 10, 9, 9);
                    }
                }
            }
            RenderSystem.disableBlend();

            gui.leftHeight += ((armorHealth / 50 - 1) / 10) * rowHeight;
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
            int ironArmorHealth = 0;
            for (ItemStack itemStack : player.getArmorSlots()) {
                if (itemStack.getItem() instanceof ExtraHealthArmorItem) {
                    int extra = itemStack.getMaxDamage() - itemStack.getDamageValue();
                    armorHealth += extra;
                    if (itemStack.is(PVZItemTags.IRON)) {
                        ironArmorHealth += extra;
                    }
                }
            }
            if (armorHealth == 0) return;
            armorHealth /= 5; // 5 durability equals to 1 health.
            ironArmorHealth /= 5;

            int health = (int) (player.getMaxHealth() + Mth.ceil(player.getAbsorptionAmount()));
            int healthRows = Mth.ceil(health / 2.0F / 10.0F);
            int rowHeight = Math.max(12 - healthRows, 3);
            int left = width / 2 - 100;
            int top = height - gui.leftHeight + healthRows * rowHeight + (rowHeight != 10 ? 10 - rowHeight : 0);
            int totalDrawTimes = (int) Math.min(Mth.ceil((float) armorHealth / 5.0F), Math.ceil((float) health / 2));

            Util.setTexture(Util.prefix("textures/gui/overlay/icons.png"));
            RenderSystem.enableBlend();
            for (int i = totalDrawTimes; i > 0; -- i) {
                int x = left + ((i - 1) % 10) * 8 + 9;
                int y = top - ((i - 1) / 10) * rowHeight;
                if (health - 2 * i == 1 || i == totalDrawTimes && Math.round((float) armorHealth / 5.0F) != Math.ceil((float) armorHealth / 5.0F)) {
                    blit(stack, x, y, 100, 0, 9, 9);
                } else {
                    blit(stack, x, y, 90, 0, 9, 9);
                }
                if (ironArmorHealth > (i - 1) * 5) {
                    if (health - 2 * i == 1 || i > Math.floor((float) ironArmorHealth / 5.0F) && Math.round((float) ironArmorHealth / 5.0F) != Math.ceil((float) ironArmorHealth / 5.0F)) {
                        blit(stack, x, y, 120, 0, 9, 9);
                    } else {
                        blit(stack, x, y, 110, 0, 9, 9);
                    }
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
            if (gatlingPea.getFusing()) {
                blitColor(stack, width / 2 - 91, k, 0, 40, 182, 5, 0xffffff,
                        (int) (128 + Math.sin((float) gatlingPea.getOverheat() / 15 - Math.PI / 2) * 80));
            }
            mc.getProfiler().pop();
        }
    }

    private static void renderButterOverlay(ForgeGui gui, PoseStack stack, float partialTick, int width, int height) {
        if (ClientProxy.MC.getCameraEntity() instanceof LivingEntity entity) {
            if (entity.getAttribute(Attributes.MOVEMENT_SPEED).getModifier(PVZMobEffects.BUTTER_EFFECT_UUID) != null) {
                gui.renderTextureOverlay(Util.prefix("textures/gui/outline/butter_outline.png"),
                        entity.hasEffect(PVZMobEffects.BUTTER.get()) ? Math.min(1, (float) entity.getEffect(PVZMobEffects.BUTTER.get()).getDuration() / 60) : 0);
            }
        }
    }

    private static void renderHypnosis(ForgeGui gui, PoseStack stack, float partialTick, int width, int height) {
        if (getCameraPlayer() instanceof LocalPlayer player && player.hasEffect(PVZMobEffects.HYPNOTISED.get())) {
            float time = Math.min(1, (float) player.getEffect(PVZMobEffects.HYPNOTISED.get()).getDuration() / 20);
            double d0 = Mth.lerp(time, 2.0D, 1.0D);
            double d1 = (double)width * d0;
            double d2 = (double)height * d0;
            double d3 = ((double)width - d1) / 2.0D;
            double d4 = ((double)height - d2) / 2.0D;
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
            RenderSystem.setShaderColor(0.6F * time, 0.2F * time, 0.2F * time, 1.0F);
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, new ResourceLocation("textures/misc/nausea.png"));
            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder bufferbuilder = tesselator.getBuilder();
            bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            bufferbuilder.vertex(d3, d4 + d2, -90.0D).uv(0.0F, 1.0F).endVertex();
            bufferbuilder.vertex(d3 + d1, d4 + d2, -90.0D).uv(1.0F, 1.0F).endVertex();
            bufferbuilder.vertex(d3 + d1, d4, -90.0D).uv(1.0F, 0.0F).endVertex();
            bufferbuilder.vertex(d3, d4, -90.0D).uv(0.0F, 0.0F).endVertex();
            tesselator.end();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
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
            if (! EntityUtil.isEntityValid(player)) {
                return;
            }
            Util.setTexture(Util.prefix("textures/gui/overlay/icons.png"));
            RenderSystem.enableBlend();

            int left = width / 2 + 91;
            int top = height - gui.rightHeight;
            gui.rightHeight += 10;

            int levelShow = (int) Math.round(bufferSunAmount);
            int levelMax = PVZPlayerCapability.getValueLimit(player, PVZPlayerCapStats.SUN).getSecond();
            int levelActual = PVZPlayerCapability.getValue(player, PVZPlayerCapStats.SUN);

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
                } else if (player.hasEffect(PVZMobEffects.BRIGHTNESS.get())) {
                    int tmp = ((gui.getGuiTicks() + 30 - i * 4) % 30) / 2 - 12;
                    if (tmp >= 0) {
                        blit(stack, x, y, 90 - 10 * tmp, 20, 9, 9);
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
            Util.setTexture(new ResourceLocation("textures/gui/icons.png"));
            mc.getProfiler().pop();
        }
    }

    private static void renderEnderSeedBundleHotBar(ForgeGui gui, PoseStack posestack, float partialTick, int width, int height) {
        Player player = getCameraPlayer();
        if (enderSeedBundleAnim <= 0 || player == null || ClientProxy.MC.screen instanceof AbstractContainerScreen<?>) {
            return;
        }
        posestack.pushPose();
        RenderSystem.enableBlend();
        Util.setTexture(Util.prefix("textures/gui/overlay/icons.png"));
        blit(posestack, width / 2 - 91, height - 47 - (int) enderSeedBundleAnim, 0, 101, 182, 22);
        ItemStack itemStack;
        int slot = -1;
        itemStack = EnderSeedBundleItem.getHoldingEnderSeedBundle(player);
        if (itemStack.getItem() instanceof EnderSeedBundleItem item) {
            slot = item.getPointer(itemStack);
        }
        if (slot >= 0) blit(posestack, width / 2 - 92 + 20 * slot, height - 48 - (int) enderSeedBundleAnim, 158, 77, 24, 24);
        int y = height - 44 - (int) enderSeedBundleAnim;
        for (int i = 0; i < 9; i ++) {
            int x = width / 2 - 88 + 20 * i;
            itemStack = PVZPlayerCapability.getEnderSeedBundleSlot(player, i);
            if (! itemStack.isEmpty()) {
                ClientProxy.MC.getItemRenderer().renderGuiItem(itemStack, x, y);
                ClientProxy.MC.getItemRenderer().renderGuiItemDecorations(ClientProxy.MC.font, itemStack, x, y);
                if (i == slot) {
                    PVZOverlayHandler.itemsToDrawCost.put(itemStack.copy(), Pair.of(x, y));
                }
            }
        }
        posestack.popPose();
    }

    private static void renderCostOfSeeds(ForgeGui gui, PoseStack posestack, float partialTick, int width, int height) {
        Player player = getCameraPlayer();
        if (player == null || (ClientProxy.MC.screen instanceof AbstractContainerScreen<?>)) {
            return;
        }
        itemsToDrawCost.forEach((itemStack, offset) -> {
            posestack.pushPose();
            posestack.translate(offset.getFirst(), offset.getSecond(), 0.0F);
            if (PVZPlayerCapability.getValue(player, "plant_have_cost") != 0) {
                Util.setTexture(Util.prefix("textures/gui/overlay/icons.png"));
                RenderSystem.enableBlend();
                int now = PVZPlayerCapability.getValue(ClientProxy.getPlayer(), PVZPlayerCapStats.SUN);
                if (itemStack.getItem() instanceof SeedPacketItem<?>) {
                    PVZResourceEvent.CheckResourceEvent event = Util.checkPlantResourceEvent(player, itemStack);
                    MinecraftForge.EVENT_BUS.post(event);
                    boolean resourceIsSun = event.resource.equals(PVZPlayerCapStats.SUN);
                    int cost = event.cost;
                    int w;
                    if (resourceIsSun) {
                        if (PVZConfig.renderSunAsNumber()) {
                            w = ClientProxy.MC.font.width(cost + "") + 10;
                            blit(posestack, 8 - w / 2, 7, 40, 0, 9, 9);
                            ClientProxy.MC.font.draw(posestack, cost + "", 8 - (float) w / 2 + 11, -8, cost > now ? 0xEF1010 : 0x663600);
                            ClientProxy.MC.font.draw(posestack, cost + "", 8 - (float) w / 2 + 10,-9, 0xFFFFFF);
                        } else {
                            int tmp = cost;
                            int h = -2 * ceil((float) tmp / 500);
                            while (tmp > 0){
                                w = tmp > 500 ? 40 : ceil((float) tmp / 100) * 8;
                                for (int i = 0; i < w / 8; i ++) {
                                    if (tmp > 100) {
                                        blit(posestack, 8 - w / 2 + 8 * i,h - 8, 40, 0, 9, 9);
                                    } else {
                                        blit(posestack, 8 - w / 2 + 8 * i,h - 8, ceil((float)tmp / 25) * 10, 0, 9, 9);
                                    }
                                    if (cost > now) {
                                        blit(posestack, 8 - w / 2 + 8 * i,h - 8, 50, 20, 9, 9);
                                    }
                                    tmp -= 100;
                                }
                                h += 2;
                            }
                        }
                    } else {
                        w = ClientProxy.MC.font.width(cost + "");
                        ClientProxy.MC.font.draw(posestack, cost + "", 9 - (float) w / 2, -8, 0x663600);
                        ClientProxy.MC.font.draw(posestack, cost + "", 8 - (float) w / 2,-9, 0xFFFFFF);
                    }
                }
            }
            posestack.popPose();
        });
        itemsToDrawCost.clear();
    }

    @SubscribeEvent
    public static void getInvasionBars(CustomizeGuiOverlayEvent.BossEventProgress event) {
        ComponentContents contents = event.getBossEvent().getName().getContents();
        if (contents instanceof TranslatableContents tc && tc.getKey().contains("event.pvz.invasion")) {
            event.setIncrement(PVZConfig.renderPVZTypeInvasionBar() ? 0 : event.getIncrement() + 5);
            event.setCanceled(true);
            List<Object> list = Arrays.stream(tc.getArgs()).toList();
            invasionBars.add(new ZombieEventBarInformation(UUID.fromString((String) list.get(list.size() - 1)), event.getX(), event.getY(), event.getBossEvent()));
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
                blit(stack, width - 160, height - renderHeight, 0, 59, 158, 21);
                blit(stack, (int) (width - 9 - 144 * (information.event.getProgress())), height - renderHeight, 7 + (int) (144 * (1 - information.event.getProgress())), 80, (int) (144 * (information.event.getProgress())), 21);
                for (int i = 0; i < invasion.waves.size(); i ++) {
                    Invasion.Wave wave = invasion.waves.get(i);
                    if (wave.isBigWave) {
                        blit(stack, (int) (width - 23 - 130 * ((float) i / (invasion.waves.size() - 2))),
                                height - renderHeight + (invasion.currentWave >= i ? - 2 : + 3), 242, wave.isGivenUp ? 26 : 11, 14, (invasion.currentWave >= i ? 15 : 10));
                    }
                }
                blit(stack, (int) (width - 9 - 144 * (information.event.getProgress())), height - renderHeight + 3, 241, 46, 15, 12);
                renderHeight += 25;
                stack.popPose();
            } else {
                //when drawing at top, not affected by overlay scale.
                blit(stack, information.x() - 2, information.y() + 2, 0, 45, 186, 9);
                blit(stack, information.x(), information.y() + 4, 0, 54, (int) (182 * information.event.getProgress()), 5);
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

    public static void blit(PoseStack stack, int x, int y, float u, float v, int width, int height) {
        GuiComponent.blit(stack, x, y, 0/*biltOffset*/, u, v, width, height, 256, 256);
    }

    private static void blitColor(PoseStack poseStack, int x, int y, float u, float v, int width, int height, int rgb, int alpha) {
        Matrix4f p_93113_ = poseStack.last().pose();
        RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        float xStartPercent = u / 256;
        float yStartPercent = v / 256;
        float xEndPercent = (u + width) / 256;
        float yEndPercent = (v + height) / 256;
        int r = rgb >> 16 & 255;
        int g = rgb >> 8 & 255;
        int b = rgb & 255;
        int biltOffset = 0;
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
        bufferbuilder.vertex(p_93113_, x, y + height, biltOffset)
                .color(r, g, b, alpha).uv(xStartPercent, yEndPercent).endVertex();
        bufferbuilder.vertex(p_93113_, x + width, y + height, biltOffset)
                .color(r, g, b, alpha).uv(xEndPercent, yEndPercent).endVertex();
        bufferbuilder.vertex(p_93113_, x + width, y, biltOffset)
                .color(r, g, b, alpha).uv(xEndPercent, yStartPercent).endVertex();
        bufferbuilder.vertex(p_93113_, x,y, biltOffset)
                .color(r, g, b, alpha).uv(xStartPercent, yStartPercent).endVertex();
        BufferUploader.drawWithShader(bufferbuilder.end());
    }

    private static @Nullable Player getCameraPlayer() {
        return ! (ClientProxy.MC.getCameraEntity() instanceof Player) ? null : ClientProxy.getPlayer();
    }

    @SubscribeEvent
    public static void registerOverlay(RegisterGuiOverlaysEvent ev) {
        ev.registerBelow(VanillaGuiOverlay.AIR_LEVEL.id(), "sun_level", PVZOverlayHandler::renderSunAsStats);
        ev.registerBelow(VanillaGuiOverlay.AIR_LEVEL.id(), "sun_bar", PVZOverlayHandler::renderSunAsBar);
        ev.registerBelow(VanillaGuiOverlay.EXPERIENCE_BAR.id(), "gatling_overheat", PVZOverlayHandler::renderGatlingOverheat);
        ev.registerBelow(VanillaGuiOverlay.EXPERIENCE_BAR.id(), "ender_seed_bundle_hotbar", PVZOverlayHandler::renderEnderSeedBundleHotBar);
        ev.registerAbove(VanillaGuiOverlay.EXPERIENCE_BAR.id(), "card_cost", PVZOverlayHandler::renderCostOfSeeds);
        ev.registerAbove(VanillaGuiOverlay.PLAYER_HEALTH.id(), "armor_on_health", PVZOverlayHandler::renderArmorOnHealthBar);
        ev.registerAbove(VanillaGuiOverlay.ARMOR_LEVEL.id(), "armor_bar", PVZOverlayHandler::renderArmorAsSingleBar);
        ev.registerAbove(VanillaGuiOverlay.CROSSHAIR.id(), "butter", PVZOverlayHandler::renderButterOverlay);
        ev.registerAbove(VanillaGuiOverlay.FROSTBITE.id(), "hypnosis", PVZOverlayHandler::renderHypnosis);
        ev.registerAbove(VanillaGuiOverlay.BOSS_EVENT_PROGRESS.id(), "invasion", PVZOverlayHandler::renderInvasionBars);
    }
    public static void renderTextureOverlay(ResourceLocation texture, double screenWidth, double screenHeight, int rgb, float alpha) {
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        float r = (float) (rgb >> 16 & 255) / 255;
        float g = (float) (rgb >> 8 & 255) / 255;
        float b = (float) (rgb & 255) / 255;
        RenderSystem.setShaderColor(r, g, b, alpha);
        RenderSystem.setShaderTexture(0, texture);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder.vertex(0.0D, screenHeight, -90.0D).uv(0.0F, 1.0F).endVertex();
        bufferbuilder.vertex(screenWidth, screenHeight, -90.0D).uv(1.0F, 1.0F).endVertex();
        bufferbuilder.vertex(screenWidth, 0.0D, -90.0D).uv(1.0F, 0.0F).endVertex();
        bufferbuilder.vertex(0.0D, 0.0D, -90.0D).uv(0.0F, 0.0F).endVertex();
        tesselator.end();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }
    public record ZombieEventBarInformation(UUID uuid, int x, int y, BossEvent event) {}
}
