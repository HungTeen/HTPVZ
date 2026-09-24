package com.hungteen.pvz.common.entity;

import com.hungteen.pvz.common.register.OtherRegisters;
import com.hungteen.pvz.common.register.PVZEntities;
import com.mojang.datafixers.util.Pair;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.Tags;
import net.minecraftforge.entity.PartEntity;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;

public class Portal extends Entity {

    public static final float WIDTH = 3F;
    public static final float HEIGHT = 4F;
    public static final int APPEAR_TICKS = 5;
    private static final int COOLDOWN = 5;

    private static final EntityDataAccessor<Optional<UUID>> PAIR_UUID = SynchedEntityData.defineId(Portal.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Vec3> PAIR_POS = SynchedEntityData.defineId(Portal.class, OtherRegisters.VEC3.get());
    private static final EntityDataAccessor<Integer> STATE = SynchedEntityData.defineId(Portal.class, EntityDataSerializers.INT); //0 appearing, 1 open, 2 closing

    private final Map<Player, Vec3> lastPlayerPositions = new HashMap<>();
    private Predicate<Entity> predicate = entity -> ! (entity instanceof PartEntity<?>) && ! entity.getType().is(Tags.EntityTypes.BOSSES) && ! (entity instanceof Portal);
    public int animTick = 0;

    public Portal(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public static Pair<Portal, Portal> createPair(Level level, Vec3 pos, Vec3 otherPos) {
        return createPair(level, pos, otherPos, null, null);
    }

    public static Pair<Portal, Portal> createPair(Level level, Vec3 pos, Vec3 otherPos, @Nullable UUID uuid, @Nullable UUID otherUuid) {
        Portal portal = new Portal(PVZEntities.PORTAL.get(), level);
        portal.moveTo(pos);
        if (uuid != null) portal.setUUID(uuid);
        Portal portal1 = new Portal(PVZEntities.PORTAL.get(), level);
        portal1.moveTo(otherPos);
        if (otherUuid != null) portal1.setUUID(otherUuid);
        portal.setPair(portal1);
        portal1.setPair(portal);
        level.addFreshEntity(portal);
        level.addFreshEntity(portal1);
        return new Pair<>(portal, portal1);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(PAIR_UUID, Optional.empty());
        this.entityData.define(PAIR_POS, this.position().add(1, 0, 0));
        this.entityData.define(STATE, 0);
    }

    @Override
    public void tick() {
        super.tick();
        int state = getState();
        Portal pair = getPairedPortal();
        if (state == 0) {
            if (++ animTick >= APPEAR_TICKS) setState(1);
        } else if (state == 2) {
            if (-- animTick <= 0 && ! level.isClientSide) {
                discard();
                return;
            }
        } else if (! level.isClientSide && (pair == null || ! pair.isAlive())) {
            setState(2);
        }
        if (pair != null && ! pair.position().equals(getPairPos())) {
            this.entityData.set(PAIR_POS, pair.position());
        }

        if (state == 1 && ! level.isClientSide) {
            AABB box = this.getBoundingBox().inflate(3);
            List<Entity> entities = level.getEntities(this, box, e -> predicate.test(e));
            for (Entity entity : entities) {
                Vec3 hit = getCrossingPoint(entity);
                if (entity instanceof Player player) {
                    lastPlayerPositions.put(player, player.position());
                }
                if (hit == null) continue;
                Portal target = getPairedPortal();
                if (target != null && target.isAlive() && target.getState() == 1) {
                    teleport(entity, target, hit);
                }
            }
            for (Player player : Set.copyOf(lastPlayerPositions.keySet())) {
                if (! entities.contains(player))
                    lastPlayerPositions.remove(player);
            }
        }
    }

    @Nullable
    private Vec3 getCrossingPoint(Entity entity) {
        if (entity.getPersistentData().contains("PVZPortalCooldown")
                && entity.getPersistentData().getLong("PVZPortalCooldown") >= level.getGameTime()) return null;
        Vec3 to = entity.position();
        Vec3 from = (entity instanceof Player player && lastPlayerPositions.containsKey(player))
                ? lastPlayerPositions.get(player) : new Vec3(entity.xo, entity.yo, entity.zo); //players can't use old positions...
        Vec3 displacement = to.subtract(from);
        Vec3 horizontal = new Vec3(displacement.x, 0, displacement.z);
        if (horizontal.lengthSqr() < 1E-7) {
            Vec3 motion = entity.getDeltaMovement();
            horizontal = new Vec3(motion.x, 0, motion.z);
            if (horizontal.lengthSqr() < 1E-7) return null;
            from = to.subtract(horizontal);
        }

        Vec3 normal = getPlaneNormal();
        Vec3 center = getPortalCenter();
        double d1 = from.subtract(center).dot(normal);
        double d2 = to.subtract(center).dot(normal);
        if (d1 * d2 > 0) return null;
        double denom = d1 - d2;
        double t = d1 / denom;
        if (t < 0 || t > 1) return null;

        Vec3 hit = from.add(to.subtract(from).scale(t));
        hit = new Vec3(hit.x, to.y, hit.z);
        Vec3 rel = hit.subtract(center);
        double u = rel.dot(getPlaneRight());
        double v = rel.y;
        if (Math.abs(u) > WIDTH / 2.0 || Math.abs(v) > HEIGHT / 2.0) return null;
        return hit;
    }

    private void teleport(Entity entity, Portal target, Vec3 hit) {
        Vec3 srcCenter = getPortalCenter();
        Vec3 dstCenter = target.getPortalCenter();
        Vec3 rel = hit.subtract(srcCenter);
        double u = rel.dot(getPlaneRight());
        double v = rel.y;
        double w = rel.dot(getPlaneNormal());
        Vec3 dstRight = target.getPlaneRight();
        Vec3 dstNormal = target.getPlaneNormal();
        Vec3 newPos = dstCenter.add(dstRight.scale(-u)).add(0, v, 0).add(dstNormal.scale(-w));
        if (entity instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.teleport(
                    newPos.x, newPos.y, newPos.z,
                    serverPlayer.getYRot(), serverPlayer.getXRot(),
                    EnumSet.of(ClientboundPlayerPositionPacket.RelativeArgument.X
                            , ClientboundPlayerPositionPacket.RelativeArgument.Y
                            , ClientboundPlayerPositionPacket.RelativeArgument.Z));
        } else {
            Vec3 motion = entity.getDeltaMovement();
            entity.teleportTo(newPos.x, newPos.y, newPos.z);
            if (entity.level instanceof ServerLevel serverLevel) {
                serverLevel.getChunkSource().broadcast(entity, new ClientboundTeleportEntityPacket(entity));
            }
            entity.setDeltaMovement(motion);
            if (entity instanceof Mob mob) {
                mob.getNavigation().stop();
            }
        }
        entity.getPersistentData().putLong("PVZPortalCooldown", level.getGameTime() + COOLDOWN);
    }

    public Vec3 getPlaneNormal() {
        Vec3 to = getPairPos().subtract(position());
        Vec3 horizontal = new Vec3(to.x, 0, to.z);
        if (horizontal.lengthSqr() > 1E-4) {
            return horizontal.normalize();
        }
        float rad = - this.getYRot() * Mth.DEG_TO_RAD - Mth.PI;
        return new Vec3(Mth.sin(rad), 0, Mth.cos(rad));
    }

    public Vec3 getPlaneRight() {
        Vec3 n = getPlaneNormal();
        return new Vec3(- n.z, 0, n.x);
    }

    @Nullable
    public Portal getPairedPortal() {
        Optional<UUID> uuid = entityData.get(PAIR_UUID);
        if (uuid.isEmpty() || !(level instanceof ServerLevel serverLevel)) return null;
        Entity entity = serverLevel.getEntity(uuid.get());
        return entity instanceof Portal portal ? portal : null;
    }

    public void setPair(@Nullable Portal other) {
        entityData.set(PAIR_UUID, other == null ? Optional.empty() : Optional.of(other.getUUID()));
        if (other != null) {
            this.entityData.set(PAIR_POS, other.position());
        }
    }

    public Vec3 getPairPos() {
        return entityData.get(PAIR_POS);
    }

    public void setPredicate(Predicate<Entity> filter) {
        this.predicate = filter;
    }

    public int getState() {
        return entityData.get(STATE);
    }

    public void setState(int state) {
        entityData.set(STATE, state);
    }

    public Vec3 getPortalCenter() {
        return position().add(0, HEIGHT / 2.0, 0);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID("Pair")) {
            entityData.set(PAIR_UUID, Optional.of(tag.getUUID("Pair")));
        }
        if (tag.contains("PairPosX")) {
            entityData.set(PAIR_POS, new Vec3(tag.getDouble("PairPosX"), tag.getDouble("PairPosY"), tag.getDouble("PairPosZ")));
        }
        setState(tag.getInt("State"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        entityData.get(PAIR_UUID).ifPresent(uuid -> tag.putUUID("Pair", uuid));
        Vec3 p = getPairPos();
        tag.putDouble("PairPosX", p.x); tag.putDouble("PairPosY", p.y); tag.putDouble("PairPosZ", p.z);
        tag.putInt("State", getState());
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}