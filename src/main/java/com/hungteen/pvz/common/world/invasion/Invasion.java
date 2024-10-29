package com.hungteen.pvz.common.world.invasion;

import com.hungteen.pvz.PVZConfig;
import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.api.ZombieEvent;
import com.hungteen.pvz.common.capability.entity.PVZEntityCapability;
import com.hungteen.pvz.common.capability.level.PVZZombieEventCapability;
import com.hungteen.pvz.common.capability.player.PVZPlayerCapability;
import com.hungteen.pvz.common.entity.EntityLifter;
import com.hungteen.pvz.common.entity.LootBag;
import com.hungteen.pvz.common.register.PVZEntities;
import com.hungteen.pvz.common.world.PathSeeker;
import com.hungteen.pvz.util.EntityUtil;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**This class is the instance of an invasion. For the types of invasion, check {@link InvasionType}.
 * <br> A director system is used in invasion, recording the atk, extent of being damaged, flee will and the activation of player and dynamically adjusting the difficulty.
 * <br> An invasion is devided into waves, which need player to beat most of their members to continue. When all waves get cleared, the invasion ends.*/
@Mod.EventBusSubscriber(modid = PVZMod.MODID)
public class Invasion extends ZombieEvent implements INBTSerializable<CompoundTag> {
    private static final Random random = new Random();
    public List<InvasionType> types;
    public int invasionLevel;
    public List<Wave> waves = new ArrayList<>();
    public int currentWave;
    /**
     * the threat of enemies already added to level.
     */
    public int currentWaveThreat;
    public int currentWaveTime;
    public int lastSpawnTime = 0;
    public int lastRemoveTime = 0;
    public int totalTime;
    public int expectedTotalTime;
    public int endTime = -1;
    /**
     * At start of a wave invasion generates enemies and put them in this map. When a wave ends the map refreshes.
     */
    private Map<CompoundTag/*enemy*/, Integer/*threat*/> waveEnemies = new HashMap<>();
    /**The entities summoned are put in this set preventing the enemy from summoning again.*/
    private final Set<CompoundTag/*enemy*/> summonedEntities = new HashSet<>();
    private ServerBossEvent invasionEvent;
    private float seekPositionHardness = 0;
    private PathSeeker pathSeeker;

    //director system
    private final Set<Integer> killCount = new HashSet<>();
    private Vec3 storedTargetPos = null;
    private int maxTargetTeammateCount = 1;
    private int targetTeammateCount = 0;

    public Invasion(Level level, List<InvasionType> types, LivingEntity target, BlockPos pos, int invasionLevel) {
        this(level, UUID.randomUUID(), types, target, pos, invasionLevel);
    }

    /**Used by {@link com.hungteen.pvz.common.register.PVZZombieEvents#fromTag(Level, UUID, CompoundTag) PVZZombieEvents#fromTag()} when syncing or reloading.*/
    public Invasion(Level level, UUID uuid, CompoundTag tag) {
        super(level, uuid, tag);
        this.deserializeNBT(tag);
        if (level instanceof ServerLevel serverLevel) {
            invasionEvent = new ServerBossEvent(Component.translatable("event.pvz.invasion",
                    uuid.toString() /*to let client identify which invasion it is, provide one extra argument than needed which is the uuid of the event.*/),
                    BossEvent.BossBarColor.GREEN, BossEvent.BossBarOverlay.PROGRESS);
            this.pathSeeker = new PathSeeker(serverLevel);
            this.lastRemoveTime = totalTime;
            this.lastSpawnTime = totalTime;
        }
    }

    public Invasion(Level level, UUID uuid, List<InvasionType> types, LivingEntity target, BlockPos pos, int invasionLevel) {
        this(level, uuid, types);
        this.target = target;
        this.targetUUID = target.getUUID();
        this.position = pos;
        this.invasionLevel = invasionLevel;
        this.expectedTotalTime = this.generateWaves();
        this.waveEnemies = generateEnemies(this.getCurrentWave().threat);
    }

