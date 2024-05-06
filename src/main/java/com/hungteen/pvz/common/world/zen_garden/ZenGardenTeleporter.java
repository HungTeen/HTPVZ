package com.hungteen.pvz.common.world.zen_garden;

import com.hungteen.pvz.common.block.ZenGardenPortalBlock;
import com.hungteen.pvz.common.capability.player.PVZPlayerCapability;
import com.hungteen.pvz.common.register.PVZBlocks;
import com.hungteen.pvz.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.PortalForcer;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

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
                if (destWorld.dimension().location().equals(Util.prefix("zen_garden"))) {
                    vec3 = new Vec3(0, 85, 0);
                    //TODO change this.
                } else {
                    BlockPos pos = player.getRespawnPosition();
                    if (pos == null) {
                        pos = level.getSharedSpawnPos();
                    }
                    vec3 = Vec3.atBottomCenterOf(pos);
                }
            }
            return new PortalInfo(vec3, Vec3.ZERO, entity.getYRot(), entity.getXRot());
        } else {
            return super.getPortalInfo(entity, destWorld, defaultPortalInfo);
        }
    }
}
