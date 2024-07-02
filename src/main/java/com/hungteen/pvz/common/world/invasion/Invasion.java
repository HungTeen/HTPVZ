package com.hungteen.pvz.common.world.invasion;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.common.capability.entity.PVZEntityCapability;
import com.hungteen.pvz.common.entity.EntityLifter;
import com.hungteen.pvz.common.entity.zombies.PVZZombie;
import com.hungteen.pvz.common.register.PVZEntities;
import com.hungteen.pvz.common.world.ZombieEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.event.TickEvent;

import java.util.*;

public class Invasion extends ZombieEvent implements INBTSerializable<CompoundTag> {
    public InvasionType type;
    public int invasionLevel;
    public List<Wave> waves = new ArrayList<>();
    public int currentWave;
    public int currentWaveThreat;
    public int currentWaveTime;
    private final Random random = new Random();
    private ServerBossEvent invasionEvent;

    public Invasion(Level level, LivingEntity target, BlockPos pos, int invasionLevel) {
        this(level, UUID.randomUUID(), target, pos, invasionLevel);
    }
    public Invasion(Level level, UUID uuid, LivingEntity target, BlockPos pos, int invasionLevel) {
        this(level, uuid);
        this.target = target;
        this.position = pos;
        this.invasionLevel = invasionLevel;
        this.generateWaves();
    }

    public Invasion(Level level, UUID uuid) {
        super(level, uuid);
        this.currentWave = 0;
        this.currentWaveTime = 0;
        this.currentWaveThreat = 0;
        if (level instanceof ServerLevel) {
            invasionEvent = new ServerBossEvent(Component.translatable("event.pvz.invasion",
                    uuid /*to let client identify which invasion it is, the last argument of component should be uuid.*/),
                    BossEvent.BossBarColor.GREEN, BossEvent.BossBarOverlay.PROGRESS);
        }
        this.invasionLevel = 1;
    }

    /**Used by {@link ZombieEvent#fromTag(Level, UUID, CompoundTag)}. */
    public Invasion(Level level, UUID uuid, CompoundTag tag) {
        this(level, uuid);
        this.deserializeNBT(tag);
    }

    public void generateWaves() {
        this.addWave(false, 600);
        this.addWave(true, 600);
        this.addWave(false, 400);
        this.addWave(false, 600);
        this.addWave(true, 500);
    }

    public void addWave(boolean isBigWave, int threat, int minimumWaitTime, int maximumWaitTime) {
        this.waves.add(new Wave(isBigWave, threat, minimumWaitTime, maximumWaitTime));
    }

    public void addWave(boolean isBigWave, int threat) {
        this.waves.add(new Wave(isBigWave, threat));
    }

    @Override
    public void tick(TickEvent.ServerTickEvent ev) {
        super.tick(ev);
        this.currentWaveTime += 1;
        this.invasionEvent.setProgress(Mth.clamp(
                this.currentWave >= this.waves.size() - 1 ? 1 :
                        (this.currentWave + 1 + (float) this.currentWaveThreat / this.waves.get(currentWave).threat) / this.waves.size(),
                0.0F, 1.0F));
        if (tickCount % 20 == 0) {
            //player.
            List<ServerPlayer> players = ((ServerLevel) level).getPlayers(player -> player.distanceToSqr(Vec3.atCenterOf(this.position)) < (this.range + 15) * (this.range + 15));
            Collection<ServerPlayer> players1 = this.invasionEvent.getPlayers();
            for (ServerPlayer player : players1) {
                if (! players.contains(player)) {
                    this.invasionEvent.removePlayer(player);
                } else {
                    players.remove(player);
                }
            }
            for (ServerPlayer player : players) {
                this.invasionEvent.addPlayer(player);
            }
            //switch wave.
            if (!this.level.isClientSide && (
                    (this.members.isEmpty() && this.currentWaveTime > this.waves.get(this.currentWave).minimumWaitTime) ||
                            this.currentWaveTime > this.waves.get(this.currentWave).maximumWaitTime)
            ) {
                if (this.currentWave < this.waves.size() - 1) {
                    this.currentWave += 1;
                    this.currentWaveTime = 0;
                    this.currentWaveThreat = 0;
                } else {
                    this.remove();
                }
            }
            //summoning.
            if (! this.level.isClientSide && this.members.isEmpty()) {
                PVZMod.LOGGER.info(this.currentWave + " ：" + this.currentWaveThreat + " : "
                        + this.currentWaveTime + " : " + this.waves.get(this.currentWave).isBigWave);
                this.currentWaveThreat = this.waves.get(currentWave).threat - summonWithThreat(
                        this.waves.get(currentWave).threat - this.currentWaveThreat, this.waves.get(this.currentWave).isBigWave);
            }
        }
    }