    public Invasion(Level level, UUID uuid, List<InvasionType> types) {
        super(level, uuid);
        this.types = types;
        this.currentWave = 0;
        this.currentWaveTime = 0;
        this.currentWaveThreat = 0;
        this.totalTime = 0;
        this.invasionLevel = 5;
        if (level instanceof ServerLevel serverLevel) {
            invasionEvent = new ServerBossEvent(Component.translatable("event.pvz.invasion",
                    uuid.toString()),
                    BossEvent.BossBarColor.GREEN, BossEvent.BossBarOverlay.PROGRESS);
            this.pathSeeker = new PathSeeker(serverLevel);
        }
    }

    /**Determines whether an invasion can happen.*/
    public static boolean canHappenInvasion(LivingEntity target) {
        List<Entity> entities = target.level.getEntities(target, target.getBoundingBox().inflate(10));
        int obs = 0;
        for (Entity entity : entities){
            if (EntityUtil.isTeammate(entity, target)) {
                obs += 1;
            }
        }
        return obs < 15;
    }

    /**Generate waves. Attention that the waves generated here may not be the ones actually starts in the game. With the invasion going these values are dynamically adjusted.*/
    public int generateWaves() {
        final int totalLength = (int) (10000 * this.types.get(0).length() * ((this.invasionLevel) * 0.1 + 0.5) + 100);
        final int bigWaveNum = Math.max(1, random.nextInt((int) ((float) totalLength / 6000 + Math.max(0, (this.invasionLevel) / 3) + 1)));
        int length = 0;
        while (length < totalLength) {
            int threat = (int) (Math.pow((100 + length * 0.4), 1.05) * (0.8 + random.nextFloat() * 0.4));
            int waveLength = 200 * threat / (100 + length / 7);
            threat *= ((float) (this.invasionLevel) * 0.1 + 0.5);
            Difficulty difficulty = ((ServerLevel) level).getServer().getWorldData().getDifficulty();
            switch (difficulty) {
                case PEACEFUL -> threat = 0;
                case EASY -> threat *= 0.75;
                case HARD -> threat *= 1.25;
                default -> {}//normal difficulty or other possible situations.
            }
            boolean bigWave = (length / totalLength * bigWaveNum) != ((length + waveLength) / totalLength);
            if (bigWave) {
                if (this.waves.size() > 0) {
                    Wave lastWave = this.waves.get(this.waves.size() - 1);
                    lastWave.threat *= 0.8;
                    lastWave.minimumWaitTime += 150;
                    if (lastWave.maximumWaitTime < lastWave.minimumWaitTime) lastWave.maximumWaitTime = lastWave.minimumWaitTime;
                }
                threat *= 1.2;
                waveLength *= 1.5;
            }
            length += waveLength;
            threat = Math.max(100, threat);
            this.addWave(bigWave, threat, (int) Math.max(400, waveLength * 0.8), (int) Math.max(1000, waveLength * 1.5));
        }
        return totalLength;
    }

    public void addWave(boolean isBigWave, int threat, int minimumWaitTime, int maximumWaitTime) {
        this.waves.add(new Wave(isBigWave, threat, minimumWaitTime, maximumWaitTime));
    }

