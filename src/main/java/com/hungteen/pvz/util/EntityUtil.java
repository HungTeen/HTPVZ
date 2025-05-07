package com.hungteen.pvz.util;

import com.hungteen.pvz.PVZConfig;
import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.api.events.CheckReteamableToOwnerEvent;
import com.hungteen.pvz.api.events.SculkJudgmentEvent;
import com.hungteen.pvz.api.events.TeammateTestingEvent;
import com.hungteen.pvz.common.register.PVZMobEffects;
import com.hungteen.pvz.common.tags.PVZBlockTags;
import com.hungteen.pvz.common.tags.PVZEntityTags;
import com.hungteen.pvz.common.world.team.PVZTeamData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Random;
import java.util.UUID;

public class EntityUtil {
    public static final Random random = new Random();


    //Basic
    public static boolean isEntityValid(Entity target) {
        return target != null && target.isAlive();
    }

    public static boolean attributeHasModifierOfUUID(LivingEntity entity, Attribute attribute, UUID uuid) {
        AttributeInstance attr = entity.getAttribute(attribute);
        return attr != null && attr.modifierById.keySet().stream().anyMatch(uuid1 -> uuid1.equals(uuid));
    }

    public static void removeModifierFromAttribute(LivingEntity entity, Attribute attribute, UUID uuid) {
        AttributeInstance attr = entity.getAttribute(attribute);
        if (attr != null) {
            attr.removeModifier(uuid);
        }
    }
    public static void addModifierToAttribute(LivingEntity entity, Attribute attribute, AttributeModifier modifier) {
        AttributeInstance attr = entity.getAttribute(attribute);
        if (attr != null && attr.modifierById.keySet().stream().noneMatch(uuid1 -> uuid1.equals(modifier.getId()))) {
            attr.addTransientModifier(modifier);
        }
    }


    //Environment
    /**{@link Entity#isOnGround()} sometimes cannot reflect actual situation. So added this.*/
    public static boolean isLeavingGround(Entity entity, double tolerance) {
        float width = entity.getBbWidth();
        if (entity.isPassenger()) return false;
        for (int x = -1; x < 2; x ++) {
            for (int z = -1; z < 2 ; z ++) {
                if (width < 1 && (x == 0 || z == 0)) continue;
                BlockPos pos = new BlockPos(entity.getX() + x * width * 0.5F ,entity.getOnPos().getY(), entity.getZ() + z * width * 0.5F);
                if (entity.getY() - entity.getOnPos().getY()
                        - Math.max(entity.level.getBlockState(pos).getCollisionShape(entity.level, pos).max(Direction.Axis.Y), 0)
                         < tolerance) {
                    return false;
                }
            }
        }
        return true;
    }

    public static Fluid getFluidEntityIn(LivingEntity entity) {
        if (! entity.isInFluidType()) {
            return null;
        }
        final Fluid[] fluid = new Fluid[1];
        ForgeRegistries.FLUIDS.forEach(fluid1 -> {
            if (entity.isInFluidType()) fluid[0] = fluid1;
        });
        return Arrays.stream(fluid).findAny().orElse(null);
    }

    public static boolean isLeavingGround(Entity entity) {
        return isLeavingGround(entity, 0.0001);
    }

    public static boolean isSculk(LivingEntity entity) {
        SculkJudgmentEvent event = new SculkJudgmentEvent(entity,
                entity.level.getBlockState(entity.getOnPos()).is(PVZBlockTags.SCULK) || entity.hasEffect(MobEffects.DARKNESS)
                        && ! entity.hasEffect(PVZMobEffects.BRIGHTNESS.get())
        );
        MinecraftForge.EVENT_BUS.post(event);
        return event.result;
    }


    //Hating & Teaming
    /**Check if entities are teammates. <b>CAN ONLY</b> call on server.
     * <br>I you want to check if an entity is attackable, use {@link EntityUtil#checkCanEntityBeAttack(Entity, Entity)}.*/
    public static boolean isTeammate(Entity A, Entity B) {
        boolean result;
        if (A == null || B == null) {
            PVZMod.LOGGER.error(A == null ? "A" : "B" + " is null!");
            return false;
        }

        Team teamA = A.getTeam();
        Team teamB = B.getTeam();
        boolean AIsEnemy = (! A.getType().is(PVZEntityTags.FRIENDLY)) && (A instanceof Enemy || A.getType().is(PVZEntityTags.ENEMY) || A.getType().is(Tags.EntityTypes.BOSSES));
        boolean BIsEnemy = (! B.getType().is(PVZEntityTags.FRIENDLY)) && (B instanceof Enemy || B.getType().is(PVZEntityTags.ENEMY) || B.getType().is(Tags.EntityTypes.BOSSES));
        Scoreboard scoreboard = A.getServer().getScoreboard();

        boolean teamBattle = PVZConfig.PVZGameRules.getBoolean(A.level, PVZConfig.Common.teamBattle);

        if (teamA == teamB) {
            result = teamA != null || (AIsEnemy == BIsEnemy);
        } else if (teamA == null) {
            result = PVZTeamData.isEvil(scoreboard, teamB.getName()) == AIsEnemy;
        } else if (teamB == null) {
            result = PVZTeamData.isEvil(scoreboard, teamA.getName()) == BIsEnemy;
        } else if (PVZTeamData.isEvil(scoreboard, teamA.getName()) || PVZTeamData.isEvil(scoreboard, teamB.getName())) {
            result = false;
        } else if (! PVZTeamData.isEvil(scoreboard, teamA.getName()) || ! PVZTeamData.isEvil(scoreboard, teamB.getName())) {
            result = true;
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
        //TODO enable Player#canHarmPlayer().
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

    public static boolean isEntityPeace(LivingEntity entity, int cd) {
        return entity.getLastHurtByMobTimestamp() < entity.tickCount - cd || entity.getLastHurtByMob() == null;
    }

    public static boolean canReteamToOwner(Entity entity, Entity owner) {
        CheckReteamableToOwnerEvent event = new CheckReteamableToOwnerEvent(entity, owner,
                (! (entity instanceof LivingEntity living)) || ! living.hasEffect(PVZMobEffects.HYPNOTISED.get()));
        MinecraftForge.EVENT_BUS.post(event);
        return event.result;
    }


    //Riding
    public static boolean hasRidingRelationship(Entity A, Entity B) {
        return isFinallyVehicleOf(A, B) || isFinallyVehicleOf(B, A);
    }

    private static boolean isFinallyVehicleOf(Entity A, Entity B) {
        if (B.getVehicle() == null) {
            return false;
        }
        return B.getVehicle() == A || isFinallyVehicleOf(A, B.getVehicle());
    }


    //Others
    /**
     * use to play sound in world.
     */
    public static void playSound(Entity entity, SoundEvent ev) {
        if(ev != null) {
            entity.playSound(ev, 1.0F, random.nextFloat() * 0.2F + 0.9F);
        }
    }

    public static Vec3 getNormalisedVector2d(@Nonnull Entity a, @Nonnull Entity b) {
        final double dx = b.getX() - a.getX();
        final double dz = b.getZ() - a.getZ();
        final double dis = Math.sqrt(dx * dx + dz * dz);
        return new Vec3(dis == 0 ? 0 : dx / dis, 0, dis == 0 ? 0 : dz / dis);
    }
}
