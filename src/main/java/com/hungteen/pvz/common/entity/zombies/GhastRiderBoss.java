package com.hungteen.pvz.common.entity.zombies;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.api.interfaces.ICanGroupUp;
import com.hungteen.pvz.api.interfaces.IPlant;
import com.hungteen.pvz.common.entity.LavaGhastling;
import com.hungteen.pvz.common.entity.ai.goal.GhastRiderActivitiesGoal;
import com.hungteen.pvz.common.register.PVZAttributes;
import com.hungteen.pvz.util.EntityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.BlockPathTypes;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class GhastRiderBoss extends FireImp {
    public BlockPos homePos = null;
    protected GhastRiderActivitiesGoal bossGoal;
    public Set<LavaGhastling> ghastlings = new HashSet<>(); // effective only on server.
    public int cantFreeze = 0;
    private final ServerBossEvent bossEvent = (ServerBossEvent)(new ServerBossEvent(this.getDisplayName(), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS)).setDarkenScreen(false);
    private static final EntityDataAccessor<Boolean> PHASE_2 = SynchedEntityData.defineId(GhastRiderBoss.class, EntityDataSerializers.BOOLEAN);

    public GhastRiderBoss(EntityType<? extends Zombie> p_34271_, Level p_34272_) {
        super(p_34271_, p_34272_);
        this.setPathfindingMalus(BlockPathTypes.DANGER_FIRE, 0.0F);
        this.setPathfindingMalus(BlockPathTypes.DAMAGE_FIRE, 0.0F);
        this.setPathfindingMalus(BlockPathTypes.LAVA, 0.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PVZZombie.createAttributes()
                .add(Attributes.MAX_HEALTH, 200D)
                .add(Attributes.ARMOR, 20D)
                .add(Attributes.ARMOR_TOUGHNESS, 20D)
                .add(PVZAttributes.PLANT_HURT_RESISTANCE.get(), 0.5D);
    }
    @Override
    protected void addBehaviourGoals() {
        super.addBehaviourGoals();
        this.goalSelector.removeGoal(fireImpSummonGoal);
        this.goalSelector.removeGoal(randomStrollGoal);
        this.goalSelector.removeGoal(attackGoal);
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 32.0F, 1));
        this.bossGoal = new GhastRiderActivitiesGoal(this);
        this.goalSelector.addGoal(1, bossGoal);
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Player.class,
                false, (entity) -> EntityUtil.checkCanEntityBeAttack(this, entity)));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Mob.class,
                true, (entity) -> entity instanceof IPlant && EntityUtil.checkCanEntityBeAttack(this, entity)));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(PHASE_2, false);
    }

    public void setPhase2(boolean on) {
        this.entityData.set(PHASE_2, on);
    }

    public boolean isPhase2() {
        return entityData.get(PHASE_2);
    }

    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Phase2", isPhase2());
        if (this.homePos != null) {
            CompoundTag posTag = new CompoundTag();
            posTag.putInt("x", this.homePos.getX());
            posTag.putInt("y", this.homePos.getY());
            posTag.putInt("z", this.homePos.getZ());
            tag.put("HomePos", posTag);
        }
        CompoundTag AITag = new CompoundTag();
        this.bossGoal.save(AITag);
        tag.put("AIMemories", AITag);
        ListTag listTag = new ListTag();
        for (LavaGhastling lavaGhastling : this.ghastlings) {
            listTag.add(NbtUtils.createUUID(lavaGhastling.getUUID()));
        }
        tag.put("Ghastlings", listTag);
    }

    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Phase2")) {
            setPhase2(tag.getBoolean("Phase2"));
        }
        if (tag.contains("HomePos")) {
            CompoundTag posTag = tag.getCompound("HomePos");
            try {
                this.homePos = new BlockPos(posTag.getInt("x"), posTag.getInt("y"), posTag.getInt("z"));
            } catch (Exception ignored) {
                this.homePos = this.blockPosition();
            }
        }
        if (tag.contains("AIMemories")) {
            this.bossGoal.read(tag.getCompound("AIMemories"));
        }
        if (this.hasCustomName()) {
            this.bossEvent.setName(this.getDisplayName());
        }
        if (tag.contains("Ghastlings") && this.getLevel() instanceof ServerLevel level) {
            ListTag listTag = tag.getList("Ghastlings", CompoundTag.TAG_INT_ARRAY);
            listTag.forEach(tag1 -> {
                try {
                    UUID uuid = NbtUtils.loadUUID(tag1);
                    level.getEntity(uuid);
                } catch (Exception ignored) {
                    PVZMod.LOGGER.warn("Ghast rider saved wrong ghastling uuid!");
                }
            });
        }
    }
    @Override
    public void setCustomName(@Nullable Component p_31476_) {
        super.setCustomName(p_31476_);
        this.bossEvent.setName(this.getDisplayName());
    }
    @Override
    public boolean hurt(DamageSource damageSource, float amount) {
        PVZMod.LOGGER.info("ghast rider hurt: " + amount + " " + damageSource.getMsgId());
        if (this.getVehicle() != null && ! damageSource.isBypassArmor()) {
            return this.getVehicle().hurt(damageSource, amount);
        } else if (this.isEffectiveAi() && this.homePos != null && damageSource == DamageSource.FALL) {
            if (amount <= 2F) {
                return super.hurt(damageSource, amount);
            } else if (hurt(damageSource, 2F)) {
                this.teleportTo(this.homePos.getX() + 0.5F, this.homePos.getY() + 1, this.homePos.getZ() + 0.5F);
                this.setDeltaMovement(0, 0.5, 0);
                return true;
            } else {
                return super.hurt(damageSource, amount);
            }
        }
        return super.hurt(damageSource, amount);
    }

    @Override
    public void die(DamageSource damageSource) {
        this.bossEvent.removeAllPlayers();
        super.die(damageSource);
        ghastlings.forEach(g -> g.hurt(DamageSource.MAGIC, 60));
    }

    @Override
    public void startFollowing(ICanGroupUp target) {
    }

    @Override
    public boolean canFreeze() {
        return super.canFreeze() && cantFreeze <= 0;
    }

    public void tick() {
        int storedDeathTime = this.deathTime;
        int boardingCoolDown = this.boardingCooldown;
        if (! level.isClientSide) {
            if (cantFreeze > 0) cantFreeze --;
            if (this.isAlive()) {
                List<Player> newPlayers = level.getNearbyPlayers(TargetingConditions.forNonCombat().ignoreLineOfSight().ignoreInvisibilityTesting()
                        , this, this.getBoundingBox().inflate(48));
                List<ServerPlayer> currentPlayers = List.copyOf(this.bossEvent.getPlayers());
                for (ServerPlayer player : currentPlayers) {
                    if (! newPlayers.contains(player)) this.bossEvent.removePlayer(player);
                }
                for (Player player : newPlayers) {
                    if (player instanceof ServerPlayer serverPlayer && ! currentPlayers.contains(serverPlayer)) this.bossEvent.addPlayer(serverPlayer);
                }
                if (this.homePos == null) {
                    this.homePos = this.blockPosition();
                }
            } else {
                this.stopRiding();
                this.noPhysics = true;
                this.yRot += 5;
                this.setDeltaMovement(0, 0.05, 0);
            }
            if (this.getVehicle() instanceof LavaGhastling lavaGhastling) {
                if (this.ghastlings.size() > 1) {
                    this.bossEvent.setColor(BossEvent.BossBarColor.WHITE);
                    this.bossEvent.setProgress(((float) ghastlings.size() - 1) / 6);
                    this.bossEvent.setOverlay(BossEvent.BossBarOverlay.NOTCHED_6);
                } else {
                    this.bossEvent.setColor(isPhase2() ? BossEvent.BossBarColor.BLUE : BossEvent.BossBarColor.YELLOW);
                    this.bossEvent.setProgress(lavaGhastling.getHealth() / lavaGhastling.getMaxHealth());
                    this.bossEvent.setOverlay(BossEvent.BossBarOverlay.PROGRESS);
                }
            } else {
                this.bossEvent.setColor(BossEvent.BossBarColor.PURPLE);
                this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
                this.bossEvent.setOverlay(BossEvent.BossBarOverlay.PROGRESS);
            }
        } else {
            float healthRate = getHealth() / getMaxHealth();
            if (this.isAlive()) {
                if (healthRate < 0.5 || random.nextBoolean()) {
                    level.addParticle(healthRate < 0.5 ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.FLAME,
                            getX(), getY() + 1.05, getZ(),
                            random.nextFloat() * 0.1 - 0.05,
                            random.nextFloat() * 0.15,
                            random.nextFloat() * 0.1 - 0.05);
                }
            } else {
                for (int i = 0; i < 5; i ++) {
                    level.addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                            getX(), getY() + 0.5, getZ(),
                            random.nextFloat() * 0.2 - 0.1,
                            random.nextFloat() * 0.2 - 0.1,
                            random.nextFloat() * 0.2 - 0.1);
                }
                if (this.deathTime < 2 || (random.nextBoolean() && random.nextBoolean() && random.nextBoolean())) {
                    level.addParticle(ParticleTypes.EXPLOSION,
                            getX() + random.nextFloat() * 0.2 - 0.1, getY() + 0.5, getZ() + random.nextFloat() * 0.2 - 0.1, 0, 0, 0);
                }
            }
        }
        super.tick();
        if (this.tickCount % 20 >= 5 && deathTime >= 2) {
            this.deathTime = storedDeathTime;
        }
        if (this.getTicksFrozen() >= 350) {
            this.boardingCooldown = boardingCoolDown;
        }
    }
    @Override
    public boolean removeWhenFarAway(double p_27598_) {
        return false;
    }
}