    @Override
    public void tick(TickEvent.ServerTickEvent ev) {
        this.currentWaveTime += 1;
        this.totalTime += 1;
        this.invasionEvent.setProgress(Mth.clamp(
                this.currentWave >= this.waves.size() - 1 ? 1 :
                        (this.currentWave + 1 + (float) this.currentWaveThreat / this.getCurrentWave().threat) / this.waves.size(),
                0.0F, 1.0F));
        if (this.endTime > 0) {
            this.endTime --;
            if (this.endTime <= 0) {
                this.remove();
            }
            super.tick(ev);
            return;
        }
        if (tickCount % 20 == 0) {
            if (this.waves.isEmpty()) {
                PVZMod.LOGGER.warn("Find an invasion with no waves! Removing it.");
                this.remove();
                return;
            }
            if (! this.isEnded()) {
                if (target != null && target.isDeadOrDying()) {
                    this.end(Vec3.atCenterOf(this.position), "event.pvz.invasion.end.fail", false);
                    return;
                }
                if (this.level.getDifficulty() == Difficulty.PEACEFUL) {
                    this.end(Vec3.atCenterOf(this.position), "event.pvz.invasion.end.peace", false);
                    return;
                }
                if (target != null &&
                        target.blockPosition().distSqr(this.position) > this.range * this.range * 10 &&
                        getPlayerFleeWill() - getPlayerActivation() > 1000) {
                    this.end(Vec3.atCenterOf(this.position), "event.pvz.invasion.end.escape", false);
                    return;
                }
            }
            //player.
            List<ServerPlayer> players = ((ServerLevel) level).getPlayers(player -> player.distanceToSqr(Vec3.atCenterOf(this.position)) < (this.range + 15) * (this.range + 15));
            Collection<ServerPlayer> players1 = this.invasionEvent.getPlayers();
            for (ServerPlayer player : players1) {
                if (!players.contains(player)) {
                    this.invasionEvent.removePlayer(player);
                } else {
                    players.remove(player);
                }
            }
            for (ServerPlayer player : players) {
                this.invasionEvent.addPlayer(player);
            }
            //switch wave.
            if (! this.level.isClientSide && (
                    ((this.members.isEmpty() || totalTime - lastRemoveTime > 150)
                            && this.waveEnemies.size() >= this.summonedEntities.size() /*enemies spawned up*/
                            && this.currentWaveTime > this.getCurrentWave().minimumWaitTime /*time up*/) ||
                    ((this.members.isEmpty() || totalTime - lastRemoveTime > 150 ||
                            (totalTime - lastSpawnTime > 150 && this.waveEnemies.size() < this.summonedEntities.size()) /*enemies can't spawn*/)
                            && this.waves.size() - 1 > this.currentWave /*not final wave*/
                            && this.currentWaveTime > this.getCurrentWave().maximumWaitTime /*time up*/)
            )) {
                this.switchWave();
            }
            if (this.currentWaveThreat == 0 && currentWaveTime > getCurrentWave().maximumWaitTime) {
                //in order not to pass threat to next wave.
                this.waveEnemies.clear();
                this.summonedEntities.clear();
                this.getCurrentWave().isGivenUp = true;
                this.switchWave();
            }
            //tracking target
            if (this.target != null && this.target.blockPosition().distSqr(this.position) > 64 && tickCount % 40 == 0) {
                this.position = this.position.offset(new Vec3i(
                        Math.min(Math.max(-1, target.getX() - this.position.getX()), 1),
                        Math.min(Math.max(-1, target.getY() - this.position.getY()), 1),
                        Math.min(Math.max(-1, target.getZ() - this.position.getZ()), 1)
                        ));
            }
            if (! this.level.isClientSide && target != null) {
                //summoning.
                Optional<CompoundTag/*enemy*/> optional = this.waveEnemies.keySet().stream().filter(entity -> ! this.summonedEntities.contains(entity)).findFirst();
                if (optional.isPresent()) {
                    CompoundTag entityData = optional.get();
                    if (summonEntity(entityData)) {
                        this.currentWaveThreat += this.waveEnemies.get(entityData);
                        this.summonedEntities.add(entityData);
                    }
                }
                //director system.
                List<Entity> teammates = level.getEntities(target,
                        AABB.ofSize(Vec3.atCenterOf(this.position), this.range * 2, this.range, this.range * 2),
                        entity -> ! (entity instanceof Projectile) && EntityUtil.isTeammate(target, entity));
                targetTeammateCount = teammates.size();
                maxTargetTeammateCount = Math.max(targetTeammateCount, maxTargetTeammateCount);
                if (PVZConfig.PVZGameRules.getBoolean(level, PVZConfig.Common.showInvasionDetails)) {
                    PVZMod.LOGGER.info(
                            " TIME " + this.currentWaveTime + "/" + this.totalTime + "/" + (int) (10000 * this.types.get(0).length() * ((this.invasionLevel) * 0.1 + 0.5) + 100) + " " +
                            this.getCurrentWave().minimumWaitTime + "/" + this.getCurrentWave().maximumWaitTime +
                            "\n WAVE " + (this.currentWave + 1) + "/" + this.waves.size() + "  THREAT " + this.currentWaveThreat + "/" + this.getCurrentWave().threat +
                            "\n ENEMIES " + this.summonedEntities.size() + "/" + this.members.size() + "/" + this.waveEnemies.size() + "  POS_SEKR " + pathSeeker.availablePositions.size() +
                            "\n DIR_SYS: " +
                            "\n  ATK " + getPlayerAttack() + " : " + (int) Math.max(-100, ((float) (this.waveEnemies.size() - this.members.size()) / this.waveEnemies.size()) * this.getCurrentWave().threat) + " " +
                            (Math.max(0, Math.min(this.getCurrentWave().maximumWaitTime / 2, this.getCurrentWave().minimumWaitTime * 2) - this.currentWaveTime) + 1) +
                            "\n  HRT " + getPlayerHurt() + " : " + this.members.size() * 2 + " " +
                            this.killCount.size() * 15 + " " +
                            (200 * (1 - this.targetTeammateCount / this.maxTargetTeammateCount)) +
                            "\n  FLE " + getPlayerFleeWill() + " : " + Math.max(1, 500 / (getPlayerAttack() + 1)) + " " +
                            Math.max(1, 100 / (getPlayerHurt() + 1)) + " " +
                            (target == null ? 0 : target.blockPosition().distSqr(this.position)) + " " +
                            ((int) this.seekPositionHardness * 4) + " " +
                            Math.max(0, this.totalTime - (int) (10000 * this.types.get(0).length() * ((this.invasionLevel) * 0.1 + 0.5) + 100)) +
                            "\n  ACT " + getPlayerActivation() + " : " + (getPlayerActivation() - (this.storedTargetPos == null ? 0 : (int) target.position().distanceToSqr(this.storedTargetPos) * 2)) + " " +
                            (this.storedTargetPos == null ? 0 : (int) target.position().distanceToSqr(this.storedTargetPos) * 2)
                    );
                }
                this.storedTargetPos = this.target.position();
                Set<Integer> killCount = Set.copyOf(this.killCount);
                for (int i : killCount) {
                    if (this.totalTime - i > 400) {
                        this.killCount.remove(i);
                    }
                }
            }
        }
        //path seeker
        if (target != null) {
            pathSeeker.targetPos = this.target.blockPosition();
            pathSeeker.center = this.position;
        }
        if (this.seekPositionHardness > 25) {
            this.pathSeeker.tick();
        }
        if (seekPositionHardness > 0) {
            this.seekPositionHardness -= 0.25 + 0.001 * seekPositionHardness;
        }
        super.tick(ev);
    }

