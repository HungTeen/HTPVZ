package com.hungteen.pvz.common.entity.zombies;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.api.interfaces.IPlant;
import com.hungteen.pvz.common.capability.entity.PVZEntityCapability;
import com.hungteen.pvz.common.entity.LavaGhastling;
import com.hungteen.pvz.common.entity.creatures.Anger;
import com.hungteen.pvz.common.register.PVZAttributes;
import com.hungteen.pvz.common.register.PVZBlocks;
import com.hungteen.pvz.common.register.PVZEntities;
import com.hungteen.pvz.util.EntityUtil;
import com.mojang.serialization.Dynamic;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class GhastRiderBoss extends FireImp {
    public BlockPos homePos = null;
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
                .add(Attributes.ARMOR, 10D)
                .add(PVZAttributes.PLANT_HURT_RESISTANCE.get(), 0.6D);
    }
    @Override
    protected void addBehaviourGoals() {
        super.addBehaviourGoals();
        this.goalSelector.removeGoal(fireImpShootGoal);
        this.goalSelector.removeGoal(randomStrollGoal);
        this.goalSelector.removeGoal(attackGoal);
        fireImpShootGoal = new GhastRiderShootGoal(this);
        this.goalSelector.addGoal(1, fireImpShootGoal);
        this.goalSelector.addGoal(1, new RideLavaGhastlingGoal(this));
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 32.0F, 1));
        this.goalSelector.addGoal(1, new GhastRiderActivitiesGoal(this));
        this.goalSelector.addGoal(1, new GoToLavaGoal(this, 1F));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1F));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Player.class, 5, 1, 1D));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Mob.class,
                true, (entity) -> entity instanceof IPlant && EntityUtil.checkCanEntityBeAttack(this, entity)));
    }

    @Override
    public void startSeenByPlayer(ServerPlayer p_31483_) {
        super.startSeenByPlayer(p_31483_);
        this.bossEvent.addPlayer(p_31483_);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer p_31488_) {
        super.stopSeenByPlayer(p_31488_);
        this.bossEvent.removePlayer(p_31488_);
    }

    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (this.homePos != null) {
            BlockPos.CODEC.encodeStart(NbtOps.INSTANCE, homePos).resultOrPartial(PVZMod.LOGGER::error)
                    .ifPresent((p_219418_) -> tag.put("HomePos", p_219418_));
        }
    }

    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("HomePos")) {
            BlockPos.CODEC.parse(new Dynamic<>(NbtOps.INSTANCE, tag.getCompound("HomePos"))).resultOrPartial(PVZMod.LOGGER::error)
                    .ifPresent((p_219408_) -> this.homePos = p_219408_);
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
    public static class GhastRiderActivitiesGoal extends Goal {
        GhastRiderBoss zombie;
        static UUID CROSS_FIRE_ATTRIBUTE_MODIFIER = UUID.fromString("71edaec2-2470-078a-7362-fbb577ca68fc");
        int nextSummon = 250;
        int crossFireTime = 2000;
        boolean riding = true;
        BlockPos nextSummonPos = null;
        public GhastRiderActivitiesGoal(GhastRiderBoss zombie) {
            this.zombie = zombie;
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
            double healthRate = zombie.getHealth() / zombie.getMaxHealth();
            // move back when to far or cross fire is charged
            if (this.zombie.homePos != null
                    && (this.zombie.blockPosition().distSqr(zombie.homePos) > 400 && ! this.zombie.isPassenger()
                    || (this.crossFireTime <= 0 && this.zombie.blockPosition().distSqr(zombie.homePos) > 9))) {
                if (this.zombie.isPassenger()) {
                    zombie.stopRiding();
                }
                this.zombie.navigation.stop();
                this.zombie.teleportTo(this.zombie.homePos.getX() + 0.5F, this.zombie.homePos.getY() + 1, this.zombie.homePos.getZ() + 0.5F);
                this.zombie.setDeltaMovement(0, 0.5, 0);
            }
            // healing when on lava
            this.zombie.setPathfindingMalus(BlockPathTypes.LAVA, healthRate < 0.5 ? 0F : 16F);
            if (zombie.isInLava()) {
                zombie.jumpControl.jump();
            }
            if (zombie.level.getBlockState(this.zombie.getOnPos()).getBlock() == Blocks.LAVA) {
                zombie.heal(1.5F);
                for (int i = 0; i < 15; i ++) {
                    ((ServerLevel) zombie.level).sendParticles(ParticleTypes.COMPOSTER,
                            zombie.getX(), zombie.getY() + 1.5, zombie.getZ(),
                            1, 1D, 1D, 1D, 0.0D);
                }
                for (int x = -1; x < 2; x ++) {
                    for (int z = -1; z < 2; z ++) {
                        BlockState state = zombie.level.getBlockState(this.zombie.getOnPos().offset(x, -1, z));
                        if (state.getBlock().defaultDestroyTime() < 2F || state.getBlock() instanceof LiquidBlock) {
                            zombie.level.setBlock(this.zombie.getOnPos().offset(x, -1, z), PVZBlocks.FLOATING_SOUL_SOIL.get().defaultBlockState(), 3);
                        }
                        state = zombie.level.getBlockState(this.zombie.getOnPos());
                        if (state.getBlock().defaultDestroyTime() < 2F || state.getBlock() instanceof LiquidBlock) {
                            zombie.level.setBlock(this.zombie.getOnPos(), PVZBlocks.FLOATING_SOUL_SOIL.get().defaultBlockState(), 3);
                        }
                    }
                }
            }
            // mount ghastling when unoccupied
            boolean hasTarget = EntityUtil.isEntityValid(zombie.getTarget());
            if (zombie.getHangingEntity() instanceof LavaGhastling lavaGhastling) {
                if (crossFireTime <= 0) {
                    this.zombie.setHangingEntity(null);
                } else if (this.zombie.getTicksFrozen() <= 350) {
                    this.zombie.setRopeLengthSqr(zombie.getRopeLengthSqr() * 0.8 - 1);
                    if (lavaGhastling.position().distanceTo(this.zombie.position()) < 2) {
                        zombie.startRiding(lavaGhastling);
                        zombie.setHangingEntity(null);
                    }
                }
            } else if (! zombie.isPassenger()) {
                if (this.riding) {
                    this.nextSummon = Math.max(this.nextSummon, 250);
                    this.zombie.boardingCooldown = 300;
                    this.riding = false;
                }
                if (zombie.boardingCooldown <= 0 && crossFireTime > 0 && this.zombie.getTicksFrozen() <= 350) {
                    List<LavaGhastling> ghasts = this.zombie.level.getEntities(EntityTypeTest.forClass(LavaGhastling.class),
                            zombie.getBoundingBox().inflate(15, 15, 15),
                            ghast -> ! ghast.isVehicle() && ! ghast.isPassenger() && EntityUtil.isTeammate(zombie, ghast)
                                    && ghast.isAlive() && ghast.blockPosition().distSqr(zombie.homePos) < 225
                    );
                    if (! ghasts.isEmpty()) {
                        this.zombie.setHangingEntity(ghasts.get(0));
                        this.zombie.setRopeLengthSqr(zombie.position().distanceToSqr(ghasts.get(0).position()) * 8);
                        this.riding = true;
                    }
                }
            } else if ((! hasTarget) && zombie.blockPosition().distSqr(zombie.homePos) > 3) {
                Path path = zombie.navigation.createPath(zombie.homePos, 1);
                if (path != null) {
                    zombie.getNavigation().path = path;
                }
            }
            if (! hasTarget) {
                // off-battle healing
                if (zombie.tickCount % 40 == 0) {
                    zombie.heal(2F);
                }
                crossFireTime = 2000;
                nextSummon = 400;
            } else {
                if (zombie.getTicksFrozen() <= 350) {
                    nextSummon --;
                    //determine summoning position
                    if (nextSummon < 50) {
                        if (nextSummonPos == null) {
                            double randomAngle = zombie.random.nextDouble() * 2 * Math.PI;
                            double randomLength = zombie.random.nextDouble() * 8 + 7;
                            if (! this.zombie.isPassenger() && healthRate < 0.9) {
                                nextSummonPos = new BlockPos(zombie.blockPosition().getX(), this.zombie.homePos.getY(), zombie.blockPosition().getZ());
                            } else {
                                nextSummonPos = this.zombie.homePos.offset(Math.sin(randomAngle) * randomLength, 0, Math.cos(randomAngle) * randomLength);
                            }
                        } else {
                            ((ServerLevel) zombie.level).sendParticles(ParticleTypes.LAVA,
                                    nextSummonPos.getX(), nextSummonPos.getY() + 1, nextSummonPos.getZ(),
                                    1, 0.5D, 0.0D, 0.5D, 0.0D);
                        }
                        if (zombie.level.getBlockState(nextSummonPos).isCollisionShapeFullBlock(zombie.level, nextSummonPos)
                                || ! zombie.level.getEntities(EntityTypeTest.forClass(Entity.class),
                                new AABB(nextSummonPos.offset(-2, -2, -2), nextSummonPos.offset(2, 2,2)),
                                entity -> ! EntityUtil.isTeammate(zombie, entity)).isEmpty()) {
                            nextSummonPos = null;
                        }
                    }
                    //summoning
                    if (zombie.getTicksFrozen() <= 0 && nextSummon <= 0 && nextSummonPos != null) {
                        for (int x = -1; x < 2; x ++) {
                            for (int z = -1; z < 2; z ++) {
                                BlockState state = zombie.level.getBlockState(nextSummonPos.offset(x, -1, z));
                                if (state.getBlock().defaultDestroyTime() < 2F || state.getBlock() instanceof LiquidBlock) {
                                    zombie.level.setBlock(nextSummonPos.offset(x, -1, z), PVZBlocks.FLOATING_SOUL_SOIL.get().defaultBlockState(), 3);
                                }
                            }
                        }
                        EntityType<? extends Entity> entityType = null;
                        for (int i = 0; i < (healthRate < 0.33 ? 2 : 1); i ++) {
                            int fireImpCount = 0;
                            int lavaGhastlingCount = 0;
                            int tacoCount = 0;
                            int enemyCount = 0;
                            List<Entity> list = zombie.level.getEntities(EntityTypeTest.forClass(Entity.class),
                                    new AABB(zombie.homePos.offset(-30, -6, -30),
                                            zombie.homePos.offset(30, 15, 30)),
                                    entity -> true);
                            for (Entity entity : list) {
                                if (EntityUtil.isTeammate(zombie, entity)) {
                                    EntityType<?> type = entity.getType();
                                    if (type == PVZEntities.TACO_IMP.get()) tacoCount ++;
                                    if (type == PVZEntities.FIRE_IMP.get()) fireImpCount ++;
                                    if (type == PVZEntities.LAVA_GHASTLING.get()) lavaGhastlingCount ++;
                                } else if (entity instanceof LivingEntity && EntityUtil.checkCanEntityBeAttack(zombie, entity)) {
                                    enemyCount ++;
                                }
                            }
                            boolean isAnnoyed = healthRate < 0.9 || enemyCount > 12;
                            if (! this.zombie.isPassenger() && ! this.zombie.isHanging()
                                    && isAnnoyed && crossFireTime > 0 && lavaGhastlingCount == 0) {
                                entityType = PVZEntities.LAVA_GHASTLING.get();
                            } else {
                                Map<EntityType<? extends Entity>, Integer> map = new HashMap<>();
                                map.put(PVZEntities.IMP.get(), healthRate < 0.66 ? 8 : 3);
                                if (isAnnoyed && lavaGhastlingCount < 3) {
                                    map.put(PVZEntities.LAVA_GHASTLING.get(), healthRate > 0.66 ? 1 : (healthRate < 0.33 ? 5 : 3));
                                }
                                if (healthRate > 0.66) {
                                    if (enemyCount < 8) {
                                        map.put(PVZEntities.TACO_IMP.get(), tacoCount == 0 ? (isAnnoyed ? 8 : 4) : 1);
                                    }
                                } else if (fireImpCount < 3) {
                                    map.put(PVZEntities.FIRE_IMP.get(), healthRate < 0.33 ? 2 : 1);
                                }
                                AtomicInteger choice = new AtomicInteger();
                                map.values().forEach(choice::addAndGet);
                                choice.set(zombie.random.nextInt(choice.get()));
                                for (EntityType<? extends Entity> typeToChoose : map.keySet()) {
                                    int i1 = choice.addAndGet(- map.get(typeToChoose));
                                    if (i1 < 0) {
                                        entityType = typeToChoose;
                                    }
                                }
                            }
                            if (entityType != null) {
                                Entity entity = entityType.create(zombie.level);
                                entity.setPos(Vec3.atCenterOf(nextSummonPos));
                                entity.setDeltaMovement(0, 0.5, 0);
                                if (entity instanceof Zombie zombie1 && healthRate < 0.66 && zombie.random.nextFloat() < 0.33) {
                                    zombie1.setItemSlot(EquipmentSlot.HEAD, Items.GOLDEN_HELMET.getDefaultInstance());
                                    if (healthRate < 0.33 && zombie.random.nextFloat() < 0.5) {
                                        zombie1.setItemSlot(EquipmentSlot.CHEST, Items.GOLDEN_CHESTPLATE.getDefaultInstance());
                                        zombie1.setItemSlot(EquipmentSlot.FEET, Items.GOLDEN_BOOTS.getDefaultInstance());
                                        zombie1.setItemSlot(EquipmentSlot.MAINHAND, Items.GOLDEN_SWORD.getDefaultInstance());
                                    }
                                }
                                zombie.level.addFreshEntity(entity);
                                entity.getCapability(PVZEntityCapability.CAP).ifPresent(cap -> cap.setOwner(this.zombie));
                            }
                        }
                        nextSummonPos = null;
                        nextSummon += (int) (300 * healthRate * healthRate) + 100;
                    }
                    if (crossFireTime > 0) {
                        crossFireTime --;
                        EntityUtil.removeModifierFromAttribute(zombie, Attributes.KNOCKBACK_RESISTANCE, CROSS_FIRE_ATTRIBUTE_MODIFIER);
                    } else {
                        EntityUtil.addModifierToAttribute(zombie, Attributes.KNOCKBACK_RESISTANCE,
                                new AttributeModifier(CROSS_FIRE_ATTRIBUTE_MODIFIER, "skill_bonus", 1, AttributeModifier.Operation.ADDITION));
                    }
                }
                if (crossFireTime <= 0 && zombie.blockPosition().distSqr(zombie.homePos) < 3) {
                    this.zombie.navigation.stop();
                    if (zombie.tickCount % 10 == 0) {
                        crossFireTime -= 1;
                        if (crossFireTime >= -12 && zombie.getTicksFrozen() <= 350) {
                            for (Direction direction : List.of(Direction.EAST, Direction.WEST, Direction.SOUTH, Direction.NORTH)) {
                                Anger anger = new Anger(zombie.level);
                                anger.setPos(zombie.position().add(0, 2, 0));
                                anger.getCapability(PVZEntityCapability.CAP).ifPresent(cap -> cap.setOwner(this.zombie));
                                anger.yRot = direction.toYRot();
                                if ((- crossFireTime) % 2 == 0) {
                                    anger.targetSelector.disableControlFlag(Goal.Flag.TARGET);
                                    anger.getAttribute(Attributes.FLYING_SPEED).setBaseValue(1F);
                                }
                                zombie.level.addFreshEntity(anger);
                            }
                        } else if (crossFireTime < -16) {
                            crossFireTime = 2000;
                        }
                    }
                }
            }
        }
    }
    static class GoToLavaGoal extends MoveToBlockGoal {
        private final GhastRiderBoss ghastRider;

        GoToLavaGoal(GhastRiderBoss p_33955_, double p_33956_) {
            super(p_33955_, p_33956_, 8, 2);
            this.ghastRider = p_33955_;
        }
        protected int nextStartTick(PathfinderMob p_25618_) {
            return p_25618_.getRandom().nextInt(20);
        }

        public BlockPos getMoveToTarget() {
            return this.blockPos;
        }

        public boolean canContinueToUse() {
            return this.ghastRider.getHealth() / this.ghastRider.getMaxHealth() < 0.5
                    && ! ghastRider.isPassenger() && ghastRider.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE) < 1 && this.isValidTarget(this.ghastRider.level, this.blockPos);
        }

        public boolean canUse() {
            return this.ghastRider.getHealth() / this.ghastRider.getMaxHealth() < 0.5
                    && ! ghastRider.isPassenger() && ghastRider.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE) < 1 && super.canUse();
        }

        public boolean shouldRecalculatePath() {
            return this.tryTicks % 20 == 0;
        }

        protected boolean isValidTarget(LevelReader level, BlockPos pos) {
            return level.getBlockState(pos).is(Blocks.LAVA)
                    && level.getBlockState(pos.above()).isPathfindable(level, pos, PathComputationType.LAND)
                    && this.ghastRider.homePos != null && pos.distSqr(this.ghastRider.homePos) < 225;
        }
    }
    public static class GhastRiderShootGoal extends FireImpShootGoal {
        public GhastRiderShootGoal(Mob zombie) {
            super(zombie);
        }
        @Override
        public boolean canUse() {
            double healthRate = zombie.getHealth() / zombie.getMaxHealth();
            return super.canUse() && (healthRate > 0.4 && healthRate < 0.95 || zombie.isPassenger());
        }
        @Override
        public void tick() {
            if (zombie.getTicksFrozen() <= 0 && zombie.getPose() == Pose.CROAKING) {
                zombie.getNavigation().stop();
                if (EntityUtil.isEntityValid(zombie.getTarget())) {
                    zombie.lookAt(zombie.getTarget(), 10, 10);
                }
                if (zombie.tickCount % 30 <= 1 && zombie.tickCount % 300 > 50) {
                    Anger anger = new Anger(zombie.level);
                    anger.setDeltaMovement(this.zombie.getDeltaMovement());
                    anger.setPos(this.zombie.position().add(0, 1.6F, 0));
                    anger.setTarget(zombie.getTarget());
                    anger.getCapability(PVZEntityCapability.CAP).ifPresent(cap -> cap.setOwner(this.zombie));
                    anger.yRot = this.zombie.yRot;
                    anger.xRot = this.zombie.xRot;
                    anger.maxLife = 120;
                    zombie.level.addFreshEntity(anger);
                    anger.getAttribute(Attributes.FLYING_SPEED).setBaseValue(0.6F);
                    Scoreboard scoreboard = zombie.level.getScoreboard();
                    PlayerTeam team = scoreboard.getPlayersTeam(zombie.getScoreboardName());
                    PlayerTeam team1 = scoreboard.getPlayerTeam(PVZMod.ENEMY_TEAM);
                    if (team1 != null) {
                        scoreboard.addPlayerToTeam(anger.getScoreboardName(), team == null ? team1 : team);
                    } else if (team != null) {
                        scoreboard.addPlayerToTeam(anger.getScoreboardName(), team);
                    }
                }
            }
        }
    }
    static class RideLavaGhastlingGoal extends Goal {
        private final GhastRiderBoss ghastRider;

        public RideLavaGhastlingGoal(GhastRiderBoss p_32783_) {
            this.ghastRider = p_32783_;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        public boolean canUse() {
            if (ghastRider.getVehicle() instanceof LavaGhastling lavaGhastling) {
                MoveControl movecontrol = lavaGhastling.getMoveControl();
                if (! movecontrol.hasWanted()) {
                    return true;
                } else {
                    double d0 = movecontrol.getWantedX() - lavaGhastling.getX();
                    double d1 = movecontrol.getWantedY() - lavaGhastling.getY();
                    double d2 = movecontrol.getWantedZ() - lavaGhastling.getZ();
                    double d3 = d0 * d0 + d1 * d1 + d2 * d2;
                    return d3 < 1.0D || d3 > 3600.0D;
                }
            } else return false;
        }

        public boolean canContinueToUse() {
            return ghastRider.getVehicle() instanceof LavaGhastling && ghastRider.getRandom().nextInt(10) != 0;
        }

        public void start() {
            if (ghastRider.getVehicle() instanceof LavaGhastling lavaGhastling) {
                RandomSource randomsource = lavaGhastling.getRandom();
                Vec3 pos = ghastRider.homePos != null ? Vec3.atCenterOf(ghastRider.homePos) : lavaGhastling.position();
                double d0 = pos.x + (double)((randomsource.nextFloat() * 2.0F - 1.0F) * 16.0F);
                double d1 = pos.y + 5 + randomsource.nextFloat() * 4F;
                double d2 = pos.z + (double)((randomsource.nextFloat() * 2.0F - 1.0F) * 16.0F);
                lavaGhastling.getMoveControl().setWantedPosition(d0, d1, d2, 1.0D);
            }
        }
    }
}
