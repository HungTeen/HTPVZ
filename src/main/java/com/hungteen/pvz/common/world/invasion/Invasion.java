package com.hungteen.pvz.common.world.invasion;

import com.hungteen.pvz.PVZConfig;
import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.common.capability.entity.PVZEntityCapability;
import com.hungteen.pvz.common.entity.EntityLifter;
import com.hungteen.pvz.common.register.PVZEntities;
import com.hungteen.pvz.api.ZombieEvent;
import com.hungteen.pvz.common.world.PathSeeker;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.event.TickEvent;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Stream;

/**this class is the instance of an invasion. For the types of invasion, check {@link InvasionType}.*/
public class Invasion extends ZombieEvent implements INBTSerializable<CompoundTag> {
    public InvasionType type;
    public int invasionLevel;
    public List<Wave> waves = new ArrayList<>();
    public int currentWave;
    /**
     * the threat of enemies already added to level.
     */
    public int currentWaveThreat;
    public int currentWaveTime;
    public int lastSpawn;
    public int totalTime;
    /**
     * At start of a wave invasion generates enemies and put 'em in this map. And as enemies are killed the game calculates threat from this and remove them from the map.
     */
    private final Map<Entity, Integer/*threat*/> enemiesToSummon = new HashMap<>();
    private final Random random = new Random();
    private ServerBossEvent invasionEvent;
    private float seekPositionHardness = 0;
    private PathSeeker pathSeeker;

    public Invasion(Level level, LivingEntity target, BlockPos pos, int invasionLevel) {
        this(level, UUID.randomUUID(), target, pos, invasionLevel);
    }

    /**Used by {@link com.hungteen.pvz.common.register.PVZZombieEvents#fromTag(Level, UUID, CompoundTag) PVZZombieEvents#fromTag()} when syncing or reloading.*/
    public Invasion(Level level, UUID uuid, CompoundTag tag) {
        super(level, uuid, tag);
    }

    public Invasion(Level level, UUID uuid, LivingEntity target, BlockPos pos, int invasionLevel) {
        this(level, uuid);
        this.target = target;
        this.targetUUID = target.getUUID();
        this.position = pos;
        this.invasionLevel = invasionLevel;
        this.generateWaves();
        generateEnemies(this.getCurrentWave().threat);
    }

    public Invasion(Level level, UUID uuid) {
        super(level, uuid);
        this.currentWave = 0;
        this.currentWaveTime = 0;
        this.currentWaveThreat = 0;
        this.lastSpawn = 0;
        this.totalTime = 0;
        if (level instanceof ServerLevel) {
            invasionEvent = new ServerBossEvent(Component.translatable("event.pvz.invasion",
                    uuid /*to let client identify which invasion it is, provide one extra arguments than needed which is the uuid of the event.*/),
                    BossEvent.BossBarColor.GREEN, BossEvent.BossBarOverlay.PROGRESS);
        }
        this.invasionLevel = 1;
        if (level instanceof ServerLevel serverLevel) {
            this.pathSeeker = new PathSeeker(serverLevel);
        }
    }

    public void generateWaves() {
        this.addWave(false, 300, 100, 1000);
        this.addWave(true, 600, 100, 1000);
        this.addWave(false, 300, 100, 1000);
        this.addWave(false, 400, 300, 1000);
        this.addWave(true, 1000, 100, 1000);
        this.addWave(false, 300, 100, 1000);//5
        this.addWave(true, 600, 100, 1000);
        this.addWave(false, 300, 100, 1000);
        this.addWave(false, 400, 300, 1000);
        this.addWave(true, 1000, 100, 1000);
        this.addWave(false, 500, 300, 1000);//10
        this.addWave(true, 1000, 100, 1000);
    }

    public void addWave(boolean isBigWave, int threat, int minimumWaitTime, int maximumWaitTime) {
        this.waves.add(new Wave(isBigWave, threat, minimumWaitTime, maximumWaitTime));
    }

    public void addWave(boolean isBigWave, int threat) {
        this.waves.add(new Wave(isBigWave, threat));
    }

