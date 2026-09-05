package com.hungteen.pvz.common.world;

import com.hungteen.pvz.api.ZombieEvent;
import com.hungteen.pvz.common.capability.entity.PVZEntityCapability;
import com.hungteen.pvz.common.capability.level.PVZZombieEventCapability;
import com.hungteen.pvz.common.world.invasion.InvasionCondition;
import com.hungteen.pvz.common.world.invasion.InvasionType;
import com.hungteen.pvz.util.EntityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class ZombieGroup extends ZombieEvent {
    public ZombieGroup(ServerPlayer player, UUID uuid) {
        super(player.level, uuid);
        this.target = player;
        this.range = 48;
        this.position = player.blockPosition();
    }

    public ZombieGroup(Level level, UUID uuid, CompoundTag tag) {
        super(level, uuid, tag);
        this.deserializeNBT(tag);
    }

    public static boolean spawnFor(ServerPlayer player) {
        var cap = PVZZombieEventCapability.fromLevel(player.level);
        ZombieGroup zombieGroup = new ZombieGroup(player, UUID.randomUUID());
        if (cap != null) {
            cap.addEvent(zombieGroup);
            return true;
        }
        return false;
    }

    public boolean needsSync() {
        return false;
    }

    public boolean isMainEvent() {
        return false;
    }

    @Override
    public void tick(TickEvent.ServerTickEvent ev) {
        super.tick(ev);
        if (tickCount % 20 == 0 && target instanceof ServerPlayer player && ! player.isDeadOrDying() && ! player.isCreative()
                && PVZZombieEventCapability.fromLevel(player.level).getNearestEvent(ZombieEvent.class, this.position
                , e -> e.position.distSqr(this.position) < Math.pow(e.range + 8, 2) && e != this && e.isMainEvent()) == null) {
            int plantCost = Math.max(200, getPlantsCostNearby(player));
            if ((player.getRespawnPosition() == null ? player.level.getSharedSpawnPos() : player.getRespawnPosition())
                    .distSqr(player.blockPosition()) > 2048 && plantCost < 400) {
                remove();
                return;
            }
            float angle = player.getRandom().nextFloat() * 6.28f;
            float dist = player.getRandom().nextFloat() * 16 + 24;
            BlockPos pos = player.blockPosition().offset(Math.sin(angle) * dist, 0, Math.cos(angle) * dist);
            pos = new BlockPos(pos.getX(), player.level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, pos.getX(), pos.getZ()), pos.getZ());
            if (! level.getEntities((Entity) null
                    , new AABB(pos.getX() - 8, pos.getY() - 2, pos.getZ() - 8
                            , pos.getX() + 8, pos.getY() + 2, pos.getZ() + 8)
                    , e -> EntityUtil.isTeammate(player, e)).isEmpty()) {
                return;
            }
            BlockPos pos1 = new BlockPos(pos.getX(), player.level.getHeight(Heightmap.Types.WORLD_SURFACE, pos.getX(), pos.getZ()), pos.getZ());
            if (pos1.getY() > pos.getY() + 2) return;
            if (pos1.getY() < pos.getY()) pos = pos1;
            InvasionCondition.AroundEntitiesCostCondition.shouldCalculateAroundEntityCost = false;
            List<InvasionType> types = InvasionType.generateTypes(target);
            InvasionCondition.AroundEntitiesCostCondition.shouldCalculateAroundEntityCost = true;
            if (types.isEmpty()) return;
            int totalThreat = (int) (Math.min(3000, plantCost * 1.5) * (player.getRandom().nextFloat() * 2 + 1) / 3);
            int tries = 0;
            for (int threat = 0; threat < totalThreat && tries <= 10; ) {
                InvasionType invasionType = types.get(player.getRandom().nextInt(types.size()));
                List<InvasionType.EnemyType> enemies = invasionType.enemies().stream()
                        .filter(t -> ! t.isElite() && t.startFrom() < 0.1 + ((float) totalThreat / 3000) && t.isAvailable(target, invasionType, types)).toList();
                if (enemies.isEmpty()) return;
                InvasionType.EnemyType type = enemies.get(player.getRandom().nextInt(enemies.size()));
                tries ++;
                if (totalThreat - threat >= type.threat()) {
                    threat += type.threat();
                    Entity entity = EntityType.loadEntityRecursive(type.entityData().copy(), level, Function.identity());
                    if (entity == null) {
                        continue;
                    }
                    if (! NaturalSpawner.isValidEmptySpawnBlock(level, pos, level.getBlockState(pos), level.getFluidState(pos), entity.getType())) {
                        entity.discard();
                        continue;
                    }
                    types.forEach(t -> t.getModifiers().forEach( m -> m.test(null, entity, type.threat())));
                    entity.setPos(Vec3.atCenterOf(pos));
                    ((ServerLevel) level).addFreshEntityWithPassengers(entity);
                    if (entity instanceof Mob mob) mob.setTarget(target);
                }
                remove();
            }
        } else if (tickCount > 500) {
            remove();
        }
    }
    public static int getPlantsCostNearby(ServerPlayer target) {
        List<Entity> entities = target.level.getEntities(target, target.getBoundingBox().inflate(20));
        AtomicInteger totalCost = new AtomicInteger();
        entities.forEach(entity -> entity.getCapability(PVZEntityCapability.CAP).ifPresent(cap ->
                totalCost.addAndGet(cap.resource.equals("pvz.sun") ? cap.cost : 0)));
        return totalCost.get();
    }
}
