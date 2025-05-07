package com.hungteen.pvz.common.entity.zombies;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.api.interfaces.IPlant;
import com.hungteen.pvz.common.capability.entity.PVZEntityCapability;
import com.hungteen.pvz.common.entity.LavaGhastling;
import com.hungteen.pvz.common.entity.creatures.Anger;
import com.hungteen.pvz.common.register.PVZBlocks;
import com.hungteen.pvz.common.register.PVZEntities;
import com.hungteen.pvz.util.EntityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Zombie;
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
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class GhastRiderBoss extends FireImp {
    public BlockPos homePos = null;
    private final ServerBossEvent bossEvent = (ServerBossEvent)(new ServerBossEvent(this.getDisplayName(), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS)).setDarkenScreen(false);

    public GhastRiderBoss(EntityType<? extends Zombie> p_34271_, Level p_34272_) {
        super(p_34271_, p_34272_);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PVZZombie.createAttributes()
                .add(Attributes.MAX_HEALTH, 100D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0D)
                .add(Attributes.ARMOR, 5D);
    }
    @Override
    protected void addBehaviourGoals() {
        super.addBehaviourGoals();
        this.goalSelector.removeGoal(fireImpShootGoal);
        fireImpShootGoal = new GhastRiderShootGoal(this);
        this.goalSelector.addGoal(1, fireImpShootGoal);
        this.goalSelector.addGoal(1, new GhastRiderActivitiesGoal(this));
        this.goalSelector.addGoal(1, new GoToLavaGoal(this, 1F));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1F));
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
        CompoundTag pos = new CompoundTag();
        pos.putInt("x", homePos.getX());
        pos.putInt("y", homePos.getY());
        pos.putInt("z", homePos.getZ());
        tag.put("HomePos", pos);
    }

    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("HomePos")) {
            CompoundTag pos = tag.getCompound("HomePos");
            this.homePos = new BlockPos(pos.getInt("x"), pos.getInt("y"), pos.getInt("z"));
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
        if (this.isPassenger()) {
            return false;
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
        super.tick();
        if (! level.isClientSide) {
            if (this.homePos == null) {
                this.homePos = this.blockPosition();
            }
            this.bossEvent.setProgress(this.isPassenger() && this.getVehicle() instanceof LavaGhastling lavaGhastling ?
                    lavaGhastling.getHealth() / lavaGhastling.getMaxHealth() : this.getHealth() / this.getMaxHealth());
            this.bossEvent.setColor(this.isPassenger() ? BossEvent.BossBarColor.RED : BossEvent.BossBarColor.PURPLE);
        }
    }

    public static class GhastRiderActivitiesGoal extends Goal {
        GhastRiderBoss zombie;
        static UUID CROSS_FIRE_ATTRIBUTE_MODIFIER = UUID.fromString("71edaec2-2470-078a-7362-fbb577ca68fc");
        int nextSummon = 250;
        int crossFireTime = 1000;
        boolean riding = true;
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
                    && (this.zombie.blockPosition().distSqr(zombie.homePos) > 400
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
            if (zombie.level.getBlockState(this.zombie.getOnPos().below()).getBlock() == Blocks.LAVA) {
                zombie.heal(1F);
                for (int x = -1; x < 2; x ++) {
                    for (int z = -1; z < 2; z ++) {
                        BlockState state = zombie.level.getBlockState(this.zombie.getOnPos().offset(x, -1, z));
                        if (state.getBlock().defaultDestroyTime() < 2F || state.getBlock() instanceof LiquidBlock) {
                            zombie.level.setBlock(this.zombie.getOnPos().offset(x, -1, z), PVZBlocks.FLOATING_SOUL_SOIL.get().defaultBlockState(), 3);
                        }
                    }
                }
            }
            // mount ghastling when unoccupied
            boolean hasTarget = EntityUtil.isEntityValid(zombie.getTarget());
            if (! zombie.isPassenger()) {
                if (this.riding) {
                    this.nextSummon = Math.max(this.nextSummon, 250);
                    this.zombie.boardingCooldown = 200;
                    this.riding = false;
                }
                if (zombie.boardingCooldown <= 0 && crossFireTime > 0) {
                    List<LavaGhastling> ghasts = this.zombie.level.getEntities(EntityTypeTest.forClass(LavaGhastling.class),
                            zombie.getBoundingBox().inflate(15, 15, 15),
                            ghast -> ! ghast.isVehicle() && ! ghast.isPassenger() && EntityUtil.isTeammate(zombie, ghast)
                                    && ghast.isAlive() && ghast.blockPosition().distSqr(zombie.homePos) < 225
                    );
                    if (! ghasts.isEmpty()) {
                        this.zombie.setPos(ghasts.get(0).position().add(0, 4, 0));
                        this.zombie.startRiding(ghasts.get(0));
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
                    zombie.heal(1F);
                }
                crossFireTime = 1000;
                nextSummon = 400;
            } else {
                // summoning
                if (zombie.getTicksFrozen() <= 350) {
                    nextSummon --;
                    if (zombie.getTicksFrozen() <= 0 && nextSummon <= 0) {
                        BlockPos pos = this.zombie.homePos;
                        double randomAngle = zombie.random.nextDouble() * 2 * Math.PI;
                        double randomLength = zombie.random.nextDouble() * 8 + 7;
                        if (! this.zombie.isPassenger() && healthRate < 0.9) {
                            pos = new BlockPos(zombie.blockPosition().getX(), pos.getY(), zombie.blockPosition().getZ());
                        } else {
                            pos = pos.offset(Math.sin(randomAngle) * randomLength, -1, Math.cos(randomAngle) * randomLength);
                            for (int x = -1; x < 2; x ++) {
                                for (int z = -1; z < 2; z ++) {
                                    BlockState state = zombie.level.getBlockState(pos.offset(x, 0, z));
                                    if (state.getBlock().defaultDestroyTime() < 2F || state.getBlock() instanceof LiquidBlock) {
                                        zombie.level.setBlock(pos.offset(x, 0, z), PVZBlocks.FLOATING_SOUL_SOIL.get().defaultBlockState(), 3);
                                    }
                                }
                            }
                        }
                        pos = pos.above();
                        EntityType<? extends Entity> spawnType = null;
                        for (int i = 0; i < (healthRate < 0.33 ? 2 : 1); i ++) {
                            if (! this.zombie.isPassenger() && healthRate < 0.9 && crossFireTime > 0) {
                                spawnType = PVZEntities.LAVA_GHASTLING.get();
                            }
                            if (spawnType == null) {
                                int fireImpCount = 0;
                                int lavaGhastlingCount = 0;
                                int tacoCount = 0;
                                List<Entity> list = zombie.level.getEntities(zombie, zombie.getBoundingBox().inflate(20, 20, 20), entity -> true);
                                for (Entity entity : list) {
                                    EntityType<?> type = entity.getType();
                                    if (type == PVZEntities.TACO_IMP.get()) tacoCount ++;
                                    if (type == PVZEntities.FIRE_IMP.get()) fireImpCount ++;
                                    if (type == PVZEntities.LAVA_GHASTLING.get()) lavaGhastlingCount ++;
                                }
                                Map<EntityType<? extends Entity>, Integer> map = new HashMap<>();
                                map.put(PVZEntities.IMP.get(), healthRate < 0.66 ? 8 : 3);
                                if (healthRate < 0.9 && lavaGhastlingCount < 6) {
                                    map.put(PVZEntities.LAVA_GHASTLING.get(), healthRate > 0.66 ? 1 : (healthRate < 0.33 ? 5 : 3));
                                }
                                if (healthRate > 0.66) {
                                    map.put(PVZEntities.TACO_IMP.get(), tacoCount == 0 ? 4 : 1);
                                } else if (fireImpCount < 3) {
                                    map.put(PVZEntities.FIRE_IMP.get(), healthRate < 0.33 ? 2 : 1);
                                }
                                AtomicInteger choice = new AtomicInteger();
                                map.values().forEach(choice::addAndGet);
                                choice.set(zombie.random.nextInt(choice.get()));
                                for (EntityType<? extends Entity> type : map.keySet()) {
                                    int i1 = choice.addAndGet(- map.get(type));
                                    if (i1 < 0) {
                                        spawnType = type;
                                    }
                                }
                            }
                            if (spawnType != null) {
                                PVZMod.LOGGER.info("" + spawnType);
                                Entity entity = spawnType.create(zombie.level);
                                entity.setPos(Vec3.atCenterOf(pos));
                                entity.getCapability(PVZEntityCapability.CAP).ifPresent(cap -> cap.setOwner(this.zombie));
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
                            }
                        }
                        nextSummon += (int) (400 * healthRate * healthRate) + 50;
                    }
                }
                if (crossFireTime > 0) {
                    crossFireTime --;
                    EntityUtil.removeModifierFromAttribute(zombie, Attributes.KNOCKBACK_RESISTANCE, CROSS_FIRE_ATTRIBUTE_MODIFIER);
                } else {
                    EntityUtil.addModifierToAttribute(zombie, Attributes.KNOCKBACK_RESISTANCE,
                            new AttributeModifier(CROSS_FIRE_ATTRIBUTE_MODIFIER, "skill_bonus", 1, AttributeModifier.Operation.ADDITION));
                }
                if (crossFireTime <= 0 && zombie.blockPosition().distSqr(zombie.homePos) < 3) {
                    this.zombie.navigation.stop();
                    if (zombie.tickCount % 20 == 0) {
                        crossFireTime -= 1;
                        if (crossFireTime >= -6 && zombie.getTicksFrozen() <= 350) {
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
                        } else if (crossFireTime < -8) {
                            crossFireTime = 1000;
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
            return this.ghastRider.getHealth() / this.ghastRider.getMaxHealth() < 0.5 && this.isValidTarget(this.ghastRider.level, this.blockPos);
        }

        public boolean canUse() {
            return this.ghastRider.getHealth() / this.ghastRider.getMaxHealth() < 0.5 && super.canUse();
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
    }
}