    @Override
    public void tick(TickEvent.ServerTickEvent ev) {
        this.currentWaveTime += 1;
        this.totalTime += 1;
        this.invasionEvent.setProgress(Mth.clamp(
                this.currentWave >= this.waves.size() - 1 ? 1 :
                        (this.currentWave + 1 + (float) this.currentWaveThreat / this.getCurrentWave().threat) / this.waves.size(),
                0.0F, 1.0F));
        if (tickCount % 20 == 0) {
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
                    ((this.members.isEmpty() || lastSpawn > 150) && this.enemiesToSummon.isEmpty() && this.currentWaveTime > this.getCurrentWave().minimumWaitTime) ||
                            (((this.members.isEmpty() || lastSpawn > 150) || this.waves.size() - 1 > this.currentWave) && this.currentWaveTime > this.getCurrentWave().maximumWaitTime))
            ) {
                this.switchWave();
            }
            if (this.currentWaveThreat == 0 && currentWaveTime > getCurrentWave().maximumWaitTime) {
                this.enemiesToSummon.clear();
                this.getCurrentWave().isGivenUp = true;
                this.switchWave();
            }
            //summoning.
            if (!this.level.isClientSide && target != null) {
                if (! this.enemiesToSummon.isEmpty()) {
                    Entity entity = this.enemiesToSummon.keySet().stream().findFirst().get();
                    boolean summoned = false;
                    Optional<Entity> member = this.members.stream().findFirst();
                    if ((this.getCurrentWave().isBigWave || random.nextInt(3) != 0) &&
                            ! this.members.isEmpty() && member.get().distanceToSqr(this.target) > 144) {
                        Vec3 vec3 = generateSpawningPositionAround(entity, member.get().position());
                        if (vec3 != null) {
                            summonEntity(entity, vec3);
                            this.currentWaveThreat += this.enemiesToSummon.get(entity);
                            this.enemiesToSummon.remove(entity);
                            summoned = true;
                        }
                    }
                    if (! summoned) {
                        Vec3 vec3 = generateSpawningPosition(entity);
                        if (vec3 != null) {
                            summonEntity(entity, vec3);
                            this.currentWaveThreat += this.enemiesToSummon.get(entity);
                            this.enemiesToSummon.remove(entity);
                        }
                    }
                }
                if (PVZConfig.PVZGameRules.getBoolean(level, PVZConfig.Common.showInvasionDetails)) {
                    PVZMod.LOGGER.info(this.currentWave + " : " + this.currentWaveThreat + " : " + this.enemiesToSummon.size() + " : "
                            + this.currentWaveTime + " : " + seekPositionHardness + " : " + pathSeeker.availablePositions.size());
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

    public Wave getCurrentWave() {
        return this.waves.get(this.currentWave);
    }

    public void switchWave() {
        if (this.currentWave < this.waves.size() - 1) {
            this.currentWave += 1;
            this.currentWaveTime = 0;
            this.currentWaveThreat = 0;
            for (int threat : this.enemiesToSummon.values()) {
                this.getCurrentWave().threat += threat / 2;
            }
            for (Entity entity : this.members) {
                if (target == null || entity.distanceToSqr(target) > 256) {
                    entity.discard();
                    this.getCurrentWave().threat += 100;
                }
            }
            this.enemiesToSummon.clear();
            generateEnemies(this.getCurrentWave().threat);
        } else {
            this.remove();
        }
    }

    public void generateEnemies(int totalThreat) {
        int threat = 0;
        while (threat <= totalThreat) {
            Pair<Entity, Integer/*threat*/> choice = selectEntity(totalThreat - threat);
            if (choice != null) {
                this.enemiesToSummon.put(choice.getFirst(), choice.getSecond());
                threat += choice.getSecond();
            } else {
                break;
            }
        }
    }

    /**
     * @return a pair of entity and the threat it cost, null for no available entity.
     */
    protected @Nullable Pair<Entity, Integer/*threat*/> selectEntity(int threatLeft) {
        if (threatLeft > 100) {
            EntityType<?> type = PVZEntities.ZOMBIE.get();
            Entity entity = type.create(this.level);
            return Pair.of(entity, 100);
        } else {
            return null;
        }
    }

    private void summonEntity(Entity entity, Vec3 pos) {
        EntityLifter lifter = PVZEntities.ENTITY_LIFTER.get().create(this.level);
        lifter.setPos(pos);
        entity.setPos(pos.add(0, - entity.getBbHeight(), 0));
        entity.startRiding(lifter);
        if (entity instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.GLOWING, 1000));
        }
        level.addFreshEntity(lifter);
        level.addFreshEntity(entity);
        entity.getCapability(PVZEntityCapability.CAP).ifPresent(cap -> cap.zombieEventUUIDs.add(this.uuid));
        this.members.add(entity);
        PlayerTeam enemyTeam = entity.getServer().getScoreboard().getPlayerTeam(PVZMod.ENEMY_TEAM);
        String name = entity.getScoreboardName();
        if (enemyTeam != null) {
            entity.getServer().getScoreboard().addPlayerToTeam(name, enemyTeam);
        }
        lastSpawn = 0;
    }

