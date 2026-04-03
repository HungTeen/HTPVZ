package com.hungteen.pvz.common.world.invasion;

import com.hungteen.pvz.PVZConfig;
import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.api.ZombieEvent;
import com.hungteen.pvz.common.capability.entity.PVZEntityCapability;
import com.hungteen.pvz.common.capability.player.PVZPlayerCapStats;
import com.hungteen.pvz.common.capability.player.PVZPlayerCapability;
import com.hungteen.pvz.common.entity.EntityLifter;
import com.hungteen.pvz.common.item.LootBagItem;
import com.hungteen.pvz.common.item.SeedPacketItem;
import com.hungteen.pvz.common.register.PVZCriteriaTriggers;
import com.hungteen.pvz.common.register.PVZEntities;
import com.hungteen.pvz.common.register.PVZItems;
import com.hungteen.pvz.common.register.PVZStats;
import com.hungteen.pvz.common.world.PathSeeker;
import com.hungteen.pvz.util.EntityUtil;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
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
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.TriPredicate;
import net.minecraftforge.event.TickEvent;
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
    public static final String INVASION_THREAT = "pvz.invasion_threat";
    private static final Random random = new Random();
    public boolean trackable;
    /**Invasion types. The first one (types[0]) is the main type. */
    public List<InvasionType> types;
    /**Hardness of incasion.*/
    public int invasionLevel;
    /**The waves of the invasion. Generated whenever an invasion starts.*/
    public List<Wave> waves = new ArrayList<>();
    public int currentWave;
    /**the threat of enemies already added to level.*/
    public int currentWaveThreat;
    public int currentWaveTime;
    /**The number of entities summoned in current wave.*/
    public int currentWaveSummoned = 0;
    public int lastSpawnTime = 0;
    public int lastRemoveTime = 0;
    /**The time this invasion has passed.*/
    public int totalTime;
    /**The time this invasion expect to end in.*/
    public int expectedTotalTime;
    /**The countdown after the invasion ends.*/
    public int endCountDown = -1;
    /**Progress bar of this invasion.*/
    private ServerBossEvent invasionEvent;
    private PathSeeker pathSeeker;
    private float seekPositionHardness = 0;

    //director system
    private int fleeCount = 0;
    private Vec3 storedTargetPos = null;
    private int storedTargetTeammateCount = 1;
    private int targetTeammateCount = 0;
    private int storedTargetTeammateCost = 1;
    private int targetTeammateCost = 0;
    public float threatFactor = 1;
    private float timeFactor = 1;

    public Invasion(Level level, List<InvasionType> types, LivingEntity target, BlockPos pos, int invasionLevel) {
        this(level, UUID.randomUUID(), types, target, pos, invasionLevel);
    }

    /**Used by {@link com.hungteen.pvz.common.register.PVZZombieEvents#fromTag(Level, UUID, CompoundTag) PVZZombieEvents#fromTag()} when syncing or reloading.*/
    public Invasion(Level level, UUID uuid, CompoundTag tag) {
        super(level, uuid, tag);
        this.deserializeNBT(tag);
        if (level instanceof ServerLevel serverLevel) {
            invasionEvent = new ServerBossEvent(Component.translatable("zombie_event.pvz.invasion",
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
        if (target instanceof ServerPlayer player) {
            player.awardStat(PVZStats.INVASIONS);
            PVZCriteriaTriggers.INVASION.trigger(player);
        }
        this.position = pos;
        this.invasionLevel = invasionLevel;
        this.expectedTotalTime = this.generateWaves();
    }

    public Invasion(Level level, UUID uuid, List<InvasionType> types) {
        super(level, uuid);
        this.trackable = true;
        this.types = types;
        this.currentWave = 0;
        this.currentWaveTime = 0;
        this.currentWaveThreat = 0;
        this.totalTime = 0;
        this.invasionLevel = 5;
        if (level instanceof ServerLevel serverLevel) {
            invasionEvent = new ServerBossEvent(Component.translatable("zombie_event.pvz.invasion",
                    uuid.toString()),
                    BossEvent.BossBarColor.GREEN, BossEvent.BossBarOverlay.PROGRESS);
            this.pathSeeker = new PathSeeker(serverLevel);
        }
    }

    /**Generate waves. Attention that the waves generated here may not be the ones actually starts in the game. With the invasion going these values are dynamically adjusted.*/
    public int generateWaves() {
        float lengthFactor = 1;
        for (InvasionType type : this.types) {
            lengthFactor *= type.length();
        }
        final int totalLength = (int) (6000 * lengthFactor * ((this.invasionLevel) * 0.1 + 0.5) + 100);

        final int bigWaveNum = Math.max(1, random.nextInt((int) ((float) totalLength / 6000 + Math.max(0, (this.invasionLevel) / 3) + 1)));
        int length = 0;
        float k = (float) PVZConfig.PVZGameRules.getInt(level, PVZConfig.Common.invasionDifficultyFactorK) / 1000;
        int b = PVZConfig.PVZGameRules.getInt(level, PVZConfig.Common.invasionDifficultyFactorB);
        while (length < totalLength) {
            int threat = (int) (Math.pow((b + length * k), 1.05) * (0.8 + random.nextFloat() * k) * ((double) this.invasionLevel * 0.05 + 0.75));
            int waveLength = 200 * threat / (b + length / (8 - 2 * length / totalLength));
            Difficulty difficulty = ((ServerLevel) level).getServer().getWorldData().getDifficulty();
            switch (difficulty) {
                case PEACEFUL -> threat = 0;
                case EASY -> threat *= 0.75;
                case HARD -> threat *= 1.25;
                default -> {}//normal difficulty or other possible situations.
            }
            boolean bigWave = (int) (((float) length / totalLength) * bigWaveNum) != (int) ((float) (length + waveLength) / totalLength * bigWaveNum);
            if (bigWave) {
                if (! this.waves.isEmpty()) {
                    Wave lastWave = this.waves.get(this.waves.size() - 1);
                    lastWave.threat *= 0.8;
                    lastWave.minimumWaitTime += 300;
                    if (lastWave.maximumWaitTime < lastWave.minimumWaitTime) lastWave.maximumWaitTime = lastWave.minimumWaitTime;
                }
                threat *= 1.8;
                waveLength *= 1.5;
            }
            length += waveLength;
            threat = Math.max(100, threat);
            this.addWave(bigWave, threat, (int) Math.max(400, waveLength * 0.6), (int) Math.max(1000, waveLength * 1.2));
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
                        Math.min(this.currentWave + 2, this.currentWave + 1 + (float) this.currentWaveThreat / this.getCurrentWave().threat) / this.waves.size(),
                0.0F, 1.0F));
        if (this.endCountDown > 0) {
            this.endCountDown--;
            if (this.endCountDown <= 0) {
                this.remove();
            }
            super.tick(ev);
            return;
        }
        if (tickCount % 5 == 0) {
            //ending invasion.
            if (this.waves.isEmpty()) {
                PVZMod.LOGGER.warn("Found an invasion with no waves! Removing it.");
                this.remove();
                return;
            }
            if (! this.isEnded()) {
                if (target != null && target.isDeadOrDying()) {
                    this.end(Vec3.atCenterOf(this.position), "zombie_event.pvz.invasion.end.fail", false);
                    return;
                }
                if (this.level.getDifficulty() == Difficulty.PEACEFUL) {
                    this.end(Vec3.atCenterOf(this.position), "zombie_event.pvz.invasion.end.peace", false);
                    return;
                }
                if (target != null
                        && (getPlayerFleeWill() - getPlayerActivation()) > (getPlayerAlive() < 10 ? 2500 : 5000)) {
                    if (++ fleeCount > 20) {
                        this.end(Vec3.atCenterOf(this.position), "zombie_event.pvz.invasion.end.escape", false);
                        return;
                    }
                } else {
                    fleeCount = 0;
                }
            }
            //handle progress bar.
            List<ServerPlayer> players = ((ServerLevel) level).getPlayers(player -> player.distanceToSqr(Vec3.atCenterOf(this.position)) < (this.range + 15) * (this.range + 15));
            Collection<ServerPlayer> players1 = Set.copyOf(this.invasionEvent.getPlayers());
            for (ServerPlayer player : players1) {
                if (! players.contains(player)) {
                    this.invasionEvent.removePlayer(player);
                }
            }
            for (ServerPlayer player : players) {
                if (! players1.contains(player)) {
                    this.invasionEvent.addPlayer(player);
                }
            }
            //switch wave.
            if  (
                    ((this.members.isEmpty() || totalTime - lastRemoveTime > 500)
                        && this.currentWaveThreat >= this.getCurrentWave().threat/*enemies spawned up*/
                        && (this.currentWaveTime > this.getCurrentWave().minimumWaitTime || (this.members.isEmpty() && totalTime - lastRemoveTime > 500)) /*time up*/)
                    || ((this.members.size() < this.currentWaveSummoned / 3 || totalTime - lastRemoveTime > 500
                            || (totalTime - lastSpawnTime > 500 && this.currentWaveThreat < this.getCurrentWave().threat) /*enemies can't spawn*/)
                        && this.waves.size() - 1 > this.currentWave /*current wave is not final wave*/
                        && this.currentWaveTime > this.getCurrentWave().maximumWaitTime /*time up*/)
            ) {
                this.switchWave();
            }
            if (this.currentWaveThreat == 0 && currentWaveTime > getCurrentWave().maximumWaitTime) {
                this.currentWaveSummoned = 0; //in order not to pass threat to next wave.
                this.getCurrentWave().isGivenUp = true;
                this.switchWave();
            }
            if (target != null) {
                //tracking target
                if (trackable && this.target.blockPosition().distSqr(this.position) > 64 && tickCount % 40 == 0) {
                    this.position = this.position.offset(new Vec3i(
                            Math.min(Math.max(-1, target.getX() - this.position.getX()), 1),
                            Math.min(Math.max(-1, target.getY() - this.position.getY()), 1),
                            Math.min(Math.max(-1, target.getZ() - this.position.getZ()), 1)
                    ));
                }
                //summoning.
                if (currentWaveTime >= 60) {
                    int threatLeft = this.getCurrentWave().threat - this.currentWaveThreat;
                    if (threatLeft > 0 && (this.getLivingMembersThreat() <= 500 + this.getCurrentWave().threat / 2 || this.members.size() < 5 + this.invasionLevel / 2)) {
                        Pair<CompoundTag/*enemy*/, Integer> pair = this.selectEntity();
                        if (pair != null) {
                            if (summonEntity(pair)) {
                                this.currentWaveThreat += pair.getSecond();
                                this.currentWaveSummoned += 1;
                            }
                        }
                    }
                }
                //director system.
                if (tickCount % 40 == 0 && directorEnabled()) {
                    List<Entity> teammates = level.getEntities(target,
                            AABB.ofSize(Vec3.atCenterOf(this.position), this.range * 2, this.range, this.range * 2),
                            entity -> ! (entity instanceof Projectile) && EntityUtil.isTeammate(target, entity));
                    targetTeammateCount = teammates.size();
                    AtomicInteger cost = new AtomicInteger(0);
                    teammates.forEach(e -> e.getCapability(PVZEntityCapability.CAP).ifPresent(cap -> cost.addAndGet(cap.cost)));
                    targetTeammateCost = cost.get();
                    if (PVZConfig.PVZGameRules.getBoolean(level, PVZConfig.Common.showInvasionDetails)) {
                        if (target instanceof ServerPlayer player && EntityUtil.isEntityValid(player)) {
                            player.sendSystemMessage(Component.literal(getInfo()));
                        }
                    }
                    this.storedTargetPos = this.target.position();
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
            this.seekPositionHardness -= (float) (0.25 + 0.001 * seekPositionHardness);
        }
        super.tick(ev);
    }

    //director system
    public boolean directorEnabled() {
        return EntityUtil.isEntityValid(this.target) && this.types.stream().noneMatch(InvasionType::disableDirector);
    }

    /**Returns a relative value of the ability player and plants damage zombies.*/
    public int getPlayerAttack() {
        Wave wave = this.getCurrentWave();
        return (int) (((double) (80 * Math.max(0, (currentWaveThreat - getLivingMembersThreat()))) / (1 + wave.threat)) / (currentWaveTime / Math.min(1 + (double) wave.maximumWaitTime / 2, 1 + wave.minimumWaitTime * 1.6)))
                + 20 * totalTime * this.waves.size() / (1 + expectedTotalTime) / (1 + currentWave);
    }
    /**Returns a relative value of the ability player grow its army.*/
    public int getPlayerAlive() {
        return currentWave * targetTeammateCount * 3 / waves.size() / 10
                + (int) ((double) (30 * targetTeammateCount) / (4 + storedTargetTeammateCount) / (getCurrentWave().isBigWave ? 1.2 : 0.8))
                + (int) ((double) (50 * targetTeammateCost) / (200 + storedTargetTeammateCost) / (getCurrentWave().isBigWave ? 1.2 : 0.8));
    }
    /**Return a relative value of the will player flee from the invasion.*/
    public int getPlayerFleeWill() {
        AtomicInteger rayResult = new AtomicInteger(0);
        if (target instanceof Player player) {
            for (int x = -1; x < 2; x ++) {
                for (int z = -1; z < 2; z ++) {
                    Vec3 destination = Vec3.atCenterOf(player.blockPosition()).add((double)x * 2, x == 0 && z == 0 ? 4 : random.nextInt(4), (double)z * 2);
                    if (level.clip(new ClipContext(Vec3.atCenterOf(player.blockPosition()), destination, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, null))
                            .getType() == HitResult.Type.BLOCK) {
                        rayResult.addAndGet(1);
                    }
                }
            }
        }
        return 1000 * waves.size() / (1 + currentWave) / (1 + targetTeammateCount)
                + (int) Math.pow(this.position.distSqr(target.blockPosition()), 0.75) * 5
                + (int) Math.pow(2.4, rayResult.get());
    }
    /**Return a relative value of the will player join the invasion.*/
    public int getPlayerActivation() {
        AtomicInteger cdSum = new AtomicInteger();
        AtomicInteger cdPer = new AtomicInteger();
        AtomicInteger cdNum = new AtomicInteger();
        AtomicInteger all = new AtomicInteger();
        if (target instanceof Player player) {
            player.getInventory().items.forEach(item -> {
                if (item.getItem() instanceof SeedPacketItem<?>) all.addAndGet(1);
            });
            player.getCooldowns().cooldowns.values().forEach(value -> {
                cdPer.addAndGet(50 * (value.endTime - player.getCooldowns().tickCount) / (value.endTime - value.startTime));
                cdSum.addAndGet(50 * (value.endTime - player.getCooldowns().tickCount));
                cdNum.addAndGet(1);
            });
        }
        if (all.get() == 0) {
            all.addAndGet(1);
        }
        cdNum.set(cdNum.get() == 0 ? 1 :cdNum.get());
        return (int) (target == null || storedTargetPos == null ? 20 : target.position().distanceToSqr(storedTargetPos) / 4)
                + 16 * cdPer.get() / all.get()
                + cdSum.get() / 5 / all.get()
                + cdNum.get() * 5;
    }

    public String getInfo() {
        Wave wave = getCurrentWave();
        int fle = 0;
        int far = 0;
        int act = 0;
        if (directorEnabled()) {
            fle = getPlayerFleeWill();
            far = (int) Math.pow(this.position.distSqr(target.blockPosition()), 0.75) * 5;
            act = getPlayerActivation();
        }
        String result = "\ninvasion " + this.uuid.toString() +
                "\n LV " + invasionLevel + " WAVE " + (this.currentWave + 1) + "/" + this.waves.size() + " POS_SEKR " + pathSeeker.availablePositions.size() +
                "\n TIME wave " + this.currentWaveTime / 20 + " exp. " + this.getCurrentWave().minimumWaitTime / 20 + "~" + this.getCurrentWave().maximumWaitTime / 20 + " total " + this.totalTime / 20 + " exp. " + expectedTotalTime / 20 +
                "\n THREAT curr " + this.currentWaveThreat + "(" + this.currentWaveSummoned+ ") alive " + this.getLivingMembersThreat() + "("+ this.members.size() + ") total "+ this.getCurrentWave().threat;
        return (result + (
                directorEnabled() ? "\n DIR_SYS: " + "TME " + (int) (timeFactor * 100) + " TRT " + (int) (threatFactor * 100) +
                "\n  ATK " + getPlayerAttack() + " - "
                        + (int) (((double) (100 * Math.max(0, (currentWaveThreat - getLivingMembersThreat()))) / (1 + wave.threat)) / (currentWaveTime / Math.min(1 + (double) wave.maximumWaitTime / 2, 1 + wave.minimumWaitTime * 1.6))) + " "
                        + 100 * totalTime * this.waves.size() / (1 + expectedTotalTime) / (1 + currentWave) +
                "\n  ALV " + getPlayerAlive() + " - "
                        + (int) (1.5 * currentWave * targetTeammateCount / waves.size()) + " "
                        + (int) ((double) (100 * targetTeammateCount) / (4 + storedTargetTeammateCount) / (getCurrentWave().isBigWave ? 1.2 : 0.8)) + " "
                        + (int) ((double) (100 * targetTeammateCost) / (200 + storedTargetTeammateCost) / (getCurrentWave().isBigWave ? 1.2 : 0.8)) +
                "\n  FLE " + fle + "("+ fleeCount + ") - " + (1000 * waves.size() / (1 + currentWave) / (1 + targetTeammateCount)) + " "
                        + far + " "
                        + (fle - (1000 * waves.size() / (1 + currentWave) / (1 + targetTeammateCount)) - far) + " " +
                "\n  ACT " + getPlayerActivation() + " - " + currentWave * targetTeammateCount * 3 / waves.size() / 10 + " "
                        + (act - currentWave * targetTeammateCount * 3 / waves.size() / 10)
                : ""
        ));
    }

    //tools
    public Wave getCurrentWave() {
        return this.waves.get(this.currentWave);
    }

    @Override
    public void removeMember(Entity member) {
        super.removeMember(member);
        this.lastRemoveTime = totalTime;
        if (this.members.isEmpty() && this.currentWave >= this.waves.size() - 1 && this.currentWaveThreat > this.getCurrentWave().threat) {
            end(member.position().add(0, member.getEyeHeight(), 0), "zombie_event.pvz.invasion.end.win", true);
        }
    }

    public int getMemberThreat(Entity member) {
        if (! this.members.contains(member)) {
            return -1;
        } else {
            final int[] result = new int[1];
            member.getCapability(PVZEntityCapability.CAP).ifPresent(cap -> {
                if (cap.resource.equals(INVASION_THREAT)) {
                    result[0] = cap.cost;
                }
            });
            return result[0];
        }
    }
    public InvasionType getMainType() {
        return this.types.get(0);
    }

    public int getLivingMembersThreat() {
        int result = 0;
        for (Entity member : this.members) {
            result += getMemberThreat(member);
        }
        return result;
    }

    public boolean isEnded() {
        return this.endCountDown >= 0;
    }

    public void end(Vec3 position, String endType, boolean success) {
        if (target instanceof Player player) {
            if (success) {
                player.awardStat(PVZStats.INVASIONS_WON);
                InvasionType invasionType = this.getMainType();
                if (invasionType != null && invasionType.loot().isPresent()) {
                    ItemStack stack = LootBagItem.modify(PVZItems.LOOT_BAG.get().getDefaultInstance(), invasionType.loot().get(), this.invasionLevel + 3);
                    ItemEntity bag = new ItemEntity(this.level, position.x, position.y, position.z, stack);
                    bag.setPickUpDelay(40);
                    bag.setInvisible(true);
                    bag.setGlowingTag(true);
                    bag.moveTo(Vec3.atCenterOf(new BlockPos(position)));
                    level.addFreshEntity(bag);
                    bag.setDeltaMovement(
                            player.getRandom().nextFloat() / 3,
                            player.getRandom().nextFloat() / 3,
                            player.getRandom().nextFloat() / 3);
                }
            }
            PVZPlayerCapability.getPlayerData(player)
                    .ifPresent(cap -> {
                        cap.setValue(PVZPlayerCapStats.INVASION_DIFFICULTY, (success ?
                                (int)(((float) this.totalTime / this.expectedTotalTime + threatFactor * 2 + 7) / 10) * cap.getValue(PVZPlayerCapStats.INVASION_DIFFICULTY) :
                                (cap.getValue(PVZPlayerCapStats.INVASION_DIFFICULTY) - 10)));
                        cap.setValue(PVZPlayerCapStats.LAST_INVASION, 0);
                    });
        }
        if (PVZConfig.PVZGameRules.getBoolean(level, PVZConfig.Common.showInvasionDetails)) {
            PVZMod.LOGGER.info("Invasion ended with end component " + this.invasionEvent.getName() + ".");
        }
        this.invasionEvent.setName(Component.translatable(endType, uuid.toString()));
        this.endCountDown = 200;
    }

    public void switchWave() {
        if (this.currentWave < this.waves.size() - 1) {
            for (Entity entity : this.members) {
                if (! (entity instanceof LivingEntity living) ||
                        living.getLastHurtByMobTimestamp() < living.tickCount - 500 || living.getLastHurtByMobTimestamp() > Math.max(500, living.tickCount)) {
                    this.getCurrentWave().threat -= 20;
                    if (target == null || (entity.distanceToSqr(target) >
                            ((level.clip(new ClipContext(Vec3.atCenterOf(position), target.position(), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, null)).getType()
                                    == HitResult.Type.MISS) ? 576 : 64))) {
                        entity.discard();
                        this.getCurrentWave().threat += 100;
                    }
                }
            }
            //director.
            if (directorEnabled()) {
                float factor = Math.min(1, (float) currentWave / this.waves.size() * 2) / 2;
                float attack = (float) getPlayerAttack() / 100;
                float alive = (float) getPlayerAlive() / 100;
                Wave nextWave = this.waves.get(this.currentWave + 1);
                int minimumWaitTime = nextWave.minimumWaitTime;
                int threat = nextWave.threat;
                nextWave.minimumWaitTime += (int) (factor * ((float) nextWave.minimumWaitTime / attack * timeFactor - nextWave.minimumWaitTime));
                nextWave.maximumWaitTime += (int) (factor * ((float) nextWave.maximumWaitTime / attack * timeFactor - nextWave.maximumWaitTime));
                nextWave.threat += (int) (factor * (nextWave.threat * alive * threatFactor - nextWave.threat));
                threatFactor = (float) nextWave.threat / threat;
                timeFactor = (float) nextWave.minimumWaitTime / minimumWaitTime;
                if (getPlayerFleeWill() > 1000 && factor > 0.2) {
                    nextWave.threat = nextWave.threat * 100 / getPlayerFleeWill();
                }
                this.storedTargetTeammateCount = targetTeammateCount;
                this.storedTargetTeammateCost = targetTeammateCost;
            } else {
                threatFactor = 1;
                timeFactor = 1;
            }
            this.currentWave += 1;
            this.currentWaveTime = 0;
            this.currentWaveThreat = 0;
            this.currentWaveSummoned = 0;
            if (this.target instanceof Player player) {
                if (this.getCurrentWave().isBigWave) {
                    player.displayClientMessage(Component.translatable("hint.pvz.invasion.big_wave").withStyle(Style.EMPTY.withColor(0xFF2222)), true);
                } else if (PVZConfig.PVZGameRules.getBoolean(level, PVZConfig.Common.showInvasionDetails)) {
                    player.displayClientMessage(Component.translatable("hint.pvz.invasion.big_wave"), true);
                }
                player.awardStat(PVZStats.INVASION_WAVES);
            }
        } else if (this.members.isEmpty()) {
            end(Vec3.atCenterOf(this.position).add(0, 1, 0), "zombie_event.pvz.invasion.end.win", true);
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

    /**
     * @return a pair of entity and the threat it cost, null for no available entity.
     */
    protected @Nullable Pair<CompoundTag/*enemy*/, Integer/*threat*/> selectEntity() {
        List<InvasionType.EnemyType> enemyTypes = new ArrayList<>();
        AtomicInteger allWeight = new AtomicInteger();
        float threatFactor = 1;
        for (InvasionType type : this.types) {
            threatFactor *= type.threatFactor();
        }
        if (this.getCurrentWave().isBigWave && this.currentWaveSummoned == 0) {
            for (InvasionType type : this.types) {
                if (this.getCurrentWave().isBigWave && this.currentWaveSummoned == 0) {
                    type.flagEnemy().ifPresent(enemyType -> {
                        enemyTypes.add(enemyType);
                        allWeight.addAndGet(enemyType.weight());
                    });
                }
            }
        }
        if (enemyTypes.isEmpty()) { // when not a big wave or no available flag enemy.
            for (InvasionType type : this.types) {
                type.enemies().forEach(enemyType -> {
                    if ((! enemyType.isElite() || this.getCurrentWave().isBigWave)
                            && (float) this.currentWave / this.waves.size() >= enemyType.startFrom()) {
                        enemyTypes.add(enemyType);
                        allWeight.addAndGet(enemyType.weight());
                    }
                });
            }
        }
        if (enemyTypes.isEmpty()) { //when no available enemy.
            return null;
        }
        AtomicInteger selected = new AtomicInteger(random.nextInt(allWeight.get()));
        for (InvasionType.EnemyType enemyType : enemyTypes) {
            selected.set(selected.get() - enemyType.weight());
            if (selected.get() <= 0) {
                CompoundTag tag = enemyType.entityData().copy();
                tag.putInt("pvz_avoid_same_hash_random", random.nextInt());//to avoid same hash.
                return Pair.of(tag, (int) (enemyType.threat() * threatFactor));
            }
        }
        return null;
    }

    public boolean summonEntity(Pair<CompoundTag, Integer> entityData) {
        Entity entity = EntityType.loadEntityRecursive(entityData.getFirst().copy(), level, Function.identity());
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
        //handle modifiers.
        entity.setPos(pos);
        if (! this.types.stream().allMatch(type -> {
            for (TriPredicate<Invasion, Entity, Integer> predicate : type.getModifiers()) {
                if (! predicate.test(this, entity, entityData.getSecond())) return false;
            }
            return true;
        })) {
            return false;
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
        ((ServerLevel) level).addFreshEntityWithPassengers(lifter);
        entity.getCapability(PVZEntityCapability.CAP).ifPresent(cap -> {
            cap.zombieEventUUIDs.add(this.uuid);
            cap.cost = entityData.getSecond();
            cap.resource = INVASION_THREAT;
        });
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
                double dist = pos.distSqr(target.blockPosition());
                if (dist > 100 && dist < 574 && ! level.getBlockState(new BlockPos(pos)).is(BlockTags.LEAVES)) {
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
        } else if (entity instanceof LivingEntity living) {
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
    public CompoundTag addAdditionalSaveData(CompoundTag tag) {
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
        tag.putBoolean("trackable", trackable);
        tag.putInt("total_time", totalTime);
        tag.putInt("end_time", endCountDown);
        tag.putInt("expected_total_time", expectedTotalTime);
        tag.putInt("current_wave_threat", currentWaveThreat);
        tag.putInt("current_wave_time", currentWaveTime);
        tag.putInt("current_wave", currentWave);
        tag.putInt("invasion_level", invasionLevel);
        tag.putFloat("director_time_factor", timeFactor);
        tag.putFloat("director_threat_factor", threatFactor);
        if (! this.waves.isEmpty()) {
            ListTag waves = new ListTag();
            waves.addAll(this.waves.stream().map(Wave::serializeNBT).toList());
            tag.put("waves", waves);
        }
        return tag;
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        if (! this.level.isClientSide) {
            if (tag.contains("invasion_types")) {
                this.types.clear();
                ListTag types = ((ListTag) tag.get("invasion_types"));
                types.forEach(typeName ->  {
                    InvasionType type = InvasionType.getInvasionType(new ResourceLocation(typeName.getAsString()));
                    if (type == null) {
                        PVZMod.LOGGER.warn("Trying to load an invasion type with name" + new ResourceLocation(typeName.getAsString()) + " which is unavailable.");
                    } else {
                        this.types.add(type);
                    }
                });
            }
            if (this.types.isEmpty() || this.getMainType().isAddition()) {
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
        if (tag.contains("trackable")) {
            this.trackable = tag.getBoolean("trackable");
        }
        if (tag.contains("current_wave")) {
            this.currentWave = tag.getInt("current_wave");
        }
        if (tag.contains("current_wave_threat")) {
            this.currentWaveThreat = tag.getInt("current_wave_threat");
        }
        if (tag.contains("current_wave_time")) {
            this.currentWaveTime = tag.getInt("current_wave_time");
        }
        if (tag.contains("total_time")) {
            this.totalTime = tag.getInt("total_time");
        }
        if (tag.contains("end_time")) {
            this.endCountDown = tag.getInt("end_time");
        }
        if (tag.contains("expected_total_time")) {
            this.expectedTotalTime = tag.getInt("expected_total_time");
        }
        if (tag.contains("invasion_level")) {
            this.invasionLevel = tag.getInt("invasion_level");
        }
        if (tag.contains("director_time_factor")) {
            this.timeFactor = tag.getFloat("director_time_factor");
        }
        if (tag.contains("director_threat_factor")) {
            this.threatFactor = tag.getFloat("director_threat_factor");
        }
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
            if (tag.contains("is_big_wave")) {
                isBigWave = tag.getBoolean("is_big_wave");
            }
            if (tag.contains("threat")) {
                threat = tag.getInt("threat");
            }
            if (tag.contains("minimum_wait_time")) {
                minimumWaitTime = tag.getInt("minimum_wait_time");
            }
            if (tag.contains("maximum_wait_time")) {
                maximumWaitTime = tag.getInt("maximum_wait_time");
            }
            if (tag.contains("is_given_up")) {
                isGivenUp = tag.getBoolean("is_given_up");
            }
        }
    }
}