    public int summonWithThreat(int threat, boolean isBigWave) {
        int tmp = threat;
        boolean cont = true;
        EntityType<?> type = PVZEntities.ZOMBIE.get();
        while (threat >= 100 && cont) {
            Entity entity = type.create(this.level);
            summonEntity(entity);
            threat -= 100;
            cont = isBigWave || random.nextBoolean();
        }
        if (tmp != threat) {
            if (isBigWave) {
                Entity entity = type.create(this.level);
                PVZZombie.OVERWORLD_FLAG_ZOMBIE_CONSUMER.accept(entity);
                summonEntity(entity);
            }
        }
        return threat;
    }

    public void summonEntity(Entity entity) {
        entity.setPos(Vec3.atBottomCenterOf(this.position).add(0, - entity.getBbHeight(), 0));
        EntityLifter lifter = PVZEntities.ENTITY_LIFTER.get().create(this.level);
        lifter.setPos(Vec3.atBottomCenterOf(this.position));
        entity.startRiding(lifter);
        level.addFreshEntity(lifter);
        level.addFreshEntity(entity);
        entity.getCapability(PVZEntityCapability.CAP).ifPresent(cap -> cap.zombieEventUUIDs.add(this.uuid));
        this.members.add(entity);
        PlayerTeam enemyTeam = entity.getServer().getScoreboard().getPlayerTeam(PVZMod.ENEMY_TEAM);
        String name = entity.getScoreboardName();
        if (enemyTeam != null) {
            entity.getServer().getScoreboard().addPlayerToTeam(name, enemyTeam);
        }
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
        tag.putInt("current_wave_threat", currentWaveThreat);
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
        this.waves.clear();
        int i = 0;
        while (waveTag.contains(String.valueOf(i))) {
            CompoundTag wave = waveTag.getCompound(String.valueOf(i));
            i ++;
            addWave(wave.getBoolean("is_big_wave"), wave.getInt("threat"),
                    wave.getInt("minimum_wait_time"), wave.getInt("maximum_wait_time"));
        }
        this.currentWave = tag.getInt("current_wave");
        this.currentWaveThreat = tag.getInt("current_wave_threat");
        this.invasionLevel = tag.getInt("invasion_level");
    }

    public static class Wave implements INBTSerializable<CompoundTag> {
        public boolean isBigWave;
        public int threat;
        public int maximumWaitTime;
        public int minimumWaitTime;

        public Wave(boolean isBigWave, int threat) {
            this(isBigWave, threat, 600, 1200);
        }
        public Wave(boolean isBigWave, int threat, int minimumWaitTime, int maximumWaitTime) {
            this.isBigWave = isBigWave;
            this.threat = threat;
            this.maximumWaitTime = minimumWaitTime;
            this.minimumWaitTime = maximumWaitTime;
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();
            tag.putBoolean("is_big_wave", this.isBigWave);
            tag.putInt("threat", this.threat);
            tag.putInt("minimum_wait_time", this.minimumWaitTime);
            tag.putInt("maximum_wait_time", this.maximumWaitTime);
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            isBigWave = tag.getBoolean("is_big_wave");
            threat = tag.getInt("threat");
            minimumWaitTime = tag.getInt("minimum_wait_time");
            maximumWaitTime = tag.getInt("maximum_wait_time");
        }
    }
}
