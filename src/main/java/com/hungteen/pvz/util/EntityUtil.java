package com.hungteen.pvz.util;

import com.hungteen.pvz.common.capability.owned.PVZOwnedCapability;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.Random;

public class EntityUtil {
    public static final Random RAND = new Random();
    /**
     * spawn in random range position.
     */
    public static void onEntityRandomPosSpawn(Level level, Entity entity, BlockPos pos, int dis) {
        pos = pos.offset(MathUtil.getRandomInRange(level.getRandom(), dis), level.getRandom().nextInt(dis) + 1, MathUtil.getRandomInRange(level.getRandom(), dis));
        onEntitySpawn(level, entity, pos);
    }
    /**
     * use to spawn entity in world.
     */
    public static void onEntitySpawn(Level level, Entity entity, BlockPos pos) {
        entity.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        if(entity instanceof Mob mob && level instanceof ServerLevel serverLevel) {
            mob.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(entity.blockPosition()), MobSpawnType.SPAWNER, null, null);
        }
        level.addFreshEntity(entity);
    }
    /**
     * use to play sound in world.
     */
    public static void playSound(Entity entity, SoundEvent ev) {
        if(ev != null) {
            entity.playSound(ev, 1.0F, RAND.nextFloat() * 0.2F + 0.9F);
        }
    }

    /**
     * check can AttackGoal continue to attack target.
     */
    public static boolean checkCanEntityBeAttack(Entity attacker, Entity target) {
        if(attacker == null || target == null) {//prevent crash
            return false;
        }
        if(!isSurvivalPlayer(target)) {
            return false;
        }
        if(PVZOwnedCapability.isTeammate(attacker, target)) {//enable team attack
            return false;
        }
        return true;
    }
    public static boolean isSurvivalPlayer(Entity entity) {
        return entity instanceof Player player&&! player.isCreative() && ! player.isSpectator();
    }

    public static class EntitySorter implements Comparator<Entity> {
        private final Entity entity;

        public EntitySorter(Entity entityIn) {
            this.entity = entityIn;
        }

        public int compare(Entity a, Entity b) {
            double d0 = this.entity.distanceToSqr(a);
            double d1 = this.entity.distanceToSqr(b);
            if (d0 < d1) {
                return -1;
            } else {
                return d0 > d1 ? 1 : 0;
            }
        }
    }
    public static Vec3 getNormalisedVector2d(@Nonnull Entity a, @Nonnull Entity b) {
        final double dx = b.getX() - a.getX();
        final double dz = b.getZ() - a.getZ();
        final double dis = Math.sqrt(dx * dx + dz * dz);
        return new Vec3(dis == 0 ? 0 : dx / dis, 0, dis == 0 ? 0 : dz / dis);
    }

    public static boolean isEntityValid(Entity target) {
        return target != null && target.isAlive();
    }
    public static boolean isEntityPeace(LivingEntity entity, int cd) {
        return entity.getLastHurtByMobTimestamp() < entity.tickCount-cd || entity.getLastHurtByMob()==null;
    }

}
