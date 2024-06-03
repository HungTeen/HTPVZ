package com.hungteen.pvz.util;

import com.hungteen.pvz.PVZConfig;
import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.api.events.TeammateTestingEvent;
import com.hungteen.pvz.common.tags.PVZEntityTags;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Team;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Tags;

import javax.annotation.Nonnull;
import java.util.Random;

public class EntityUtil {
    public static final Random RAND = new Random();
    /**
     * use to play sound in world.
     */
    public static void playSound(Entity entity, SoundEvent ev) {
        if(ev != null) {
            entity.playSound(ev, 1.0F, RAND.nextFloat() * 0.2F + 0.9F);
        }
    }

    /**Check if entities are teammates. <b>CAN ONLY</b> call on server.
     * <br>I you want to check if an entity is attackable, use {@link EntityUtil#checkCanEntityBeAttack(Entity, Entity)}.*/
    public static boolean isTeammate(Entity A, Entity B) {
        boolean result;
        if (A == null || B == null) {
            PVZMod.LOGGER.error(A == null ? "A" : "B"+ " is null!");
            return false;
        }

        Team teamA = A.getTeam();
        Team teamB = B.getTeam();
        boolean AIsEnemy = (! A.getType().is(PVZEntityTags.FRIENDLY)) && (A instanceof Enemy || A.getType().is(PVZEntityTags.ENEMY) || A.getType().is(Tags.EntityTypes.BOSSES));
        boolean BIsEnemy = (! B.getType().is(PVZEntityTags.FRIENDLY)) && (B instanceof Enemy || B.getType().is(PVZEntityTags.ENEMY) || B.getType().is(Tags.EntityTypes.BOSSES));
        Team enemyTeam = A.getServer().getScoreboard().getPlayerTeam(PVZMod.ENEMY_TEAM);

        boolean teamBattle = PVZConfig.PVZGameRules.getBoolean(A.level, PVZConfig.Common.teamBattle);

        if (teamA == teamB) {
            result = teamA != null || (AIsEnemy == BIsEnemy);
        } else if (teamA == null) {
            result = (AIsEnemy) == (teamB == enemyTeam);
        } else if (teamB == null) {
            result = (BIsEnemy) == (teamA == enemyTeam);
        } else if (teamA == enemyTeam || teamB == enemyTeam) {
            result = false;
        } else {
            result = ! teamBattle;
        }
        TeammateTestingEvent event = new TeammateTestingEvent(A, B, result);
        MinecraftForge.EVENT_BUS.post(event);
        return event.currentResult;
    }
    /**
     * check can AttackGoal continue to attack target. <b>CAN ONLY</b> call on server.
     */
    public static boolean checkCanEntityBeAttack(Entity attacker, Entity target) {
        if (attacker == null || target == null) {//prevent crash
            return false;
        }
        if ((target instanceof Player && ! isSurvivalPlayer(target)) || ! isEntityValid(target)) {
            //the reason not testing whether attacker is valid: there may be some situations attacking when attacker is dead, such as for bomb plants.
            return false;
        }
        if (isTeammate(attacker, target)) {//enable team attack
            return false;
        }
        return true;
    }

    public static boolean isSurvivalPlayer(Entity entity) {
        return entity instanceof Player player && ! player.isCreative() && ! player.isSpectator();
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
        return entity.getLastHurtByMobTimestamp() < entity.tickCount - cd || entity.getLastHurtByMob() == null;
    }

    public static boolean hasRidingRelationship(Entity A, Entity B) {
        return isFinallyVehicleOf(A, B) || isFinallyVehicleOf(B, A);
    }

    private static boolean isFinallyVehicleOf(Entity A, Entity B) {
        if (B.getVehicle() == null) {
            return false;
        }
        return B.getVehicle() == A || isFinallyVehicleOf(A, B.getVehicle());
    }
}