    //director system
    /**Returns a relative value of the ability player and plants damage zombies.*/
    public int getPlayerAttack() {
        Wave wave = this.getCurrentWave();
        return (int) Math.max(-100, ((float) (this.waveEnemies.size() - this.members.size()) / this.waveEnemies.size()) * wave.threat)
                + (Math.max(0, Math.min(wave.maximumWaitTime / 2, wave.minimumWaitTime * 2) - this.currentWaveTime) + 1);
    }
    /**Returns a relative value of the ability plants prevent deaths on zombies.*/
    public int getPlayerHurt() {
        return (this.members.size() * 2) + this.killCount.size() * 15 + 200 * (1 - this.targetTeammateCount / this.maxTargetTeammateCount);
    }
    /**Return a relative value of the will player flee from the invasion.*/
    public int getPlayerFleeWill() {
        return (int) (Math.max(1, 500 / Math.max(1, getPlayerAttack() + 1)) + Math.max(1, 100 / Math.max(1, getPlayerHurt() + 1))
                + (target == null ? 0 : target.blockPosition().distSqr(this.position))) + (int) this.seekPositionHardness * 4 +
                Math.max(0, this.totalTime - (int) (10000 * this.types.get(0).length() * ((this.invasionLevel) * 0.1 + 0.5) + 100));
    }
    /**Return a relative value of the will player join the invasion.*/
    public int getPlayerActivation() {
        if (target == null) return 0;
        AtomicInteger cdCount = new AtomicInteger();
        if (target instanceof Player player) {
            player.getCooldowns().cooldowns.values().forEach(value ->
                    cdCount.addAndGet(50 * (value.endTime - player.getCooldowns().tickCount) / (value.endTime - value.startTime)));
        }
        return cdCount.get() + (this.storedTargetPos == null ? 0 : (int) target.position().distanceToSqr(this.storedTargetPos) * 2);
    }

