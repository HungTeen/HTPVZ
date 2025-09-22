package com.hungteen.pvz.util;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.api.events.PVZResourceEvent;
import com.hungteen.pvz.common.capability.player.PVZPlayerCapStats;
import com.hungteen.pvz.common.capability.player.PVZPlayerCapability;
import com.hungteen.pvz.common.item.SeedPacketItem;
import com.hungteen.pvz.common.network.ClientProxy;
import com.hungteen.pvz.common.network.PlayerCoolDownPacket;
import com.hungteen.pvz.common.world.team.PVZTeamData;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.RegistryObject;

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
        int cost = (resource.equals(PVZPlayerCapStats.SUN)
                && PVZPlayerCapability.getValue(player, "plant_have_cost") == 0) ?
                0 : item.getBaseCost(plantCard);
        int coolDown = PVZPlayerCapability.getValue(player, "plant_have_cd") == 0 ?
                1 : item.getBaseCoolDown(plantCard);
        return new PVZResourceEvent.CheckResourceEvent(player, plantCard, resource, cost, coolDown);
    }

    public static PVZResourceEvent.CheckPlantConditionEvent checkPlantConditionEvent(Player player, ItemStack plantCard, Entity spawningEntity) {
        SeedPacketItem<?> item = (SeedPacketItem<?>) plantCard.getItem();
        String resource = item.getResource(plantCard);
        int cost = (resource.equals(PVZPlayerCapStats.SUN)
                && PVZPlayerCapability.getValue(player, "plant_have_cost") == 0) ?
                0 : item.getBaseCost(plantCard);
        int coolDown = PVZPlayerCapability.getValue(player, "plant_have_cd") == 0 ?
                1 : item.getBaseCoolDown(plantCard);
        return new PVZResourceEvent.CheckPlantConditionEvent(player, plantCard, spawningEntity, resource, cost, coolDown);
    }


    //team tools
    public static boolean isTeamEvil(Level level, Team team) {
        if (level.isClientSide) {
            return PVZTeamData.clientEvilList.contains(team.getName());
        } else {
            Scoreboard scoreboard = level.getScoreboard();
            return PVZTeamData.isEvil(scoreboard, team.getName());
        }
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

    //debug tools
    public static void showPathEnd(Mob mob) {
        Path path = mob.getNavigation().path;
        if (! mob.level.isClientSide && path != null) {
            ((ServerLevel) mob.level).sendParticles(ParticleTypes.COMPOSTER, path.getEndNode().x + 0.5, path.getEndNode().y + 0.5, path.getEndNode().z + 0.5,
                    1, 0.0D, 0.0D, 0.0D, 0.25F);
        }
    }
}
