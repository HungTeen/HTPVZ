package com.hungteen.pvz.util;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.api.events.PVZResourceEvent;
import com.hungteen.pvz.common.capability.player.PVZPlayerCapNBT;
import com.hungteen.pvz.common.capability.player.PVZPlayerCapability;
import com.hungteen.pvz.common.item.SeedPacketItem;
import com.hungteen.pvz.common.network.ClientProxy;
import com.hungteen.pvz.common.network.PlayerCoolDownPacket;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;

public class Util {

    //object names and resource_location tools
    public static ResourceLocation prefix(String name){
        return new ResourceLocation(PVZMod.MODID, name);
    }
    public static String name(EntityType<? extends Entity> entityType){
        String string = entityType.getDescriptionId();
        while (string.contains(".")) {
            string = string.substring(string.indexOf(".") + 1);
        }
        return string;
    }
    public static String name(Block block){
        String string = block.getDescriptionId();
        while (string.contains(".")) {
            string = string.substring(string.indexOf(".") + 1);
        }
        return string;
    }
    public static String name(Item item){
        String string = item.getDescriptionId();
        while (string.contains(".")) {
            string = string.substring(string.indexOf(".") + 1);
        }
        return string;
    }
    public static String name(RegistryObject obj){
        return obj.getId().getPath();
    }


    //planting tools
    /**in order to put event into api, the rapid way to create the events are defined here.*/
    public static PVZResourceEvent.CheckResourceEvent checkPlantResourceEvent(Player player, ItemStack plantCard) {
        SeedPacketItem<?> item = (SeedPacketItem<?>) plantCard.getItem();
        String resource = item.getResource(plantCard);
        int cost = (resource.equals(PVZPlayerCapNBT.SUN)
                && PVZPlayerCapability.getValue(player, "plant_have_cost") == 0) ?
                0 : item.getBaseCost(plantCard);
        int coolDown = PVZPlayerCapability.getValue(player, "plant_have_cd") == 0 ?
                1 : item.getBaseCoolDown(plantCard);
        return new PVZResourceEvent.CheckResourceEvent(player, plantCard, resource, cost, coolDown);
    }

    public static PVZResourceEvent.CheckPlantConditionEvent checkPlantConditionEvent(Player player, ItemStack plantCard, Entity spawningEntity) {
        SeedPacketItem<?> item = (SeedPacketItem<?>) plantCard.getItem();
        String resource = item.getResource(plantCard);
        int cost = (resource.equals(PVZPlayerCapNBT.SUN)
                && PVZPlayerCapability.getValue(player, "plant_have_cost") == 0) ?
                0 : item.getBaseCost(plantCard);
        int coolDown = PVZPlayerCapability.getValue(player, "plant_have_cd") == 0 ?
                1 : item.getBaseCoolDown(plantCard);
        return new PVZResourceEvent.CheckPlantConditionEvent(player, plantCard, spawningEntity, resource, cost, coolDown);
    }