    public Wave getCurrentWave() {
        return this.waves.get(this.currentWave);
    }

    @Override
    public void removeMember(Entity member) {
        super.removeMember(member);
        this.lastRemoveTime = totalTime;
        if (this.members.isEmpty() && this.currentWave >= this.waves.size() - 1 && this.summonedEntities.size() >= this.waveEnemies.size()) {
            end(member.position().add(0, member.getEyeHeight(), 0), "event.pvz.invasion.end.win", true);
        }
    }

    public boolean isEnded() {
        return this.endTime >= 0;
    }

    public void end(Vec3 position, String endType, boolean success) {
        InvasionType invasionType = this.types.get(0);
        if (invasionType != null && invasionType.loot().isPresent()) {
            LootBag.drop(level, new BlockPos(position), invasionType.loot().get(), this.invasionLevel / 2 + 4);
        }
        if (target instanceof Player player) {
            player.getCapability(PVZPlayerCapability.NBT)
                    .ifPresent(cap -> {
                        cap.setValue("invasion_difficulty",
                            (success ? (((int)(20 * (float) this.totalTime / this.expectedTotalTime) + cap.getValue("invasion_difficulty") * 4) / 5) :
                                    (int) (cap.getValue("invasion_difficulty") * 0.8F)));
                        cap.setValue("last_invasion", 0);
                    });
        }
        if (PVZConfig.PVZGameRules.getBoolean(level, PVZConfig.Common.showInvasionDetails)) {
            PVZMod.LOGGER.info("invasion ended with end component " + this.invasionEvent.getName() + ".");
        }
        this.invasionEvent.setName(Component.translatable(endType, uuid.toString()));
        this.endTime = 200;
    }

    public void switchWave() {
        if (this.currentWave < this.waves.size() - 1) {
            this.currentWave += 1;
            this.currentWaveTime = 0;
            this.currentWaveThreat = 0;
            if (getPlayerHurt() > 60) {
                this.getCurrentWave().threat *= (60F / getPlayerHurt());
            }
            if (getPlayerAttack() > 1200) {
                this.getCurrentWave().threat *= Math.min(2, (float) getPlayerAttack() / 1200);
            }
            for (Entity entity : this.members) {
                this.getCurrentWave().threat -= 20;
                if (target == null || (entity.distanceToSqr(target) >
                        ((level.clip(new ClipContext(Vec3.atCenterOf(position), target.position(), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, null)).getType()
                                == HitResult.Type.MISS) ? 576 : 64))) {
                    entity.discard();
                    this.getCurrentWave().threat += 100;
                }
            }
            this.getCurrentWave().threat = Math.max(0, this.getCurrentWave().threat);
            this.waveEnemies = generateEnemies(this.getCurrentWave().threat);
            this.summonedEntities.clear();
        } else if (this.members.isEmpty()) {
            PVZMod.LOGGER.warn("Invasion " + this.uuid + " has done all waves but is still trying to continue.");
            this.remove();
        } else {
            for (Entity entity : Set.copyOf(this.members)) {
                if (target == null || entity.distanceToSqr(target) > 256) {
                    entity.discard();
                    removeMember(entity);
                }
            }
        }
    }

    public Map<CompoundTag/*enemy*/, Integer/*threat*/> generateEnemies(int totalThreat) {
        int threat = 0;
        Map<CompoundTag/*enemy*/, Integer/*threat*/> enemies = new HashMap<>();
        if (this.getCurrentWave().isBigWave && types.get(0).flagEnemy().isPresent()) {
            enemies.put(types.get(0).flagEnemy().get(), 0);
        }
        while (threat <= totalThreat) {
            Pair<CompoundTag/*enemy*/, Integer/*threat*/> choice = selectEntity(totalThreat - threat);
            if (choice != null) {
                enemies.put(choice.getFirst(), choice.getSecond());
                threat += choice.getSecond();
            } else {
                break;
            }
        }
        return enemies;
    }

