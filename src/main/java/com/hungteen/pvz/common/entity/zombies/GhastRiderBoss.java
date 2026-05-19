package com.hungteen.pvz.common.entity.zombies;

import com.hungteen.pvz.api.interfaces.IPlant;
import com.hungteen.pvz.common.entity.LavaGhastling;
import com.hungteen.pvz.common.entity.ai.goal.GhastRiderActivitiesGoal;
import com.hungteen.pvz.common.register.PVZAttributes;
import com.hungteen.pvz.util.EntityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
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
import java.util.List;

public class GhastRiderBoss extends FireImp {
    public BlockPos homePos = null;
    protected GhastRiderActivitiesGoal bossGoal;
    private final ServerBossEvent bossEvent = (ServerBossEvent)(new ServerBossEvent(this.getDisplayName(), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS)).setDarkenScreen(false);

    public GhastRiderBoss(EntityType<? extends Zombie> p_34271_, Level p_34272_) {
        super(p_34271_, p_34272_);
        this.setPathfindingMalus(BlockPathTypes.DANGER_FIRE, 0.0F);
        this.setPathfindingMalus(BlockPathTypes.DAMAGE_FIRE, 0.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PVZZombie.createAttributes()
                .add(Attributes.MAX_HEALTH, 100D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0D)
                .add(Attributes.ARMOR, 20D)
                .add(PVZAttributes.PLANT_HURT_RESISTANCE.get(), 0.6D);
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
                true, (entity) -> EntityUtil.checkCanEntityBeAttack(this, entity)));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Mob.class,
                true, (entity) -> entity instanceof IPlant && EntityUtil.checkCanEntityBeAttack(this, entity)));
    }

    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
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
    }

    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
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
    }
    @Override
    public void setCustomName(@Nullable Component p_31476_) {
        super.setCustomName(p_31476_);
        this.bossEvent.setName(this.getDisplayName());
    }
    @Override
    public boolean hurt(DamageSource damageSource, float amount) {
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
    public void tick() {
        int storedDeathTime = this.deathTime;
        int boardingCoolDown = this.boardingCooldown;
        if (! level.isClientSide) {
            List<Player> newPlayers = level.getNearbyPlayers(TargetingConditions.forCombat(), this, this.getBoundingBox().inflate(48));
            List<ServerPlayer> currentPlayers = List.copyOf(this.bossEvent.getPlayers());
            for (ServerPlayer player : currentPlayers) {
                if (! newPlayers.contains(player)) this.bossEvent.removePlayer(player);
            }
            for (Player player : newPlayers) {
                if (player instanceof ServerPlayer serverPlayer && ! currentPlayers.contains(serverPlayer)) this.bossEvent.addPlayer(serverPlayer);
            }
            if (this.isAlive()) {
                if (this.homePos == null) {
                    this.homePos = this.blockPosition();
                }
            } else {
                this.stopRiding();
                this.noPhysics = true;
                this.yRot += 5;
                this.setDeltaMovement(0, 0.05, 0);
            }
            this.bossEvent.setProgress(this.isPassenger() && this.getVehicle() instanceof LavaGhastling lavaGhastling ?
                    lavaGhastling.getHealth() / lavaGhastling.getMaxHealth() : this.getHealth() / this.getMaxHealth());
            this.bossEvent.setColor(this.isPassenger() ? BossEvent.BossBarColor.RED : BossEvent.BossBarColor.PURPLE);
        } else {
            if (this.isAlive()) {
                level.addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                        getX(), getY() + 1.05, getZ(),
                        random.nextFloat() * 0.1 - 0.05,
                        random.nextFloat() * 0.15,
                        random.nextFloat() * 0.1 - 0.05);
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
                            getX(), getY() + 0.5, getZ(), 0, 0, 0);
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