    protected @Nullable Vec3 generateSpawningPosition(Entity entity) {
        if (seekPositionHardness > 50 - 5 * pathSeeker.availablePositions.size()) {
            if (this.pathSeeker.availablePositions.isEmpty()) {
                seekPositionHardness += 8;
                return null;
            } else {
                Vec3i pos = this.pathSeeker.availablePositions.stream().findFirst().get();
                this.pathSeeker.availablePositions.remove(pos);
                seekPositionHardness += 5;
                double dist = pos.distSqr(target.blockPosition());
                if (dist > 100 && dist < 574) {
                    return Vec3.atBottomCenterOf(pos);
                } else {
                    return null;
                }
            }
        } else {
            Vec3 result;
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
    }

    protected @Nullable Vec3 generateSpawningPositionAround(Entity entity, Vec3 originSpot) {
        Vec3 result;
        if (entity instanceof PathfinderMob mob) {
            entity.setPos(originSpot);
            result = DefaultRandomPos.getPosAway(mob, 8, 3, this.target.position());
        } else {
            double angle = random.nextDouble(Math.PI * 2);
            double radius = random.nextDouble(8);
            result = this.target.position().add(Math.sin(angle) * radius, 0, Math.cos(angle) * radius);
        }
        if (result == null) {
            seekPositionHardness += 12;
        }
        return result;
    }

    public void remove() {
        super.remove();
        this.invasionEvent.setVisible(false);
        this.invasionEvent.removeAllPlayers();
    }
    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = super.serializeNBT();
        if (this.type != null)
        tag.putString("invasion_type", type.location.toString());
        tag.putInt("total_time", totalTime);
        tag.putInt("current_wave_threat", currentWaveThreat);
        tag.putInt("current_wave_time", currentWaveTime);
        tag.putInt("current_wave", currentWave);
        tag.putInt("invasion_level", invasionLevel);
        CompoundTag waveTag = new CompoundTag();
        if (waves != null) {
            for (int i = 0; i < waves.size(); i ++) {
                waveTag.put(String.valueOf(i), waves.get(i).serializeNBT());
            }
        }
        tag.put("waves", waveTag);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        super.deserializeNBT(tag);
        if (tag.contains("invasion_type"))
        this.type = InvasionType.getInvasionType(new ResourceLocation(tag.getString("invasion_type")));
        CompoundTag waveTag = tag.getCompound("waves");
        this.waves = new ArrayList<>();
        int i = 0;
        while (waveTag.contains(String.valueOf(i))) {
            CompoundTag wave = waveTag.getCompound(String.valueOf(i));
            i ++;
            addWave(wave.getBoolean("is_big_wave"), wave.getInt("threat"),
                    wave.getInt("minimum_wait_time"), wave.getInt("maximum_wait_time"));
        }
        this.currentWave = tag.getInt("current_wave");
        this.currentWaveThreat = tag.getInt("current_wave_threat");
        this.currentWaveTime = tag.getInt("current_wave_time");
        this.totalTime = tag.getInt("total_time");
        if (this.level instanceof ServerLevel) {
            generateEnemies(this.getCurrentWave().threat - this.currentWaveThreat);
        }
        this.invasionLevel = tag.getInt("invasion_level");
    }

    public static class Wave implements INBTSerializable<CompoundTag> {
        public boolean isBigWave;
        public int threat;
        public int maximumWaitTime;
        public int minimumWaitTime;
        public boolean isGivenUp;

        public Wave(boolean isBigWave, int threat) {
            this(isBigWave, threat, 600, 1200);
        }
        public Wave(boolean isBigWave, int threat, int minimumWaitTime, int maximumWaitTime) {
            this.isBigWave = isBigWave;
            this.threat = threat;
            this.maximumWaitTime = maximumWaitTime;
            this.minimumWaitTime = minimumWaitTime;
            this.isGivenUp = false;
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
}