    /**
     * @return a pair of entity and the threat it cost, null for no available entity.
     */
    protected @Nullable Pair<CompoundTag/*enemy*/, Integer/*threat*/> selectEntity(int threatLeft) {
        List<InvasionType.EnemyType> enemyTypes = new ArrayList<>();
        AtomicInteger allWeight = new AtomicInteger();
        float threatFactor = 1;
        for (InvasionType type : this.types) {
            threatFactor *= type.threatFactor();
        }
        for (InvasionType type : this.types) {
            float finalThreatFactor = threatFactor;
            type.enemies().forEach(enemyType -> {
                if ((! enemyType.isElite() || this.getCurrentWave().isBigWave)
                        && threatLeft >= (enemyType.threat() * finalThreatFactor)
                        && (float) this.currentWave / this.waves.size() >= enemyType.startFrom()) {
                    enemyTypes.add(enemyType);
                    allWeight.addAndGet(enemyType.weight());
                }
            });
        }
        if (enemyTypes.isEmpty()) {
            return null;
        }
        AtomicInteger selected = new AtomicInteger(random.nextInt(allWeight.get()));
        for (InvasionType.EnemyType enemyType : enemyTypes) {
            selected.addAndGet(-enemyType.weight());
            if (selected.get() <= 0) {
                CompoundTag tag = enemyType.entityData().copy();
                tag.putInt("pvz_invasion_enemy_summoned_while_threat_left", threatLeft);//to avoid same hash.
                return Pair.of(tag, (int) (enemyType.threat() * threatFactor));
            }
        }
        return null;
    }

    private boolean summonEntity(CompoundTag entityData) {
        Entity entity = EntityType.loadEntityRecursive(entityData.copy(), level, Function.identity());
        Vec3 pos = getSpawningPosition(entity);
        if (entity == null) {
            return false;
        }
        if (pos == null) {
            entity.discard();
            return false;
        } else {
            BlockPos pos1 = new BlockPos(pos);
            if (! NaturalSpawner.isValidEmptySpawnBlock(level, pos1, level.getBlockState(pos1), level.getFluidState(pos1), entity.getType())) {
                return false;
            }
        }
        EntityLifter lifter = PVZEntities.ENTITY_LIFTER.get().create(this.level);
        lifter.setPos(pos);
        entity.setPos(pos.add(0, - entity.getBbHeight(), 0));
        entity.getRootVehicle().startRiding(lifter);
        if (PVZConfig.PVZGameRules.getBoolean(level, PVZConfig.Common.showInvasionDetails) && (entity instanceof LivingEntity living)) {
            living.addEffect(new MobEffectInstance(MobEffects.GLOWING, 10000));
            living.getIndirectPassengers().forEach(e -> {
                if (e instanceof LivingEntity living1) {
                    living1.addEffect(new MobEffectInstance(MobEffects.GLOWING, 10000));
                }
            });
        }
        this.types.forEach(type -> type.getModifiers().forEach(
                modifier -> modifier.accept(this, entity, this.waveEnemies.getOrDefault(entityData, 0))));
        ((ServerLevel) level).addFreshEntityWithPassengers(lifter);
        entity.getCapability(PVZEntityCapability.CAP).ifPresent(cap -> cap.zombieEventUUIDs.add(this.uuid));
        this.members.add(entity);
        PlayerTeam enemyTeam = entity.getServer().getScoreboard().getPlayerTeam(PVZMod.ENEMY_TEAM);
        String name = entity.getScoreboardName();
        if (enemyTeam != null) {
            entity.getServer().getScoreboard().addPlayerToTeam(name, enemyTeam);
        }
        lastSpawnTime = this.totalTime;
        return true;
    }

    protected @Nullable Vec3 getSpawningPosition(Entity entity) {
        Optional<Entity> member = this.members.stream().findFirst();
        Vec3 vec3 = null;
        if ((this.getCurrentWave().isBigWave || random.nextInt(3) != 0) &&
                member.isPresent() && (this.target == null || member.get().distanceToSqr(this.target) > 144)) {
            vec3 = generateSpawningPositionAround(entity, member.get().position());
        }
        if (vec3 == null) {
            vec3 = generateRandomSpawningPosition(entity);
        }
        return vec3;
    }

