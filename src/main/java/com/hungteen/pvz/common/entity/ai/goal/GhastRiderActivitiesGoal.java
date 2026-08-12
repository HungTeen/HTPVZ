package com.hungteen.pvz.common.entity.ai.goal;

import com.hungteen.pvz.common.capability.entity.PVZEntityCapability;
import com.hungteen.pvz.common.entity.LavaGhastling;
import com.hungteen.pvz.common.entity.creatures.Anger;
import com.hungteen.pvz.common.entity.zombies.FireImp;
import com.hungteen.pvz.common.entity.zombies.GhastRiderBoss;
import com.hungteen.pvz.common.entity.zombies.JackInABoxZombie;
import com.hungteen.pvz.common.entity.zombies.PVZZombie;
import com.hungteen.pvz.common.register.*;
import com.hungteen.pvz.common.tags.PVZBlockTags;
import com.hungteen.pvz.util.EntityUtil;
import com.hungteen.pvz.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.monster.Strider;
import net.minecraft.world.entity.player.Player;
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
    private int hangingTick;
    private int battleStartTick;
    private int crossCooldown;
    private int tpCooldown;
    private final Set<BlockPos> summonPoses;

    public static UUID PHASE2_MODIFIER_UUID = UUID.fromString("33ffd765-acbb-d867-840b-2726daa6c654");
    public static final UUID GHAST_RIDER_MODIFIER_UUID = UUID.fromString("33ffd765-acbb-d867-840b-2726daa6c656");
    static final UUID CROSS_FIRE_ATTRIBUTE_MODIFIER = UUID.fromString("71edaec2-2470-078a-7362-fbb577ca68fc");
    static final int CROSS_COOL_DOWN = 2400;
    static final int TP_COOL_DOWN = 200;
    static final int REMOUNT_COOL_DOWN = 40;
    static final int SUMMON_COOL_DOWN_BASE = 40;
    static final int FROZEN_THRESHOLD = 20;
    static final int MAX_FROZEN_TICK = 120;


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
        battleStartTick = 0;
        frozenTick = 0;
        panicTick = 0;
        hangingTick = 0;
        crossCooldown = CROSS_COOL_DOWN;
        tpCooldown = TP_COOL_DOWN;
    }

    @Override
    public void tick() {
        double healthRate = zombie.getHealth() / zombie.getMaxHealth();
        if (crossCooldown > 0) crossCooldown --;
        if (tpCooldown > 0) tpCooldown --;
        frozenTick = zombie.getTicksFrozen() > 0 ? ++ frozenTick : 0;
        //removing dead ghast from list
        Set.copyOf(this.zombie.ghastlings).forEach(ghast -> {
            if (ghast.isDeadOrDying()) this.zombie.ghastlings.remove(ghast);
        });
        if (! EntityUtil.isEntityValid(zombie.getTarget())) {
            if (tpCooldown <= 0) {
                switchOffWalkingGoals();
                zombie.setHangingEntity(null);
                tpTo(getCenter(), true);
                tpCooldown = TP_COOL_DOWN;
            }
            if (zombie.tickCount % 5 == 0 && atCenter()) {
                zombie.heal(0.5f);

                if (zombie.isPhase2() && healthRate > 0.75) {
                    zombie.setPhase2(false);
                    EntityUtil.removeModifierFromAttribute(this.zombie, Attributes.ARMOR, PHASE2_MODIFIER_UUID);
                    EntityUtil.removeModifierFromAttribute(this.zombie, Attributes.ARMOR_TOUGHNESS, PHASE2_MODIFIER_UUID);
                }
                BlockPos pos = getRandomBlockPosOnPlatform().below().below();
                if (zombie.level.getBlockState(pos).is(PVZBlocks.FLOATING_SOUL_SOIL.get()) && ! zombie.level.getBlockState(pos.above()).is(BlockTags.FIRE)) {
                    zombie.level.setBlock(pos, Blocks.LAVA.defaultBlockState(), 3);
                }
            }
            crossCooldown = CROSS_COOL_DOWN;
            lastSummonTick = 300;
            panicTick = 0;
            if (battleStartTick > 0) battleStartTick --;
            return;
        }
        battleStartTick ++;
        if (zombie.isPassenger()) {
            this.hangingTick = 0;
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
                this.hangingTick ++;
                if (crossCooldown <= 0) {
                    this.zombie.setHangingEntity(null);
                } else if (! this.zombie.hasEffect(PVZMobEffects.FREEZE.get())) {
                    this.zombie.setRopeLengthSqr(zombie.getRopeLengthSqr() * 0.8 - 1);
                    if (lavaGhastling.position().distanceTo(this.zombie.position()) < 2) {
                        zombie.startRiding(lavaGhastling);
                        zombie.setHangingEntity(null);
                    }
                }
            } else {
                this.hangingTick = 0;
                if (unmountedTick == 1) {
                    this.lastSummonTick = 0;
                    this.summonPoses.clear();
                }
                //trying to ride ghast
                if (unmountedTick >= REMOUNT_COOL_DOWN * (healthRate > 0.5 ? 3 : 1)
                        && crossCooldown > 0 && ! this.zombie.hasEffect(PVZMobEffects.FREEZE.get())
                        && zombie.getPose() == Pose.STANDING) {
                    LavaGhastling ghastling = this.zombie.ghastlings.stream().filter(ghast ->
                            ! ghast.isVehicle() && ! ghast.isPassenger() && EntityUtil.isTeammate(zombie, ghast)
                            && ghast.isAlive() && ghast.blockPosition().distSqr(zombie.homePos) < 800
                            && Util.hasBlockBetween(this.zombie.level, zombie.position(), ghast.position())).findAny().orElse(null);
                    if (ghastling != null) {
                        this.zombie.setHangingEntity(ghastling);
                        this.zombie.setRopeLengthSqr(zombie.position().distanceToSqr(ghastling.position()) * 8);
                    }
                }
            }
            unmountedTick ++;
            //lava healing
            if (zombie.level.getBlockState(this.zombie.getOnPos()).getBlock() == Blocks.LAVA) {
                zombie.heal(1F);
                for (int i = 0; i < 15; i ++) {
                    ((ServerLevel) zombie.level).sendParticles(ParticleTypes.COMPOSTER,
                            zombie.getX(), zombie.getY() + 1.5, zombie.getZ(),
                            1, 1D, 1D, 1D, 0.0D);
                }
                for (int x = -1; x < 2; x ++) {
                    for (int z = -1; z < 2; z ++) {
                        BlockState state = zombie.level.getBlockState(this.zombie.getOnPos().offset(x, -1, z));
                        if ((! state.is(PVZBlockTags.PLANTABLE_DIRT) && state.getBlock().defaultDestroyTime() < 2F) || state.getBlock() instanceof LiquidBlock) {
                            zombie.level.setBlock(this.zombie.getOnPos().offset(x, -1, z), PVZBlocks.FLOATING_SOUL_SOIL.get().defaultBlockState(), 3);
                        }
                        state = zombie.level.getBlockState(this.zombie.getOnPos());
                        if ((! state.is(PVZBlockTags.PLANTABLE_DIRT) && state.getBlock().defaultDestroyTime() < 2F) || state.getBlock() instanceof LiquidBlock) {
                            zombie.level.setBlock(this.zombie.getOnPos(), PVZBlocks.FLOATING_SOUL_SOIL.get().defaultBlockState(), 3);
                        }
                    }
                }
                zombie.getJumpControl().jump();
            }
        }
        //tp to safe place
        if (tpCooldown <= 0
                && (panicTick > 100
                        || zombie.blockPosition().distSqr(zombie.homePos) > (zombieHasVehicle() ? 800 : 225)
                        || hangingTick > 60
                        || (frozenTick > MAX_FROZEN_TICK && ! zombie.isPassenger()))
                || zombie.isInWall()) {
            panicTick = 0;
            BlockPos pos = zombie.blockPosition();
            zombie.level.explode(zombie
                    , pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5
                    , 3, Explosion.BlockInteraction.BREAK);
            tpTo(getSafePosition(), true);
            zombie.removeEffect(PVZMobEffects.FREEZE.get());
            zombie.setTicksFrozen(0);
            zombie.cantFreeze = 100;
            zombie.setDeltaMovement(0, 0.5, 0);
        }

        //phase2 tp
        if (healthRate < 0.5 && ! zombie.isPhase2()) {
            tpTo(getCenter(), true);
            zombie.setPhase2(true);
            EntityUtil.addModifierToAttribute(this.zombie, Attributes.ARMOR,
                    new AttributeModifier(PHASE2_MODIFIER_UUID, "phase2_bonus", 30, AttributeModifier.Operation.ADDITION));
            EntityUtil.addModifierToAttribute(this.zombie, Attributes.ARMOR_TOUGHNESS,
                    new AttributeModifier(PHASE2_MODIFIER_UUID, "phase2_bonus", 20, AttributeModifier.Operation.ADDITION));
            zombie.removeEffect(PVZMobEffects.FREEZE.get());
            zombie.setTicksFrozen(0);
            zombie.cantFreeze = 100;
            LightningBolt lightningbolt = EntityType.LIGHTNING_BOLT.create(zombie.level);
            lightningbolt.moveTo(Vec3.atBottomCenterOf(zombie.getOnPos()));
            lightningbolt.setVisualOnly(true);
            zombie.level.addFreshEntity(lightningbolt);
            List<Entity> list = zombie.level.getEntities(EntityTypeTest.forClass(Entity.class),
                    new AABB(zombie.homePos.offset(-30, -6, -30),
                            zombie.homePos.offset(30, 15, 30)),
                    entity -> EntityUtil.isTeammate(zombie, entity) && entity instanceof LivingEntity && ! (entity instanceof Player));
            list.forEach(entity -> {
                if (entity instanceof LivingEntity living && entity != this.zombie) {
                    entity.hurt(DamageSource.MAGIC, 60);
                    if (living.isDeadOrDying()) living.discard();
                    LightningBolt lightningbolt1 = EntityType.LIGHTNING_BOLT.create(zombie.level);
                    lightningbolt1.moveTo(Vec3.atBottomCenterOf(entity.getOnPos()));
                    lightningbolt1.setVisualOnly(true);
                    zombie.level.addFreshEntity(lightningbolt1);
                }
            });
            zombie.setDeltaMovement(0, 0.5, 0);
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
                                anger.setPos(zombie.position().add(0, 4, 0));
                                anger.getCapability(PVZEntityCapability.CAP).ifPresent(cap -> cap.setOwner(this.zombie));
                                anger.yRot = direction.toYRot();
                                zombie.level.addFreshEntity(anger);
                            }
                        }
                        if (crossCooldown < -170 || frozenTick > 60) {
                            zombie.setPose(Pose.STANDING);
                                crossCooldown = zombie.isPhase2() ? CROSS_COOL_DOWN * 2 : CROSS_COOL_DOWN;
                            EntityUtil.removeModifierFromAttribute(zombie, Attributes.KNOCKBACK_RESISTANCE, CROSS_FIRE_ATTRIBUTE_MODIFIER);
                        }
                    }
                } else { //basic summon tick
                    if (zombie.getTicksFrozen() >= FROZEN_THRESHOLD || lastSummonTick > 0) {
                        zombie.setPose(Pose.STANDING);
                        lastSummonTick = getSummonTickInterval() - (int) (SUMMON_COOL_DOWN_BASE * (zombie.isPhase2() ? 0.5 : 1));
                    } else if (lastSummonTick > - (SUMMON_COOL_DOWN_BASE * (zombie.isPhase2() ? 0.5 : 1))) {
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
                            int bungeeCount = 0;
                            int jackCount = 0;
                            int tacoCount = 0;
                            int enemyCount = 0;
                            for (Entity entity : list) {
                                if (EntityUtil.isTeammate(zombie, entity)) {
                                    EntityType<?> type = entity.getType();
                                    if (type == PVZEntities.LAVA_GHASTLING.get() && ! zombie.ghastlings.contains(entity)) zombie.ghastlings.add((LavaGhastling) entity);
                                    if (type == PVZEntities.TACO_IMP.get()) tacoCount ++;
                                    if (type == PVZEntities.FIRE_IMP.get()) fireImpCount ++;
                                    if (type == PVZEntities.BUNGEE_ZOMBIE.get()) bungeeCount ++;
                                    if (type == PVZEntities.JACK_IN_A_BOX_ZOMBIE.get()) jackCount ++;
                                } else if (! (entity instanceof Strider) && EntityUtil.checkCanEntityBeAttack(zombie, entity)) {
                                    enemyCount ++;
                                    if (entity instanceof Player player && player.getInventory().hasAnyOf(Set.of(PVZItems.GOLDEN_TACO.get()))) {
                                        tacoCount += player.getInventory().countItem(PVZItems.GOLDEN_TACO.get());
                                    }
                                }
                            }
                            boolean isAnnoyed = healthRate < 0.95 || enemyCount > 10 || battleStartTick > 1500;
                            EntityType<? extends Entity> entityType = null;
                            if (! isAnnoyed) {
                                entityType =  (tacoCount < 3 && zombie.getRandom().nextBoolean()) ? PVZEntities.TACO_IMP.get() : PVZEntities.IMP.get();
                            } else if (isAnnoyed && ! zombieHasVehicle() && zombie.ghastlings.isEmpty()) {
                                entityType = PVZEntities.LAVA_GHASTLING.get();
                            } else {
                                Map<EntityType<? extends Entity>, Integer> map = new HashMap<>();
                                map.put(PVZEntities.IMP.get(), zombie.isPhase2() ? 4 : 7);
                                if (! zombie.isPhase2() && tacoCount <= 2) {
                                    map.put(PVZEntities.TACO_IMP.get(), 1);
                                }
                                if (bungeeCount + summonPoses.size() < 8) {
                                    map.put(PVZEntities.BUNGEE_ZOMBIE.get(), zombie.isPhase2() ? 2 : 4);
                                }
                                if (fireImpCount < (zombie.isPhase2() ? 4 : 0)) {
                                    map.put(PVZEntities.FIRE_IMP.get(), zombie.isPhase2() ? 4 : 2);
                                }
                                if (jackCount == 0) {
                                    map.put(PVZEntities.JACK_IN_A_BOX_ZOMBIE.get(), zombie.isPhase2() ? 2 : 1);
                                }
                                AtomicInteger choice = new AtomicInteger(0);
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
                                for (BlockPos pos : summonPoses) {
                                    Entity entity = entityType.create((ServerLevel) zombie.level, null, null, null, pos, MobSpawnType.SPAWN_EGG, true, false);
                                    entity.setDeltaMovement(0, 0.5, 0);
                                    if (entity instanceof Mob mob) {
                                        mob.setTarget(zombie.getTarget());
                                        if (mob instanceof PVZZombie zombie1 && ! zombie1.isBaby()) {
                                            zombie1.setBaby(true);
                                            if (zombie1 instanceof JackInABoxZombie zombie2) {
                                                LightningBolt lightningbolt = EntityType.LIGHTNING_BOLT.create(zombie.level);
                                                lightningbolt.moveTo(Vec3.atBottomCenterOf(entity.getOnPos()));
                                                lightningbolt.setVisualOnly(true);
                                                zombie.level.addFreshEntity(lightningbolt);
                                                zombie2.thunderHit((ServerLevel) zombie.level, lightningbolt);
                                            }
                                        } else if (mob instanceof LavaGhastling ghastling) {
                                            zombie.ghastlings.add(ghastling);
                                            EntityUtil.addModifierToAttribute(ghastling, PVZAttributes.PLANT_HURT_RESISTANCE.get(),
                                                    new AttributeModifier(GHAST_RIDER_MODIFIER_UUID, "riden_by_boss_bonus", 0.9, AttributeModifier.Operation.ADDITION));
                                        }
                                    }
                                    if (entity instanceof PVZZombie zombie1 && ! (zombie1 instanceof JackInABoxZombie)
                                            && ((zombie.isPhase2() && zombie.getRandom().nextBoolean())
                                                    || zombie.getRandom().nextInt(6) == 0)) {
                                        zombie1.setItemSlot(EquipmentSlot.HEAD, PVZItems.CONE_HELMET.get().getDefaultInstance());
                                        if (zombie.getRandom().nextFloat() < 0.1 && entityType == PVZEntities.IMP.get()) {
                                            zombie1.setItemSlot(EquipmentSlot.HEAD, PVZItems.BUCKET_HELMET.get().getDefaultInstance());
                                        }
                                    }
                                    for (int x = -1; x < 2; x ++) {
                                        for (int z = -1; z < 2; z ++) {
                                            BlockState state = zombie.level.getBlockState(pos.offset(x, -2, z));
                                            if ((! state.is(PVZBlockTags.PLANTABLE_DIRT) && state.getBlock().defaultDestroyTime() < 2F) || state.getBlock() instanceof LiquidBlock) {
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
                if (crossCooldown <= 0 && tpCooldown <= 0) {//cross summon start
                    tpTo(getCenter(), true);
                    zombie.playSound(PVZSoundEvents.GHAST_RIDER_SPELL.get());
                    zombie.setPose(Pose.CROAKING);
                    EntityUtil.addModifierToAttribute(zombie, Attributes.KNOCKBACK_RESISTANCE,
                            new AttributeModifier(CROSS_FIRE_ATTRIBUTE_MODIFIER, "skill_bonus", 1, AttributeModifier.Operation.ADDITION));
                    crossCooldown --;
                } else if (lastSummonTick >= getSummonTickInterval()) {//basic summon start
                    zombie.playSound(PVZSoundEvents.GHAST_RIDER_SPELL.get());
                    zombie.setPose(Pose.CROAKING);
                    int summonNum = zombie.isPhase2() ? 4 : (healthRate < 0.95 || ! this.zombie.ghastlings.isEmpty() || battleStartTick > 1500) ? 3 : 1;
                    summonPoses.clear();
                    for (int i = 0; i < summonNum; i ++) {
                        summonPoses.add(getRandomBlockPosOnPlatform(11));
                    }
                    lastSummonTick = -1;
                }
            }
        }
    }

    public int getSummonTickInterval() {
        return SUMMON_COOL_DOWN_BASE * (zombieHasVehicle() ? 1 : 4) * (zombie.isPhase2() ? 1 : 2);
    }
    public boolean zombieHasVehicle() {
        return zombie.isPassenger() || zombie.getHangingEntity() != null;
    }

    public void tpTo(BlockPos pos, boolean exact) {
        tpTo(zombie, pos, exact);
        tpCooldown = TP_COOL_DOWN;
    }

    public void tpTo(Entity zombie, BlockPos pos, boolean exact) {
        tpTo(zombie, pos, exact, 0);
    }

    public void tpTo(Entity zombie, BlockPos pos, boolean exact, int offset) {
        zombie.setPose(Pose.STANDING);
        zombie.stopRiding();
        zombie.teleportTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        if (zombie.isInWall()) {
            if (exact) {
                zombie.level.explode(zombie
                        , pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5
                        , 3, Explosion.BlockInteraction.BREAK);
            }
            int yOffSetTime = 0;
            boolean end = ! zombie.isInWall() || offset >= 3;
            while (! exact && zombie.isInWall() && yOffSetTime < 4) {
                yOffSetTime ++;
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
                tpTo(zombie, getRandomBlockPosOnPlatform(), true, ++ offset);
            }
            if (zombie instanceof Mob mob) {
                mob.getNavigation().stop();
                mob.getJumpControl().jump();
            }
        }
    }

    public @NotNull BlockPos getRandomBlockPosOnPlatform() {
        return getRandomBlockPosOnPlatform(144);
    }

    public @NotNull BlockPos getRandomBlockPosOnPlatform(int length) {
        double randomAngle = zombie.getRandom().nextDouble() * 2 * Math.PI;
        double randomLength = Math.sqrt(zombie.getRandom().nextDouble() * length + 25);
        return this.zombie.homePos.offset(Math.sin(randomAngle) * randomLength, 1, Math.cos(randomAngle) * randomLength);
    }

    public BlockPos getCenter() {
        return zombie.homePos.above();
    }

    public BlockPos getSafePosition() {
        BlockPos pos = getCenter();
        if (zombie.level.getNearestPlayer(pos.getX(), pos.getY(), pos.getZ(), 4, p -> EntityUtil.checkCanEntityBeAttack(zombie, p)) == null) {
            return pos;
        }
        return getRandomBlockPosOnPlatform();
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
        tag.putInt("BattleStartTick", battleStartTick);
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
        battleStartTick = tag.getInt("BattleStartTick");
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
            this.angerLife = 160;
            this.spellInterval = 100;
        }
        @Override
        public boolean canUse() {
            double healthRate = zombie.getHealth() / zombie.getMaxHealth();
            return super.canUse() && healthRate < 0.9 && zombie.isPassenger();
        }
        @Override
        public boolean canContinueToUse() {
            double healthRate = zombie.getHealth() / zombie.getMaxHealth();
            return super.canContinueToUse() && healthRate < 0.9 && zombie.isPassenger();
        }
    }
}
