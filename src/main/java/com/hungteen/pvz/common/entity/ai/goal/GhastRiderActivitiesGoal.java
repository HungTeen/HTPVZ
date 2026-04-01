package com.hungteen.pvz.common.entity.ai.goal;

import com.hungteen.pvz.common.capability.entity.PVZEntityCapability;
import com.hungteen.pvz.common.entity.LavaGhastling;
import com.hungteen.pvz.common.entity.creatures.Anger;
import com.hungteen.pvz.common.entity.zombies.FireImp;
import com.hungteen.pvz.common.entity.zombies.GhastRiderBoss;
import com.hungteen.pvz.common.entity.zombies.Imp;
import com.hungteen.pvz.common.entity.zombies.PVZZombie;
import com.hungteen.pvz.common.register.PVZBlocks;
import com.hungteen.pvz.common.register.PVZEntities;
import com.hungteen.pvz.common.register.PVZItems;
import com.hungteen.pvz.common.register.PVZMobEffects;
import com.hungteen.pvz.util.EntityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class GhastRiderActivitiesGoal extends Goal {

    private final GhastRiderBoss zombie;
    private final PanicGoal panicGoal;
    private final GoToLavaGoal lavaGoal;
    private final AvoidEntityGoal<Player> avoidGoal;
    private final GhastRiderSummonGoal summonAngerGoal;
    private boolean panicOn;
    private boolean lavaOn;
    private boolean avoidOn;
    private boolean angerOn;
    private int lastSummonTick;
    private int unmountedTick;
    private int frozenTick;
    private int panicTick;
    private int crossCooldown;
    private int tpCooldown;
    private final Set<BlockPos> summonPoses;

    static final UUID CROSS_FIRE_ATTRIBUTE_MODIFIER = UUID.fromString("71edaec2-2470-078a-7362-fbb577ca68fc");
    static final int CROSS_COOL_DOWN = 600;
    static final int TP_COOL_DOWN = 200;
    static final int REMOUNT_COOL_DOWN = 100;
    static final int SUMMON_COOL_DOWN_BASE = 60;
    static final int FROZEN_THRESHOLD = 350;


    public GhastRiderActivitiesGoal(GhastRiderBoss zombie) {
        this.zombie = zombie;
        this.panicGoal = new PanicGoal(zombie, 1);
        this.lavaGoal = new GoToLavaGoal(zombie, 1);
        this.avoidGoal = new AvoidEntityGoal<>(zombie, Player.class, 5, 1, 1D);
        this.summonAngerGoal = new GhastRiderSummonGoal(zombie);
        this.panicOn = false;
        this.lavaOn = false;
        this.avoidOn = false;
        this.angerOn = false;
        this.summonPoses = new HashSet<>();
        lastSummonTick = 100;
        unmountedTick = 0;
        frozenTick = 0;
        panicTick = 0;
        crossCooldown = CROSS_COOL_DOWN;
        tpCooldown = TP_COOL_DOWN;
    }

    @Override
    public void tick() {
        double healthRate = zombie.getHealth() / zombie.getMaxHealth();
        if (crossCooldown > 0) crossCooldown --;
        if (tpCooldown > 0) tpCooldown --;
        frozenTick = zombie.getTicksFrozen() > 0 ? ++ frozenTick : 0;
        if (! EntityUtil.isEntityValid(zombie.getTarget())) {
            if (tpCooldown <= 0) {
                switchOffWalkingGoals();
                tpTo(getCenter(), true);
            }
            if (zombie.tickCount % 20 == 0 && atCenter()) {
                zombie.heal(2);
            }
            BlockPos pos = getRandomBlockPosOnPlatform();
            //TODO 破坏灵浮土
            crossCooldown = CROSS_COOL_DOWN;
            tpCooldown = TP_COOL_DOWN;
            lastSummonTick = 0;
            panicTick = 0;
            return;
        }
        if (zombie.isPassenger()) {
            unmountedTick = 0;
            switchOffWalkingGoals();
            //controlling ghast
            if (zombie.tickCount % 50 == 0
                    && zombie.getVehicle() instanceof LavaGhastling lavaGhastling) {
                RandomSource randomsource = lavaGhastling.getRandom();
                Vec3 pos = zombie.homePos != null ? Vec3.atCenterOf(zombie.homePos) : lavaGhastling.position();
                double d0 = pos.x + (double)((randomsource.nextFloat() * 2.0F - 1.0F) * 16.0F);
                double d1 = pos.y + 5 + randomsource.nextFloat() * 4F;
                double d2 = pos.z + (double)((randomsource.nextFloat() * 2.0F - 1.0F) * 16.0F);
                lavaGhastling.getMoveControl().setWantedPosition(d0, d1, d2, 1.0D);
            }
        } else {
            if (zombie.getHangingEntity() instanceof LavaGhastling lavaGhastling) {
                this.tpCooldown = TP_COOL_DOWN * 2;
                if (crossCooldown <= 0) {
                    this.zombie.setHangingEntity(null);
                } else if (this.zombie.getTicksFrozen() <= FROZEN_THRESHOLD) {
                    this.zombie.setRopeLengthSqr(zombie.getRopeLengthSqr() * 0.8 - 1);
                    if (lavaGhastling.position().distanceTo(this.zombie.position()) < 2) {
                        zombie.startRiding(lavaGhastling);
                        zombie.setHangingEntity(null);
                    }
                }
            } else {
                if (unmountedTick == 1) {
                    this.lastSummonTick = 0;
                }
                //trying to ride ghast
                if (unmountedTick >= REMOUNT_COOL_DOWN * (healthRate > 0.5 ? 2 : 1)
                        && crossCooldown > 0 && ! this.zombie.hasEffect(PVZMobEffects.FREEZE.get())
                        && zombie.getPose() == Pose.STANDING) {
                    List<LavaGhastling> ghasts = this.zombie.level.getEntities(EntityTypeTest.forClass(LavaGhastling.class),
                            zombie.getBoundingBox().inflate(15, 15, 15),
                            ghast -> ! ghast.isVehicle() && ! ghast.isPassenger() && EntityUtil.isTeammate(zombie, ghast)
                                    && ghast.isAlive() && ghast.blockPosition().distSqr(zombie.homePos) < 225
                    );
                    if (! ghasts.isEmpty()) {
                        this.zombie.setHangingEntity(ghasts.get(0));
                        this.zombie.setRopeLengthSqr(zombie.position().distanceToSqr(ghasts.get(0).position()) * 8);
                    }
                }
            }
            unmountedTick ++;
            //lava healing
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
                zombie.getJumpControl().jump();
            }
        }
        //tp to center
        if (tpCooldown <= 0
                    && (panicTick > 100
                    || zombie.blockPosition().distSqr(zombie.homePos) > (zombieHasVehicle() ? 800 : 225)
                    || frozenTick > 600)
                || zombie.isInWall()) {
            panicTick = 0;
            zombie.removeEffect(PVZMobEffects.FREEZE.get());
            zombie.setTicksFrozen(0);
            tpTo(getCenter(), true);
        }
        //summoning
        if (zombie.isPassenger()) {
            if (zombie.tickCount % 2 == 0) angerOn = handleGoal(this.summonAngerGoal, angerOn);
        } else {
            angerOn = switchGoal(this.summonAngerGoal, angerOn, false);
        }
        if (lastSummonTick >= 0) lastSummonTick ++;
        if (! angerOn) {
            if (zombie.getPose() == Pose.CROAKING) {
                switchOffWalkingGoals();
                if (summonPoses.isEmpty()) { //cross summon tick
                    if (crossCooldown > 0) crossCooldown = 0;
                    crossCooldown --;
                    if (crossCooldown < - 60 && (- crossCooldown) % 20 == 0) {
                        if (zombie.getTicksFrozen() <= FROZEN_THRESHOLD && crossCooldown > - 150) {
                            for (Direction direction : List.of(Direction.EAST, Direction.WEST, Direction.SOUTH, Direction.NORTH)) {
                                Anger anger = new Anger(zombie.level);
                                anger.setPos(zombie.position().add(0, 2, 0));
                                anger.getCapability(PVZEntityCapability.CAP).ifPresent(cap -> cap.setOwner(this.zombie));
                                anger.yRot = direction.toYRot();
                                if ((- crossCooldown) % 40 == 0) {
                                    anger.targetSelector.disableControlFlag(Goal.Flag.TARGET);
                                    anger.getAttribute(Attributes.FLYING_SPEED).setBaseValue(1F);
                                }
                                zombie.level.addFreshEntity(anger);
                            }
                        }
                        if (crossCooldown < -170 || frozenTick > 60) {
                            zombie.setPose(Pose.STANDING);
                                crossCooldown = CROSS_COOL_DOWN;
                            EntityUtil.removeModifierFromAttribute(zombie, Attributes.KNOCKBACK_RESISTANCE, CROSS_FIRE_ATTRIBUTE_MODIFIER);
                        }
                    }
                } else { //basic summon tick
                    if (zombie.getTicksFrozen() >= FROZEN_THRESHOLD || lastSummonTick > 0) {
                        zombie.setPose(Pose.STANDING);
                        lastSummonTick = getSummonTickInterval() - 60;
                    } else if (lastSummonTick > -60) {
                        for (BlockPos pos : summonPoses) {
                            ((ServerLevel) zombie.level).sendParticles(ParticleTypes.LAVA,
                                    pos.getX(), pos.getY() + 1, pos.getZ(),
                                    1, 0.5D, 0.0D, 0.5D, 0.0D);
                        }
                        lastSummonTick --;
                    } else {
                        if (! summonPoses.isEmpty()) {
                            List<Entity> list = zombie.level.getEntities(EntityTypeTest.forClass(Entity.class),
                                    new AABB(zombie.homePos.offset(-30, -6, -30),
                                            zombie.homePos.offset(30, 15, 30)),
                                    entity -> true);
                            int fireImpCount = 0;
                            int ghastCount = 0;
                            int diverCount = 0;
                            int tacoCount = 0;
                            int enemyCount = 0;
                            for (Entity entity : list) {
                                if (EntityUtil.isTeammate(zombie, entity)) {
                                    EntityType<?> type = entity.getType();
                                    if (type == PVZEntities.TACO_IMP.get()) tacoCount ++;
                                    if (type == PVZEntities.FIRE_IMP.get()) fireImpCount ++;
                                    if (type == PVZEntities.LAVA_GHASTLING.get()) ghastCount ++;
                                    if (type == PVZEntities.LAVA_DIVER_ZOMBIE.get()) diverCount ++;
                                } else if (entity instanceof LivingEntity && EntityUtil.checkCanEntityBeAttack(zombie, entity)) {
                                    enemyCount ++;
                                }
                            }
                            boolean isAnnoyed = healthRate < 0.9 || enemyCount > 8;
                            boolean phase2 = healthRate < 0.5;
                            EntityType<? extends Entity> entityType = null;
                            if (isAnnoyed && ! zombieHasVehicle() && ghastCount == 0) {
                                entityType = PVZEntities.LAVA_GHASTLING.get();
                            } else if (! isAnnoyed && ! zombieHasVehicle() && tacoCount == 0) {
                                entityType = zombie.getRandom().nextBoolean() ? PVZEntities.IMP.get() : PVZEntities.TACO_IMP.get();
                            } else {
                                Map<EntityType<? extends Entity>, Integer> map = new HashMap<>();
                                map.put(PVZEntities.IMP.get(), phase2 ? 3 : 4);
                                if (fireImpCount + summonPoses.size() < 6) {
                                    map.put(PVZEntities.FIRE_IMP.get(), 1);
                                }
                                if (phase2 && ghastCount <= 1) {
                                    map.put(PVZEntities.LAVA_GHASTLING.get(), 1);
                                }
                                if (phase2 && diverCount + summonPoses.size() <= 3) {
                                    map.put(PVZEntities.LAVA_DIVER_ZOMBIE.get(), 1);
                                }
                                AtomicInteger choice = new AtomicInteger();
                                map.values().forEach(choice::addAndGet);
                                choice.set(zombie.getRandom().nextInt(choice.get()));
                                for (EntityType<? extends Entity> typeToChoose : map.keySet()) {
                                    int i1 = choice.addAndGet(- map.get(typeToChoose));
                                    if (i1 < 0) {
                                        entityType = typeToChoose;
                                        break;
                                    }
                                }
                            }
                            if (entityType != null) {
                                int summonNum = 0;
                                for (BlockPos pos : summonPoses) {
                                    if (ghastCount + summonNum >= 5 && entityType == PVZEntities.LAVA_GHASTLING.get()) {
                                        break;
                                    }
                                    summonNum ++;
                                    Entity entity = entityType.create(zombie.level);
                                    tpTo(entity, pos, false);
                                    entity.setDeltaMovement(0, 0.5, 0);
                                    if (entity instanceof PVZZombie zombie1 && ! zombie1.isBaby()) {
                                        zombie1.setBaby(true);
                                    }
                                    if (entityType == PVZEntities.IMP.get() && entity instanceof Imp zombie1
                                            && phase2 && zombie.getRandom().nextFloat() < 0.3) {
                                        zombie1.setItemSlot(EquipmentSlot.HEAD, PVZItems.CONE_HELMET.get().getDefaultInstance());
                                        zombie1.setItemSlot(EquipmentSlot.FEET, Items.GOLDEN_BOOTS.getDefaultInstance());
                                        if (zombie.getRandom().nextFloat() < 0.3) {
                                            zombie1.setItemSlot(EquipmentSlot.HEAD, PVZItems.BUCKET_HELMET.get().getDefaultInstance());
                                        }
                                    }
                                    for (int x = -1; x < 2; x ++) {
                                        for (int z = -1; z < 2; z ++) {
                                            BlockState state = zombie.level.getBlockState(pos.offset(x, -2, z));
                                            if (state.getBlock().defaultDestroyTime() < 2F || state.getBlock() instanceof LiquidBlock) {
                                                zombie.level.setBlock(pos.offset(x, -2, z), PVZBlocks.FLOATING_SOUL_SOIL.get().defaultBlockState(), 3);
                                            }
                                        }
                                    }
                                    zombie.level.addFreshEntity(entity);
                                    entity.getCapability(PVZEntityCapability.CAP).ifPresent(cap -> cap.setOwner(this.zombie));
                                }
                                summonPoses.clear();
                                zombie.setPose(Pose.STANDING);
                            }
                        }
                        lastSummonTick = 0;
                    }
                }
            } else {
                //moving
                if (! zombie.isPassenger()) {
                    if (healthRate > 0.9) {
                        panicOn = switchGoal(this.panicGoal, panicOn, false);
                    } else {
                        panicOn = handleGoal(this.panicGoal, panicOn);
                    }
                    avoidOn = panicOn ? switchGoal(this.avoidGoal, avoidOn, false)
                            : handleGoal(this.avoidGoal, avoidOn);
                    panicTick = (panicOn || avoidOn) ? ++ panicTick : 0;
                    lavaOn = panicOn || avoidOn ? switchGoal(this.lavaGoal, lavaOn, false)
                            : handleGoal(this.lavaGoal, lavaOn);
                }
                //start summoning
                if (lastSummonTick < 0) lastSummonTick = 0;
                if (crossCooldown <= 0 && tpCooldown <= 0 && healthRate < 0.9) {//cross summon start
                    tpTo(getCenter(), true);
                    zombie.setPose(Pose.CROAKING);
                    EntityUtil.addModifierToAttribute(zombie, Attributes.KNOCKBACK_RESISTANCE,
                            new AttributeModifier(CROSS_FIRE_ATTRIBUTE_MODIFIER, "skill_bonus", 1, AttributeModifier.Operation.ADDITION));
                    crossCooldown --;
                } else if (lastSummonTick >= getSummonTickInterval()) {//basic summon start
                    zombie.setPose(Pose.CROAKING);
                    int summonNum = healthRate > 0.9 ? 1 : ((zombie.isPassenger() ? 0 : 2) + (healthRate > 0.5 ? 1 : 2));
                    summonPoses.clear();
                    for (int i = 0; i < summonNum; i ++) {
                        summonPoses.add(getRandomBlockPosOnPlatform());
                    }
                    lastSummonTick = -1;
                }
            }
        }
    }

    public int getSummonTickInterval() {
        return SUMMON_COOL_DOWN_BASE * (zombieHasVehicle() ? 1 : (zombie.getHealth() / zombie.getMaxHealth() < 0.9 ? 3 : 5));
    }
    public boolean zombieHasVehicle() {
        return zombie.isPassenger() || zombie.getHangingEntity() != null;
    }

    public void tpTo(BlockPos pos, boolean exact) {
        tpTo(zombie, pos, exact);
        tpCooldown = TP_COOL_DOWN;
    }
    public void tpTo(Entity zombie, BlockPos pos, boolean exact) {
        zombie.setPose(Pose.STANDING);
        zombie.stopRiding();
        zombie.teleportTo(pos.getX(), pos.getY(), pos.getZ());
        if (zombie.isInWall()) {
            int offSetTime = 0;
            if (exact) {
                zombie.level.explode(zombie
                        , pos.getX(), pos.getY(), pos.getZ()
                        , 3, Explosion.BlockInteraction.BREAK);
            }
            boolean end = false;
            while (! exact && zombie.isInWall() && offSetTime < 4) {
                offSetTime ++;
                for (int i = -1; i < 2; i ++) {
                    for (int j = -1; j < 2; j ++) {
                        var offseted = zombie.blockPosition().offset(i, 0, j);
                        if (! zombie.level.getBlockState(offseted)
                                .isSuffocating(zombie.level, offseted)) {
                            zombie.teleportTo(offseted.getX(), offseted.getY(), offseted.getZ());
                            end = true;
                            break;
                        }
                    }
                    if (end) break;
                }
            }
            if (! end) {
                tpTo(getRandomBlockPosOnPlatform(), false);
            }
            if (zombie instanceof Mob mob) {
                mob.getNavigation().stop();
                mob.getJumpControl().jump();
            }
        }
    }
    public @NotNull BlockPos getRandomBlockPosOnPlatform() {
        double randomAngle = zombie.getRandom().nextDouble() * 2 * Math.PI;
        double randomLength = zombie.getRandom().nextDouble() * 8 + 7;
        return this.zombie.homePos.offset(Math.sin(randomAngle) * randomLength, 1, Math.cos(randomAngle) * randomLength);
    }

    public BlockPos getCenter() {
        return zombie.homePos.above();
    }

    public boolean atCenter() {
        return zombie.blockPosition().distSqr(getCenter()) < 10;
    }

    @Override
    public boolean canUse() {
        return true;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
    public void save(CompoundTag tag) {
        tag.putInt("LastSummonTick", lastSummonTick);
        tag.putInt("UnmountedTick", unmountedTick);
        tag.putInt("FrozenTick", frozenTick);
        tag.putInt("PanicTick", panicTick);
        tag.putInt("CrossCooldown", crossCooldown);
        tag.putInt("TpCooldown", tpCooldown);
        ListTag summonPosTag = new ListTag();
        summonPoses.forEach(pos -> {
            CompoundTag posTag = new CompoundTag();
            posTag.putInt("x", pos.getX());
            posTag.putInt("y", pos.getY());
            posTag.putInt("z", pos.getZ());
            summonPosTag.add(posTag);
        });
        tag.put("SummonPoses", summonPosTag);
    }
    public void read(CompoundTag tag) {
        lastSummonTick = tag.getInt("LastSummonTick");
        unmountedTick = tag.getInt("UnmountedTick");
        frozenTick = tag.getInt("FrozenTick");
        panicTick = tag.getInt("PanicTick");
        crossCooldown = tag.getInt("CrossCooldown");
        tpCooldown = tag.getInt("TpCooldown");
        ListTag summonPosTag = tag.getList("SummonPoses", Tag.TAG_COMPOUND);
        summonPoses.clear();
        summonPosTag.forEach(posTag -> {
            if (posTag instanceof CompoundTag cTag) {
                summonPoses.add(new BlockPos(cTag.getInt("x"), cTag.getInt("y"), cTag.getInt("z")));
            }
        });
    }

    public boolean switchGoal(Goal goal, boolean currentOn, boolean on) {
        if (currentOn && ! on) {
            goal.stop();
            return false;
        } else if (! currentOn && on && goal.canUse()) {
            goal.start();
            return true;
        }
        return currentOn;
    }

    public boolean handleGoal(Goal goal, boolean currentOn) {
        if (currentOn) {
            if (! (goal.canContinueToUse())) {
                goal.stop();
                return false;
            }
            goal.tick();
            return true;
        } else {
            if (goal.canUse()) {
                goal.start();
                goal.tick();
                return true;
            }
            return false;
        }
    }

    public void switchOffWalkingGoals() {
        panicOn = switchGoal(this.panicGoal, panicOn, false);
        avoidOn = switchGoal(this.avoidGoal, avoidOn, false);
        lavaOn = switchGoal(this.lavaGoal, lavaOn, false);
        panicTick = 0;
        zombie.getNavigation().stop();
    }

    public static class GoToLavaGoal extends MoveToBlockGoal {
        private final GhastRiderBoss zombie;

        GoToLavaGoal(GhastRiderBoss p_33955_, double p_33956_) {
            super(p_33955_, p_33956_, 8, 2);
            this.zombie = p_33955_;
        }
        protected int nextStartTick(PathfinderMob p_25618_) {
            return p_25618_.getRandom().nextInt(20);
        }

        public BlockPos getMoveToTarget() {
            return this.blockPos;
        }

        public boolean canContinueToUse() {
            return this.zombie.getHealth() / this.zombie.getMaxHealth() < 0.5
                    && ! zombie.isPassenger() && zombie.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE) < 1 && this.isValidTarget(this.zombie.level, this.blockPos);
        }

        public boolean canUse() {
            return this.zombie.getHealth() / this.zombie.getMaxHealth() < 0.5
                    && ! zombie.isPassenger() && zombie.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE) < 1 && super.canUse();
        }

        public boolean shouldRecalculatePath() {
            return this.tryTicks % 20 == 0;
        }

        protected boolean isValidTarget(LevelReader level, BlockPos pos) {
            return level.getBlockState(pos).is(Blocks.LAVA)
                    && level.getBlockState(pos.above()).isPathfindable(level, pos, PathComputationType.LAND)
                    && this.zombie.homePos != null && pos.distSqr(this.zombie.homePos) < 225;
        }
    }

    public static class GhastRiderSummonGoal extends FireImp.FireImpSummonGoal {
        public GhastRiderSummonGoal(Mob zombie) {
            super(zombie);
            this.summonTimes = 4;
            this.angerLife = 120;
            this.spellInterval = 300;
        }
        @Override
        public boolean canUse() {
            double healthRate = zombie.getHealth() / zombie.getMaxHealth();
            return super.canUse() && healthRate < 0.8 && zombie.isPassenger();
        }
        @Override
        public boolean canContinueToUse() {
            double healthRate = zombie.getHealth() / zombie.getMaxHealth();
            return super.canContinueToUse() && healthRate < 0.8 && zombie.isPassenger();
        }
    }
}
