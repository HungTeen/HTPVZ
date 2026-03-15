package com.hungteen.pvz.common.world.zen_garden;

import com.hungteen.pvz.PVZConfig;
import com.hungteen.pvz.common.capability.player.PVZPlayerCapability;
import com.hungteen.pvz.common.register.PVZBlocks;
import com.hungteen.pvz.common.register.PVZDimensions;
import com.hungteen.pvz.common.register.PVZStructures;
import com.hungteen.pvz.util.Util;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.portal.PortalForcer;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Random;
import java.util.function.Function;

public class ZenGardenTeleporter extends PortalForcer {
    public static final ResourceKey<Level> GARDEN = ResourceKey.create(Registry.DIMENSION_REGISTRY, Util.prefix("zen_garden"));

    public ZenGardenTeleporter(ServerLevel p_77650_) {
        super(p_77650_);
    }

    @Nullable
    public PortalInfo getPortalInfo(Entity entity, ServerLevel destWorld, Function<ServerLevel, PortalInfo> defaultPortalInfo)
    {
        Vec3 vec3;
        if (entity instanceof ServerPlayer player) {
            vec3 = PVZPlayerCapability.getTeleportPos(player, destWorld);
            if (vec3 != null && ! destWorld.getBlockState(new BlockPos(vec3)).is(PVZBlocks.ZEN_GARDEN_PORTAL.get())) {
                vec3 = null;
            }
            if (vec3 == null) {
                Random random = new Random(PVZConfig.PVZGameRules.getBoolean(player.level, PVZConfig.Common.gardenForEveryOne)
                        ? player.getUUID().getLeastSignificantBits()
                        : player.getTeam() == null ? 0 : player.getTeam().hashCode()
                        );
                if (destWorld.dimension().location().equals(PVZDimensions.ZEN_GARDEN)) {
                    HolderSet<Structure> holderSet = (destWorld.registryAccess().registryOrThrow(Registry.STRUCTURE_REGISTRY)
                            .getHolder(PVZStructures.GARDEN_PORTAL.getKey())).map(HolderSet::direct).get();
                    Pair<BlockPos, Holder<Structure>> pair = destWorld.getChunkSource().getGenerator()
                            .findNearestMapStructure(destWorld, holderSet,
                                    new BlockPos((random.nextFloat() - 0.5) * 1440000, 85, (random.nextFloat() - 0.5) * 1440000)
                                    , 100, false);
                    int height = 84;//avoid players from teleporting directly onto sky islands
                    //TODO better methods?
                    if (pair != null) {
                        vec3 = new Vec3(pair.getFirst().getX() + 0.5, height, pair.getFirst().getZ() + 0.5);
                    } else {
                        vec3 = new Vec3(0, height, 0);
                    }
                } else {
                    BlockPos pos = player.getRespawnPosition();
                    if (pos == null) {
                        pos = level.getSharedSpawnPos();
                    }
                    vec3 = Vec3.atBottomCenterOf(pos);
                }
            }
            return new PortalInfo(vec3, Vec3.ZERO, entity.getYRot(), entity.getXRot());
        } else { //Naturally can't be non-player entities because the watering pot needs right-click to activate.
            return super.getPortalInfo(entity, destWorld, defaultPortalInfo);
        }
    }
}