    protected @Nullable Vec3 generateRandomSpawningPosition(Entity entity) {
        Vec3 result = null;
        if (seekPositionHardness > 50 - 5 * pathSeeker.availablePositions.size()) {
            if (this.pathSeeker.availablePositions.isEmpty()) {
                seekPositionHardness += 8;
            } else if (target != null) {
                Vec3i pos = this.pathSeeker.availablePositions.stream().findFirst().get();
                this.pathSeeker.availablePositions.remove(pos);
                seekPositionHardness += 5;
                double dist = pos.distSqr(target.blockPosition());
                if (dist > 100 && dist < 574) {
                    double horizontalDistSqr = (pos.getX() - target.getX()) * (pos.getX() - target.getX()) + (pos.getZ() - target.getZ()) * (pos.getZ() - target.getZ());
                    double verticalDist = pos.getY() - target.getY();
                    if (verticalDist > 0 || verticalDist * verticalDist < 1.5 * horizontalDistSqr) {
                        result = Vec3.atBottomCenterOf(pos);
                    }
                }
                seekPositionHardness += result == null ? 10 : 5;
            }
        } else if (target != null) {
            if (entity instanceof PathfinderMob mob) {
                entity.setPos(this.target.position());
                Vec3 vec3 = DefaultRandomPos.getPosAway(mob, 24, 7, this.target.position());
                result = (vec3 == null || this.target.position().distanceToSqr(vec3) < 256) ? null : vec3;
            } else {
                double angle = random.nextDouble(Math.PI * 2);
                double radius = 12 + random.nextDouble(12);
                result = this.target.position().add(Math.sin(angle) * radius, 0, Math.cos(angle) * radius);
            }
            if (result == null) {
                seekPositionHardness += 8;
            }
            return result;
        }
        if (result != null && entity instanceof LivingEntity living) {
            if (level.getNearestEntity(level.getEntitiesOfClass(LivingEntity.class, entity.getBoundingBox().inflate(8),
                            entity1 -> EntityUtil.checkCanEntityBeAttack(entity, entity1)), TargetingConditions.forCombat(), living,
                    result.x, result.y, result.z) != null) {
                seekPositionHardness += 5;
                return null;
            }
        }
        return result;
    }

    protected @Nullable Vec3 generateSpawningPositionAround(Entity entity, Vec3 originSpot) {
        Vec3 result = null;
        if (entity instanceof PathfinderMob mob && this.target != null) {
            entity.setPos(originSpot);
            result = DefaultRandomPos.getPosAway(mob, 8, 3, this.target.position());
        } else if (target != null) {
            double angle = random.nextDouble(Math.PI * 2);
            double radius = random.nextDouble(8);
            result = this.target.position().add(Math.sin(angle) * radius, 0, Math.cos(angle) * radius);
        }
        if (result == null) {
            seekPositionHardness += 12;
        }
        if (result != null && entity instanceof LivingEntity living) {
            if (level.getNearestEntity(level.getEntitiesOfClass(LivingEntity.class, entity.getBoundingBox().inflate(8),
                            entity1 -> EntityUtil.checkCanEntityBeAttack(entity, entity1)), TargetingConditions.forCombat(), living,
                    result.x, result.y, result.z) != null) {
                seekPositionHardness += 5;
                return null;
            }
        }
        return result;
    }

