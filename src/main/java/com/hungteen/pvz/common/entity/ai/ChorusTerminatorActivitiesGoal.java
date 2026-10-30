package com.hungteen.pvz.common.entity.ai;

import com.hungteen.pvz.api.interfaces.IPlant;
import com.hungteen.pvz.common.capability.entity.PVZEntityCapability;
import com.hungteen.pvz.common.entity.bullet.ChorusTerminatorBullet;
import com.hungteen.pvz.common.entity.zombies.ChorusTerminatorBoss;
import com.hungteen.pvz.common.register.PVZEntities;
import com.hungteen.pvz.common.register.PVZItems;
import com.hungteen.pvz.common.register.PVZMobEffects;
import com.hungteen.pvz.util.EntityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ChorusTerminatorActivitiesGoal extends Goal {
    public final ChorusTerminatorBoss boss;
    public final List<LivingEntity> enemies = new ArrayList<>();
    public final Map<BlockPos, Integer> groups = new HashMap<>();
    private int offBattleCount = 0;
    public BlockPos targetGroup;

    public ChorusTerminatorActivitiesGoal(ChorusTerminatorBoss boss) {
        this.boss = boss;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return true;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = boss.getTarget();
        if (targetGroup != null) {
            this.boss.getLookControl().setLookAt(targetGroup.getX(), targetGroup.getY(), targetGroup.getZ());
        } else if (target != null) {
            this.boss.getLookControl().setLookAt(target.getX(), target.getEyeY(), target.getZ());
        }
        int phase = boss.getPhase();
        // targeting
        if (boss.getLastHurtByPlayer() instanceof ServerPlayer p && p.isAlive() && EntityUtil.isSurvivalPlayer(p)) boss.setTarget(p);
        if (target == null) {
            if (! enemies.isEmpty()) {
                boss.setTarget(enemies.iterator().next());
            } else {
                if (offBattleCount < 600) offBattleCount ++;
            }
        } else {
            offBattleCount = 0;
        }
        // group detect
        if (boss.tickCount % 20 == 0) {
            enemies.removeIf(entity -> ! entity.isAlive());
            BlockPos home = boss.homePos;
            if (home != null) {
                LivingEntity bossTarget = boss.getTarget();
                if (EntityUtil.isEntityValid(bossTarget)) {
                    for (Mob mob : boss.level.getEntitiesOfClass(Mob.class, new AABB(home).inflate(24, 6, 24),
                            e -> e.isAlive() && PVZEntityCapability.getOwner(e) == boss && ! EntityUtil.isEntityValid(e.getTarget()))) {
                        mob.setTarget(bossTarget);
                    }
                }
                List<LivingEntity> nearby = boss.level.getEntitiesOfClass(LivingEntity.class,
                        new AABB(home).inflate(24, 6, 24),
                        e -> e instanceof IPlant && e.isAlive());
                enemies.clear();
                enemies.addAll(nearby);
                LivingEntity lastHurt = boss.getLastHurtByMob();
                if (lastHurt != null && ! (lastHurt instanceof Player)) enemies.add(lastHurt);

                BlockPos origin = home.offset(-24, -6, -24);
                for (int bx = 0; bx < 6; bx++) {
                    for (int bz = 0; bz < 6; bz++) {
                        AABB blockBox = new AABB(
                                origin.offset(bx * 8, 0, bz * 8),
                                origin.offset(bx * 8 + 7, 11, bz * 8 + 7));
                        List<Vec3> inBlock = plantsIn(blockBox);
                        if (inBlock.size() <= 8) continue;
                        Vec3 avg1 = average(inBlock);
                        if (hasNearbyGroup(avg1)) continue;
                        List<Vec3> inMid = plantsIn(boxAround(avg1, 4, 2, 4));
                        if (inMid.size() <= 8) continue;
                        Vec3 avg2 = average(inMid);
                        if (hasNearbyGroup(avg2)) continue;
                        List<Vec3> inFinal = plantsIn(boxAround(avg2, 5, 2, 5));
                        groups.put(new BlockPos(average(inFinal)), inFinal.size());
                    }
                }
                groups.entrySet().removeIf(entry ->
                        plantsIn(boxAround(Vec3.atCenterOf(entry.getKey()), 5, 2, 5)).size() <= 10);
                if (targetGroup != null && ! groups.containsKey(targetGroup)) targetGroup = null;
            }
        }
        //phase
        if (target != null) {
            float healthPercent = boss.getHealth() / boss.getMaxHealth();
            this.boss.setPhase(healthPercent < 0.5 ? 2 : healthPercent < 0.833 ? 1 : 0);
        }
        //position
        int position = -1;
        BlockPos center = boss.homePos;
        if (center != null) {
            BlockPos bossPos = boss.blockPosition();
            int dx = bossPos.getX() - center.getX();
            int dy = bossPos.getY() - center.getY();
            int dz = bossPos.getZ() - center.getZ();
            if (dy > 3) {
                position = -2;
            } else if (Math.abs(dx) < 21 && Math.abs(dz) < 21) {
                position = (dx * dx + dz * dz < 9) ? 0 : (Math.abs(dx) < 13 && Math.abs(dz) < 13) ? 1 : 2;
            }
        }
        //action states
        switch (boss.getAction()) {
            case STIFF, SHORT_STIFF, DEFENCE_STIFF -> {
                setBody(14, new Vec3(0, 3, 0));
                setBody(24, new Vec3(0, 0, 0));
                setBody(boss.getAction() == Action.STIFF ? 220 : 80, new Vec3(0, 3, 0));
                targetGroup = null;
                boss.getNavigation().stop();
                if (boss.actionTime % 100 == 1) {
                    BlockPos home = boss.homePos;
                    boss.level.getNearbyPlayers(TargetingConditions.DEFAULT, boss
                                    , new AABB(home.getX() - 21, home.getY() - 3, home.getZ() - 21
                                            , home.getX() + 21, home.getY() + 15, home.getZ() + 21))
                            .forEach(player -> player.addEffect(new MobEffectInstance(PVZMobEffects.EXCITEMENT.get(), 100, 3)));
                }
                if (boss.actionTime >= (boss.getAction() == Action.STIFF ? 240 : 100)) {
                    if (boss.homePos != null) {
                        boss.act(Action.JUMP);
                        jumpTo(Vec3.atBottomCenterOf(boss.homePos));
                    } else {
                        boss.act(Action.IDLE);
                    }
                }
            }
            case IDLE -> {
                setBody(20, 80, new Vec3(0, 3, 0));
                int interval = phase >= 2 ? 80 : 160;
                if (boss.actionTime % 80 == 0) {
                    boss.getNavigation().stop();
                    targetGroup = groups.isEmpty() ? null
                            : groups.keySet().stream().toList().get(boss.getRandom().nextInt(groups.size()));
                }
                if (boss.homePos != null) {
                    if (boss.actionTime % interval == 0 && (this.boss.getShield() >= ChorusTerminatorBoss.MAX_SHIELD || offBattleCount >= 600)) {
                        boss.act(Action.PRE_DEFENCE);
                        if (offBattleCount >= 600) boss.setPhase(0);
                    } else if (boss.actionTime % interval == 0) {
                        if (position == -1) {
                            boss.act(Action.PRE_JUMP);
                        } else if ((boss.lastAction != Action.SUMMON && phase == 2) || boss.getRandom().nextInt(3) > 0) {
                            boss.act(Action.SUMMON);
                        } else {
                            boss.act(boss.getRandom().nextInt(3) == 0 ? Action.PRE_DASH
                                    : (phase >= 2 && boss.getRandom().nextBoolean() ? Action.SHOOT_SHULKER : Action.SHOOT_BULLET));

                        }
                    } else if (boss.actionTime == 1 && boss.getRandom().nextBoolean()) {
                        Vec3 pos = getMovePos(phase, position);
                        if (pos.distanceToSqr(boss.position()) > 400) jumpTo(pos); else moveTo(pos);
                    }
                }
            }
            
            case PRE_DEFENCE -> {
                if (boss.actionTime >= 60) boss.act(Action.DEFENCE);
                setBody(40, new Vec3(0, 1, 0));
            }
            case DEFENCE -> {
                setBody(20, 80, new Vec3(0, 1, 0));
                if (boss.actionTime % 20 == 0 && offBattleCount >= 600) boss.heal(1);
                if (boss.actionTime % (120 - phase * 40) == 0 && target != null) {
                    if (boss.lastAction != Action.DEFENCE_SUMMON && boss.getRandom().nextInt(3) > 0) {
                        boss.act(Action.DEFENCE_SUMMON);
                    } else {
                        boss.act(Action.DEFENCE_SHOOT);
                    }
                } else if (boss.actionTime == 1 && boss.getRandom().nextBoolean()) {
                    Vec3 pos = getMovePos(phase, position);
                    teleportTo(pos);
                }
            }

            case PRE_DASH -> {
                setBody(40, new Vec3(0, 4, 0));
                setBody(83, new Vec3(0, 3, 0));
                if (boss.actionTime == 80) {
                    if (targetGroup != null) {
                        moveTo(Vec3.atBottomCenterOf(targetGroup));
                    } else if (target != null) {
                        moveTo(target);
                    }
                }
                if (boss.actionTime >= 100) {
                    if (! boss.getNavigation().isDone()) {
                        boss.act(Action.DASH);
                    } else {
                        boss.act(Action.PRE_JUMP);
                    }
                }
            }
            case DASH -> {
                if (boss.getNavigation().isDone() || boss.actionTime >= 300) {
                    boss.act(Action.JUMP);
                    BlockPos home = boss.homePos;
                    if (home != null) {
                        jumpTo(Vec3.atBottomCenterOf(home));
                    } else {
                        boss.act(Action.IDLE);
                    }
                }
            }

            case PRE_JUMP -> {
                boss.getNavigation().stop();
                if (boss.actionTime == 96) {
                    BlockPos home = boss.homePos;
                    if (home != null) {
                        Vec3 jumpPos = Vec3.atCenterOf(home);
                        if (hasNearbyGroup(jumpPos) || boss.position().distanceToSqr(jumpPos) <= 16) {
                            boolean found = false;
                            for (int i = 0; i < 5; i++) {
                                Vec3 candidate = jumpPos.add(
                                        boss.getRandom().nextInt(36) - 18, 0,
                                        boss.getRandom().nextInt(36) - 18);
                                if (! hasNearbyGroup(candidate)) {
                                    jumpPos = candidate;
                                    found = true;
                                    break;
                                }
                            }
                            if (! found) jumpPos = Vec3.atCenterOf(home);
                        }
                        jumpTo(new Vec3(jumpPos.x, home.getY(), jumpPos.z));
                    }
                }
                if (boss.actionTime == 100) boss.act(Action.JUMP);
            }
            case JUMP -> {
                if (Arrays.stream(boss.legs).noneMatch(EntityUtil::isLeavingGround)) {
                    boss.act(Action.IDLE);
                }
            }

            case DEFENCE_SHOOT -> {
                setBody(40, new Vec3(0, 3, 0));
                setBody(119, new Vec3(0, 1, 0));
                if (boss.actionTime >= 120) boss.act(Action.DEFENCE);
                if (boss.actionTime == 88 || boss.actionTime == 90 || boss.actionTime == 92) {
                    Vec3 bulletTarget = this.boss.position()
                            .add(boss.getRandom().nextInt(40) - 20, 0, boss.getRandom().nextInt(40) - 20);
                    if (boss.actionTime > 90) {
                        if (! groups.isEmpty()) {
                            bulletTarget = Vec3.atBottomCenterOf(groups.keySet().stream().toList().get(boss.getRandom().nextInt(groups.size())));
                        } else if (! enemies.isEmpty()) {
                            bulletTarget = enemies.get(boss.getRandom().nextInt(enemies.size())).position();
                        } else if (target != null && boss.actionTime > 92) {
                            bulletTarget = target.position();
                        }
                    }
                    ChorusTerminatorBullet bullet = new ChorusTerminatorBullet(boss.level, boss, bulletTarget, true);
                    bullet.setPos(boss.eye.position().add(0, 1.5, 0));
                    float speed = boss.getRandom().nextFloat() * 0.5f + 0.8f;
                    bullet.setDeltaMovement(boss.eye.position().subtract(boss.body.position()).multiply(1, 0, 1).normalize().multiply(speed, speed, speed));
                    if (phase != 0 && boss.getRandom().nextInt(4) <= phase) {
                        bullet.setChorus(false);
                    }
                    boss.level.addFreshEntity(bullet);
                }
            }
            case SHOOT_BULLET -> {
                if (boss.actionTime >= 120) boss.act(Action.IDLE);
                if (boss.actionTime == 68 || boss.actionTime == 70 || boss.actionTime == 72) {
                    Vec3 bulletTarget = this.boss.position()
                            .add(boss.getRandom().nextInt(40) - 20, 0, boss.getRandom().nextInt(40) - 20);
                    if (boss.actionTime > 70) {
                        if (! groups.isEmpty()) {
                            bulletTarget = Vec3.atBottomCenterOf(groups.keySet().stream().toList().get(boss.getRandom().nextInt(groups.size())));
                        } else if (! enemies.isEmpty()) {
                            bulletTarget = enemies.get(boss.getRandom().nextInt(enemies.size())).position();
                        } else if (target != null && boss.actionTime > 72) {
                            bulletTarget = target.position();
                        }
                    }
                    ChorusTerminatorBullet bullet = new ChorusTerminatorBullet(boss.level, boss, bulletTarget, true);
                    bullet.setPos(boss.eye.position().add(0, 1.5, 0));
                    float speed = boss.getRandom().nextFloat() * 0.5f + 0.8f;
                    bullet.setDeltaMovement(boss.eye.position().subtract(boss.body.position()).multiply(1, 0, 1).normalize().multiply(speed, speed, speed));
                    if (phase != 0 && boss.getRandom().nextInt(4) <= phase) {
                        bullet.setChorus(false);
                    }
                    boss.level.addFreshEntity(bullet);
                }
            }
            case SHOOT_SHULKER -> {
                if (boss.actionTime >= 120) boss.act(Action.IDLE);
                if (boss.actionTime == 70) {
                }
            }

            case SUMMON, DEFENCE_SUMMON -> {
                if (boss.getAction() == Action.SUMMON) {
                    setBody(20, new Vec3(0, 1, 0));
                    setBody(40, new Vec3(0, 2, 0));
                    setBody(150, new Vec3(0, 3, 0));
                } else {
                    setBody(40, new Vec3(0, 2, 0));
                    setBody(150, new Vec3(0, 1, 0));
                }
                setMouth(40, new Vec3(0, -2, 0));
                setMouth(140, new Vec3(0, 0, 0));
                if (boss.actionTime >= 160) boss.act(this.boss.getAction() == Action.SUMMON ? Action.IDLE : Action.DEFENCE);
                BlockPos home = boss.homePos;
                if (boss.actionTime > 50 && boss.actionTime < 130) {
                    if (home != null) {
                        if (boss.actionTime % (30 - phase * 5) == 0) {
                            boss.level.getNearbyPlayers(TargetingConditions.DEFAULT, boss
                                    , new AABB(home.getX() - 21, home.getY() - 3, home.getZ() - 21
                                            , home.getX() + 21, home.getY() + 15, home.getZ() + 21))
                                    .forEach(player -> player.addEffect(new MobEffectInstance(PVZMobEffects.EXCITEMENT.get(), 40, 3)));
                            Map<EntityType<? extends Mob>, Integer> map = new HashMap<>();
                            map.put(PVZEntities.ZOMBIE.get(), 3);
                            map.put(PVZEntities.LADDER_ZOMBIE.get(), 3);
                            if (phase == 1) {
                                map.put(PVZEntities.PEA_SHOOTER_ZOMBIE.get(), 2);
                                map.put(PVZEntities.WALL_NUT_ZOMBIE.get(), 2);
                                map.put(PVZEntities.SNOW_PEA_ZOMBIE.get(), 2);
                            }
                            if (phase == 2) {
                                map.put(PVZEntities.PEA_SHOOTER_ZOMBIE.get(), 2);
                                map.put(PVZEntities.WALL_NUT_ZOMBIE.get(), 2);
                                map.put(PVZEntities.GARGANTUAR.get(), 1);
                                map.put(PVZEntities.SNOW_PEA_ZOMBIE.get(), 2);
                                map.put(PVZEntities.PUMPKIN_ZOMBIE.get(), 2);
                                map.put(PVZEntities.GATLING_PEA_ZOMBIE.get(), 1);
                                map.put(PVZEntities.TALL_NUT_ZOMBIE.get(), 1);
                            }
                            AtomicInteger choice = new AtomicInteger(0);
                            map.values().forEach(choice::addAndGet);
                            choice.set(boss.getRandom().nextInt(choice.get()));
                            EntityType<? extends Mob> entityType = PVZEntities.ZOMBIE.get();
                            for (EntityType<? extends Mob> typeToChoose : map.keySet()) {
                                int i1 = choice.addAndGet(- map.get(typeToChoose));
                                if (i1 < 0) {
                                    entityType = typeToChoose;
                                    break;
                                }
                            }
                            Mob zombie = entityType.create(boss.level);
                            zombie.setPos(boss.position().add(0, 1.5, 0));
                            if (phase > 0 && entityType == PVZEntities.ZOMBIE.get()) {
                                zombie.equipItemIfPossible((phase > 1 ? PVZItems.BUCKET_HELMET : PVZItems.CONE_HELMET).get().getDefaultInstance());
                                zombie.setDropChance(EquipmentSlot.HEAD, 0);
                            }
                            float sunBloodChance = 1 - phase / 2f;
                            if (sunBloodChance > 0 && boss.getRandom().nextFloat() < sunBloodChance) {
                                zombie.addEffect(new MobEffectInstance(PVZMobEffects.SUN_BLOOD.get(), 1200));
                            }
                            zombie.finalizeSpawn((ServerLevel) boss.level, boss.level.getCurrentDifficultyAt(boss.blockPosition())
                                    , MobSpawnType.MOB_SUMMONED, null, null);

                            boss.level.addFreshEntity(zombie);
                            zombie.getCapability(PVZEntityCapability.CAP).ifPresent(cap -> cap.setOwner(boss));
                            if (targetGroup != null) {
                                LivingEntity entity = boss.level.getNearestEntity(boss.level.getEntitiesOfClass(LivingEntity.class
                                                , new AABB(targetGroup.offset(-6, -2, -6)
                                                        , targetGroup.offset(6, 2, 6))
                                                , e -> ! EntityUtil.isTeammate(boss, e))
                                        , TargetingConditions.DEFAULT, zombie, zombie.getX(), zombie.getY(), zombie.getZ());
                                zombie.setTarget(entity);
                                zombie.getNavigation().moveTo(targetGroup.getX() + 0.5, targetGroup.getY(), targetGroup.getZ() + 0.5, 1);
                            } else {
                                List<Player> players = boss.level.getNearbyPlayers(TargetingConditions.DEFAULT, boss,
                                        new AABB(home).inflate(24, 6, 24));
                                if (! players.isEmpty()) {
                                    Player player = players.get(boss.getRandom().nextInt(players.size()));
                                    zombie.setTarget(player);
                                    zombie.getNavigation().moveTo(player, 1);
                                }
                            }
                        }
                        RandomSource random = boss.getRandom();
                        for (int i = 0; i < 1000; i++) {
                            BlockPos pos = boss.homePos.offset(
                                    random.nextInt(42) - 21
                                    , random.nextInt(18) - 3
                                    , random.nextInt(42) - 21);
                            boss.level.getBlockState(pos).randomTick((ServerLevel) boss.level, pos, boss.level.random);
                        }
                    }
                }
            }
        }
    }

    private Vec3 getMovePos(int phase, int currentPos) {
        if (! EntityUtil.isEntityValid(boss.getTarget())) {
            return Vec3.atBottomCenterOf(boss.homePos);
        }
        if (currentPos >= 0) {
            if (phase <= 2 && boss.getRandom().nextInt(phase >= 1 ? 8 : 12) == 0) return boss.position();
            List<BlockPos> candidates = new ArrayList<>(Arrays.asList(boss.homePos.offset(18, 0, 0)
                    , boss.homePos.offset(-18, 0, 0)
                    , boss.homePos.offset(0, 0, 18)
                    , boss.homePos.offset(0, 0, -18)
                    , boss.homePos));
            if (phase >= 2) {
                candidates.addAll(Arrays.asList(boss.homePos.offset(5, 0, -5)
                        , boss.homePos.offset(-5, 0, -5)
                        , boss.homePos.offset(-5, 0, 5)
                        , boss.homePos.offset(5, 0, 5)));
            }
            while (! candidates.isEmpty()) {
                BlockPos candidate = candidates.remove(boss.getRandom().nextInt(candidates.size()));
                Vec3 pos = Vec3.atBottomCenterOf(candidate);
                if (! hasNearbyGroup(pos)) {
                    return pos;
                }
            }
            return Vec3.atBottomCenterOf(boss.homePos);
        }
        return Vec3.atBottomCenterOf(boss.homePos);
    }

    public void moveTo(Vec3 position) {
        boss.getNavigation().moveTo(position.x, position.y, position.z, 1);
    }

    public void moveTo(Entity target) {
        boss.getNavigation().moveTo(target, 1);
    }

    public void teleportTo(Vec3 position) {
        boss.teleportTo(position.x, position.y, position.z);
    }

    public void jumpTo(Vec3 position) {
        ((ChorusTerminatorBoss.ChorusTerminatorMoveControl) boss.getMoveControl()).jumpTo(position);
    }

    private void setBody(int timeStamp, Vec3 position) {
        setBody(timeStamp, 2100000000, position);
    }

    private void setBody(int timeStamp, int looptime, Vec3 position) {
        if (boss.actionTime % looptime == timeStamp) {
            this.boss.body.move(MoverType.SELF, position.subtract(this.boss.body.getIntention()));
            this.boss.body.setIntention(position);
        }
    }

    private void setMouth(int timeStamp, Vec3 position) {
        if (boss.actionTime == timeStamp) this.boss.mouth.setIntention(position);
    }

    private List<Vec3> plantsIn(AABB box) {
        List<Vec3> result = new ArrayList<>();
        for (LivingEntity entity : enemies) {
            if (entity.isAlive() && box.contains(entity.position())) {
                result.add(entity.position());
            }
        }
        return result;
    }

    private static Vec3 average(List<Vec3> positions) {
        Vec3 sum = Vec3.ZERO;
        for (Vec3 p : positions) sum = sum.add(p);
        return sum.scale(1.0 / Math.max(1, positions.size()));
    }

    private static AABB boxAround(Vec3 center, int hx, int hy, int hz) {
        return new AABB(
                center.x - hx, center.y - hy, center.z - hz,
                center.x + hx, center.y + hy, center.z + hz);
    }

    private boolean hasNearbyGroup(Vec3 pos) {
        for (BlockPos p : groups.keySet()) {
            if (p.distSqr(new BlockPos(pos)) <= 64) return true; // within 8 blocks
        }
        return false;
    }
    public enum Action {
        IDLE, STIFF, SHORT_STIFF, SUMMON, DEFENCE, DEFENCE_STIFF, DEFENCE_SUMMON,
        DEFENCE_SHOOT, SHOOT_SHULKER, SHOOT_BULLET,
        DIE, PRE_DASH, DASH, PRE_JUMP, JUMP, PRE_DEFENCE
    }
}