    //rendering tools
    @OnlyIn(Dist.CLIENT)
    public static void setTexture(ResourceLocation texture){
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, texture);
    }

    public static void drawCenteredScaledString(PoseStack stack, Font font, String string, int x, int y, int color, float scale) {
        int width = font.width(string);
        stack.pushPose();
        stack.scale(scale, scale, scale);
        font.draw(stack, string, (x - width / 2 * scale) / scale, y / scale, color);
        stack.popPose();
    }

    public static void GuiBiltScaled(PoseStack stack, int drawX, int drawY, int uvx, int uvy, int uvw, int uvh, float scale){
        ClientProxy.MC.gui.blit(stack, (int) (drawX*scale), (int) (drawY*scale),
                (int) (uvx*scale), (int) (uvy*scale), (int) (uvw*scale), (int) (uvh*scale));
    }

    public static int clientLight(Level level, Vec3 pos) {
        pos = pos.add(-0.5, 0, -0.5);
        //for interpolation. index = y * 4 + x * 2 + z.
        List<Integer> l = new ArrayList<>(List.of(
                level.getBrightness(LightLayer.BLOCK, new BlockPos(Math.floor(pos.x), Math.floor(pos.y), Math.floor(pos.z))),
                level.getBrightness(LightLayer.BLOCK, new BlockPos(Math.floor(pos.x), Math.floor(pos.y), Math.ceil(pos.z))),
                level.getBrightness(LightLayer.BLOCK, new BlockPos(Math.ceil(pos.x), Math.floor(pos.y), Math.floor(pos.z))),
                level.getBrightness(LightLayer.BLOCK, new BlockPos(Math.ceil(pos.x), Math.floor(pos.y), Math.ceil(pos.z))),
                level.getBrightness(LightLayer.BLOCK, new BlockPos(Math.floor(pos.x), Math.ceil(pos.y), Math.floor(pos.z))),
                level.getBrightness(LightLayer.BLOCK, new BlockPos(Math.floor(pos.x), Math.ceil(pos.y), Math.ceil(pos.z))),
                level.getBrightness(LightLayer.BLOCK, new BlockPos(Math.ceil(pos.x), Math.ceil(pos.y), Math.floor(pos.z))),
                level.getBrightness(LightLayer.BLOCK, new BlockPos(Math.ceil(pos.x), Math.ceil(pos.y), Math.ceil(pos.z)))
        ));
        for (int i = 0; i < 8; i ++) {
            if (l.get(i) == 0) {
                l.set(i, Math.max(Math.max(i % 2 == 0 ? l.get(i + 1) - 1 : l.get(i - 1) - 1,
                        (i >> 1) % 2 == 0 ? l.get(i + 2) - 1 : l.get(i - 2) - 1),
                        (i >> 2) % 2 == 0 ? l.get(i + 4) - 1 : l.get(i - 4) - 1));
            }
        }
        double x = pos.x - Math.floor(pos.x);
        double y = pos.y - Math.floor(pos.y);
        double z = pos.z - Math.floor(pos.z);
        double above;
        double below;
        if (x != Math.ceil(x) && z != Math.ceil(z)) {
            above = ((x + z < 1 ?
                    ((x + z) * l.get(5) + (1 - x - z) * l.get(4)) * x / (x + z) +
                            ((x + z) * l.get(6) + (1 - x - z) * l.get(4)) * z / (x + z) :
                    (((2 - x - z) * l.get(5) + (x + z - 1) * l.get(7)) * (1 - z) / (2 - x - z) +
                            (((2 - x - z) * l.get(6) + (x + z - 1) * l.get(7)) * (1 - x) / (2 - x - z)))
            ) + (x < z ?
                    ((x + 1 - z) * l.get(7) + (z - x) * l.get(5)) * x / (x + 1 - z) +
                            ((x + 1 - z) * l.get(4) + (z - x) * l.get(5)) * (1 - z) / (x + 1 - z) :
                    ((z + 1 - x) * l.get(7) + (x - z) * l.get(6)) * z / (z + 1 - x) +
                            ((z + 1 - x) * l.get(4) + (x - z) * l.get(6)) * (1 - x) / (z + 1 - x)))
                    / 2;
            below = ((x + z < 1 ?
                    ((x + z) * l.get(1) + (1 - x - z) * l.get(0)) * x / (x + z) +
                            ((x + z) * l.get(2) + (1 - x - z) * l.get(0)) * z / (x + z) :
                    (((2 - x - z) * l.get(1) + (x + z - 1) * l.get(3)) * (1 - z) / (2 - x - z) +
                            (((2 - x - z) * l.get(2) + (x + z - 1) * l.get(3)) * (1 - x) / (2 - x - z)))
            ) + (x < z ?
                    ((x + 1 - z) * l.get(3) + (z - x) * l.get(1)) * x / (x + 1 - z) +
                            ((x + 1 - z) * l.get(0) + (z - x) * l.get(1)) * (1 - z) / (x + 1 - z) :
                    ((z + 1 - x) * l.get(3) + (x - z) * l.get(2)) * z / (z + 1 - x) +
                            ((z + 1 - x) * l.get(0) + (x - z) * l.get(2)) * (1 - x) / (z + 1 - x)))
                    / 2;
        } else {
            above = x == 0 ? z == 0 ? l.get(4) : l.get(5) : z == 0 ? l.get(6) : l.get(7);
            below = x == 0 ? z == 0 ? l.get(0) : l.get(1) : z == 0 ? l.get(2) : l.get(3);
        }
        double blockLight = above * y + below * (1 - y);
        int skyExposed = level.getBrightness(LightLayer.SKY, new BlockPos(pos));
        float dayTime = level.getTimeOfDay(1F);
        float skyLight = 3;
        if (dayTime <= 0.25 || dayTime >= 0.75) {
            skyLight = 15;
        } else if (dayTime > 0.25 && dayTime < 0.3125) {
            skyLight = (15 - 12 * (float) (dayTime - 0.25) * 16);
        } else if (dayTime > 0.6875 && dayTime < 0.75) {
            skyLight = (15 - 12 * (float) (0.75 - dayTime) * 16);
        }
        PVZMod.LOGGER.info(ClientProxy.getLevel().dimensionType().ambientLight() * 15 + " : " + skyLight + " : " + blockLight) ;
        skyLight = Math.max(ClientProxy.getLevel().dimensionType().ambientLight() * 15, skyLight);
        int light = (int) (0xFF * Math.max(Math.max(0, skyLight - 15 + skyExposed), blockLight) / 15);
        return (light << 16) + (light << 8) + light;
        //TODO still have bug. Can find a better way?
    }

    public static int clientLight(Level level, BlockPos pos) {
        int blockLight = level.getBrightness(LightLayer.BLOCK, pos);
        int skyExposed = level.getBrightness(LightLayer.SKY, pos);
        float dayTime = level.getTimeOfDay(1F);
        float skyLight = 3;
        if (dayTime <= 0.25 || dayTime >= 0.75) {
            skyLight = 15;
        } else if (dayTime > 0.25 && dayTime < 0.3125) {
            skyLight = (15 - 12 * (float) (dayTime - 0.25) * 16);
        } else if (dayTime > 0.6875 && dayTime < 0.75) {
            skyLight = (15 - 12 * (float) (0.75 - dayTime) * 16);
        }
        skyLight = Math.max(ClientProxy.getLevel().dimensionType().ambientLight(), skyLight);
        int light = (int) (0xFF * Math.max(Math.max(0, skyLight - 15 + skyExposed), blockLight) / 15);
        return (light << 16) + (light << 8) + light;
    }

    //other tools
    public static void coolDownItems(ServerPlayer player, int tick) {
        for (int i = 0; i < tick; i ++) {
            player.getCooldowns().tick();
        }
        PlayerCoolDownPacket.clientCoolDown(player, tick);
    }

    public static boolean hasBlockBetween(Level level, Vec3 pos1, Vec3 pos2) {
        BlockHitResult result = level.clip(new ClipContext(pos1, pos2, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null));
        return result.getType() != HitResult.Type.MISS;
    }
}