    //overrides
    @Override
    public void remove() {
        super.remove();
        this.invasionEvent.setVisible(false);
        this.invasionEvent.removeAllPlayers();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = super.serializeNBT();
        if (! this.types.isEmpty()) {
            ListTag types = new ListTag();
            for (InvasionType type : this.types) {
                if (type.getName() != null) {
                    ResourceLocation location = type.getName();
                    if (location == null) {
                        PVZMod.LOGGER.warn("Trying to save an invasion type which is not registered.");
                    } else {
                        types.add(StringTag.valueOf(location.toString()));
                    }
                }
            }
            tag.put("invasion_types", types);
        }
        tag.putInt("total_time", totalTime);
        tag.putInt("end_time", endTime);
        tag.putInt("expected_total_time", expectedTotalTime);
        tag.putInt("current_wave_threat", currentWaveThreat);
        tag.putInt("current_wave_time", currentWaveTime);
        tag.putInt("current_wave", currentWave);
        tag.putInt("invasion_level", invasionLevel);
        if (! this.waves.isEmpty()) {
            ListTag waves = new ListTag();
            waves.addAll(this.waves.stream().map(Wave::serializeNBT).toList());
            tag.put("waves", waves);
        }
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        super.deserializeNBT(tag);
        if (! this.level.isClientSide) {
            if (tag.contains("invasion_types")) {
                this.types = new ArrayList<>();
                ListTag types = ((ListTag) tag.get("invasion_types"));
                for (int i = 0; i < (types != null ? types.size() : 0); i ++) {
                    InvasionType type = InvasionType.getInvasionType(new ResourceLocation(types.getString(i)));
                    if (type == null) {
                        PVZMod.LOGGER.warn("Trying to load an invasion type with name" + new ResourceLocation(types.getString(i)) + " which is unavailable.");
                    } else {
                        this.types.add(type);
                    }
                }
            }
            if (this.types.isEmpty() || this.types.get(0).isAddition()) {
                PVZMod.LOGGER.warn("Trying to load an invasion which contains no main invasion type.");
                remove();
                return;
            }
        }
        if (tag.contains("waves")) {
            this.waves = new ArrayList<>();
            ListTag waves = ((ListTag) tag.get("waves"));
            for (int i = 0; i < (waves != null ? waves.size() : 0); i ++) {
                this.waves.add(new Wave(waves.getCompound(i)));
            }
        }
        this.currentWave = tag.getInt("current_wave");
        this.currentWaveThreat = tag.getInt("current_wave_threat");
        this.currentWaveTime = tag.getInt("current_wave_time");
        this.totalTime = tag.getInt("total_time");
        this.endTime = tag.getInt("end_time");
        this.expectedTotalTime = tag.getInt("expected_total_time");
        if (this.level instanceof ServerLevel) {
            this.waveEnemies = generateEnemies(this.getCurrentWave().threat - this.currentWaveThreat);
        }
        this.invasionLevel = tag.getInt("invasion_level");
    }

    public static class Wave implements INBTSerializable<CompoundTag> {
        public boolean isBigWave;
        public int threat;
        public int maximumWaitTime;
        public int minimumWaitTime;
        public boolean isGivenUp;

        public Wave(boolean isBigWave, int threat, int minimumWaitTime, int maximumWaitTime) {
            this.isBigWave = isBigWave;
            this.threat = threat;
            this.maximumWaitTime = maximumWaitTime;
            this.minimumWaitTime = minimumWaitTime;
            this.isGivenUp = false;
        }
        public Wave(CompoundTag tag) {
            this.deserializeNBT(tag);
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();
            tag.putBoolean("is_big_wave", this.isBigWave);
            tag.putInt("threat", this.threat);
            tag.putInt("minimum_wait_time", this.minimumWaitTime);
            tag.putInt("maximum_wait_time", this.maximumWaitTime);
            tag.putBoolean("is_given_up", this.isGivenUp);
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            isBigWave = tag.getBoolean("is_big_wave");
            threat = tag.getInt("threat");
            minimumWaitTime = tag.getInt("minimum_wait_time");
            maximumWaitTime = tag.getInt("maximum_wait_time");
            isGivenUp = tag.getBoolean("is_given_up");
        }
    }

    @SubscribeEvent
    public static void onTargetTeammateKilled(LivingDeathEvent ev) {
        Level level = ev.getEntity().level;
        if (! level.isClientSide) {
            if (! ev.isCanceled()) {
                level.getCapability(PVZZombieEventCapability.CAP).ifPresent(cap -> {
                    cap.getEvents().forEach(event -> {
                        if (event instanceof Invasion && EntityUtil.isEntityValid(ev.getSource().getEntity()) && event.getMembers().contains(ev.getSource().getEntity())) {
                            ((Invasion) event).killCount.add(((Invasion) event).totalTime);
                        }
                    });
                });
            }
        }
    }
}
