package com.hungteen.pvz.common.entity.zombies;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.common.entity.Portal;
import com.hungteen.pvz.common.entity.ai.ChorusTerminatorActivitiesGoal;
import com.hungteen.pvz.common.network.ChorusTerminatorSyncPacket;
import com.hungteen.pvz.common.register.OtherRegisters;
import com.hungteen.pvz.common.register.PVZAttributes;
import com.hungteen.pvz.util.EntityUtil;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.*;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.entity.PartEntity;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;

public class ChorusTerminatorBoss extends PathfinderMob implements Enemy {

    public final ChorusTerminatorPart eye;
    public final ChorusTerminatorPart body;
    public final ChorusTerminatorPart mouth;
    public final ChorusTerminatorPart legLeftFront;
    public final ChorusTerminatorPart legRightFront;
    public final ChorusTerminatorPart legLeftBack;
    public final ChorusTerminatorPart legRightBack;
    public final ChorusTerminatorPart[] subEntities;
    public final ChorusTerminatorPart[] legs;
    public final List<Pair<Portal, Portal>> portals = new ArrayList<>();
    private final List<Pair<UUID, UUID>> pendingPortals = new ArrayList<>();
    public int actionTime = 0;

    public AnimationState idleAnimationState = new AnimationState();
    public AnimationState stiffAnimationState = new AnimationState();
    public AnimationState summonAnimationState = new AnimationState();
    public AnimationState shootShulkerAnimationState = new AnimationState();
    public AnimationState shootBulletsAnimationState = new AnimationState();
    public AnimationState defenceAnimationState = new AnimationState();
    public AnimationState defenceStiffAnimationState = new AnimationState();
    public AnimationState defenceSummonAnimationState = new AnimationState();
    public AnimationState defenceShootAnimationState = new AnimationState();
    public AnimationState dieAnimationState = new AnimationState();
    public AnimationState preDashAnimationState = new AnimationState();
    public AnimationState preJumpAnimationState = new AnimationState();
    public AnimationState preDefenceAnimationState = new AnimationState();
    private final AnimationState[] animationStates = new  AnimationState[] {idleAnimationState, stiffAnimationState, summonAnimationState
            , shootBulletsAnimationState, shootShulkerAnimationState, defenceAnimationState, defenceStiffAnimationState, defenceSummonAnimationState
            , defenceShootAnimationState, dieAnimationState, preDashAnimationState, preJumpAnimationState, preDefenceAnimationState};

    public static final float MAX_SHIELD = 800;
    public static final UUID PLANT_RESISTANCE_UUID = UUID.fromString("816d9831-6c7a-6902-7e64-fad0ce2088b3");
    private static final EntityDataAccessor<Float> SHIELD = SynchedEntityData.defineId(ChorusTerminatorBoss.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> PHASE = SynchedEntityData.defineId(ChorusTerminatorBoss.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<ChorusTerminatorActivitiesGoal.Action> ACTION
            = SynchedEntityData.defineId(ChorusTerminatorBoss.class, OtherRegisters.CHORUS_TERMINATOR_ACTION.get());
    private final ServerBossEvent bossEvent = (ServerBossEvent)
            (new ServerBossEvent(this.getDisplayName(), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS)).setDarkenScreen(false);

    public BlockPos homePos;
    public ChorusTerminatorActivitiesGoal actionGoal;
    public ChorusTerminatorActivitiesGoal.Action lastAction;

    public ChorusTerminatorBoss(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.eye = new ChorusTerminatorPart(this, ChorusTerminatorPart.Type.EYE, "eye", 1F, 1F, false);
        this.body = new ChorusTerminatorPart(this, ChorusTerminatorPart.Type.BODY, "body", 3.5F, 4.5F, true);
        this.mouth = new ChorusTerminatorPart(this, ChorusTerminatorPart.Type.MOUTH, "mouth", 3F, 1F, true);
        this.legLeftFront = new ChorusTerminatorPart(this, ChorusTerminatorPart.Type.LEG, "leg_left_front", 1F, 3F, true);
        this.legRightFront = new ChorusTerminatorPart(this, ChorusTerminatorPart.Type.LEG, "leg_right_front", 1F, 3F, true);
        this.legLeftBack = new ChorusTerminatorPart(this, ChorusTerminatorPart.Type.LEG, "leg_left_back", 1F, 3F, true);
        this.legRightBack = new ChorusTerminatorPart(this, ChorusTerminatorPart.Type.LEG, "leg_right_back", 1F, 3F, true);
        this.subEntities = new ChorusTerminatorPart[]{this.eye, this.body, this.mouth, this.legLeftFront, this.legRightFront, this.legLeftBack, this.legRightBack};
        this.legs = new ChorusTerminatorPart[]{this.legLeftBack, this.legLeftFront, this.legRightFront, this.legRightBack};
        Arrays.stream(this.subEntities).forEach(p -> p.setPos(this.position().add(0, 2, 0)));
        this.setPathfindingMalus(BlockPathTypes.FENCE, 0.0F);
        this.setPathfindingMalus(BlockPathTypes.UNPASSABLE_RAIL, 0.0F);

        this.noPhysics = true;
        this.noCulling = true;
        this.setNoGravity(true);
        this.setId(ENTITY_COUNTER.getAndAdd(this.subEntities.length + 1) + 1);
        this.moveControl = new ChorusTerminatorMoveControl(this);
        this.navigation = new ChorusTerminatorPathNavigation(this, this.level);
    }

    @Override
    public void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(PHASE, 0);
        this.entityData.define(SHIELD, MAX_SHIELD);
        this.entityData.define(ACTION, ChorusTerminatorActivitiesGoal.Action.DEFENCE);
    }

    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();
        this.legLeftFront.setPos(this.position().add(-2.5, 0, -2.5));
        this.legLeftBack.setPos(this.position().add(-2.5, 0, 2.5));
        this.legRightBack.setPos(this.position().add(2.5, 0, 2.5));
        this.legRightFront.setPos(this.position().add(2.5, 0, -2.5));
        this.body.setPos(this.position().add(0, 1, 0));
        this.eye.setPos(body.position().add(0, 1.1, 1.75));
        this.mouth.setPos(body.position());
    }

    public void setPhase(int phase) {
        this.entityData.set(PHASE, phase);
        if (phase > 0) {
            EntityUtil.addModifierToAttribute(this, PVZAttributes.PLANT_HURT_RESISTANCE.get()
                    , new AttributeModifier(PLANT_RESISTANCE_UUID, "phase_addon", phase * 0.3F, AttributeModifier.Operation.ADDITION));
        } else {
            EntityUtil.removeModifierFromAttribute(this, PVZAttributes.PLANT_HURT_RESISTANCE.get(), PLANT_RESISTANCE_UUID);
        }
    }

    public int getPhase() {
        return entityData.get(PHASE);
    }

    public float getShield() {
        return entityData.get(SHIELD);
    }

    public void setShield(float shield) {
        if (shield < 0) shield = 0;
        if (shield > MAX_SHIELD) shield = MAX_SHIELD;
        this.entityData.set(SHIELD, shield);
    }

    @Override
    public void setSecondsOnFire(int seconds) {
        super.setSecondsOnFire(Math.min(3, seconds));
    }

    public void addShield(float value) {
        this.setShield(this.entityData.get(SHIELD) + value);
    }

    public ChorusTerminatorActivitiesGoal.Action getAction() {
        return this.entityData.get(ACTION);
    }

    public void setAction(ChorusTerminatorActivitiesGoal.Action action) {
        this.entityData.set(ACTION, action);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 800D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.95D)
                .add(Attributes.ARMOR, 50D)
                .add(Attributes.ARMOR_TOUGHNESS, 50D)
                .add(Attributes.FOLLOW_RANGE, 64D)
                .add(ForgeMod.ENTITY_GRAVITY.get(), 0.12D)
                .add(ForgeMod.STEP_HEIGHT_ADDITION.get(), 2D)
                .add(PVZAttributes.PLANT_HURT_RESISTANCE.get(), 0.2D);
    }

    @Override
    protected void registerGoals() {
        this.actionGoal = new ChorusTerminatorActivitiesGoal(this);
        this.goalSelector.addGoal(1, actionGoal);
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, false,
                entity -> EntityUtil.checkCanEntityBeAttack(this, entity)));
    }

    //multipart entity
    @Override
    public boolean isMultipartEntity() {
        return true;
    }

    @Override
    public PartEntity<?>[] getParts() {
        return this.subEntities;
    }

    @Override
    public void setId(int id) {
        super.setId(id);
        for (int i = 0; i < this.subEntities.length; i++) {
            this.subEntities[i].setId(id + i + 1);
        }
    }

    public boolean hurt(ChorusTerminatorPart part, DamageSource source, float damage) {
        if (this.getAction() == ChorusTerminatorActivitiesGoal.Action.DEFENCE) {
            float shield = this.getShield();
            this.addShield(- damage);
            if (this.getShield() <= 0) {
                this.act(ChorusTerminatorActivitiesGoal.Action.DEFENCE_STIFF);
            }
            damage -= shield;
            if (damage < 0) damage = 0;
        }
        int phase = (int) ((this.getMaxHealth() - this.getHealth()) * 3 / this.getMaxHealth());
        if (getPhase() < phase) {
            setPhase(phase);
            this.act(ChorusTerminatorActivitiesGoal.Action.STIFF);
        }
        Vec3 dMovement = this.getDeltaMovement();
        boolean result = super.hurt(source, damage);
        dMovement = this.getDeltaMovement().subtract(dMovement);
        float strength = (float) dMovement.length();
        dMovement = source.getEntity() == null ? dMovement : part.position().subtract(source.getEntity().position()).normalize().multiply(strength, strength, strength);
        part.setDeltaMovement(part.getDeltaMovement().add(dMovement));
        part.hasImpulse = true;
        this.hasImpulse = false;
        return result;
    }

    public boolean hurt(DamageSource damageSource, float damage) {
        return ! this.level.isClientSide && this.hurt(this.body, damageSource, damage);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> data) {
        super.onSyncedDataUpdated(data);
        if (data == ACTION) {
            switch (this.getEntityData().get(ACTION)) {
                case PRE_DASH -> animate(preDashAnimationState);
                case PRE_JUMP -> animate(preJumpAnimationState);
                case STIFF -> animate(stiffAnimationState);
                case SUMMON -> animate(summonAnimationState);
                case SHOOT_SHULKER -> animate(shootShulkerAnimationState);
                case SHOOT_BULLET -> animate(shootBulletsAnimationState);
                case DEFENCE -> animate(defenceAnimationState);
                case DEFENCE_STIFF -> animate(defenceStiffAnimationState);
                case DEFENCE_SUMMON -> animate(defenceSummonAnimationState);
                case DEFENCE_SHOOT -> animate(defenceShootAnimationState);
                case DIE -> animate(dieAnimationState);
                case PRE_DEFENCE -> animate(preDefenceAnimationState);
                default -> animate(idleAnimationState);
            }
        }
    }

    private void animate(AnimationState state) {
        for (AnimationState animationState : animationStates) {
            if (animationState == state) {
                if (! state.isStarted()) animationState.start(tickCount);
            } else {
                animationState.stop();
            }
        }
    }

    public void act(ChorusTerminatorActivitiesGoal.Action action) {
        PVZMod.LOGGER.info("Animating " + action.name() + " from " + this.getAction().name());
        this.lastAction = this.getAction();
        actionTime = 0;
        getEntityData().set(ACTION, action);
    }

    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("shield", this.getShield());
        tag.putInt("phase", this.getPhase());
        tag.putString("action", this.getAction().name());
        CompoundTag parts = new CompoundTag();
        for (ChorusTerminatorPart part : this.subEntities) {
            CompoundTag partTag = new CompoundTag();
            partTag.putInt("cooldown", part.cooldown);
            CompoundTag posTag = new CompoundTag();
            Vec3 pos = part.position();
            posTag.putDouble("x", pos.x);
            posTag.putDouble("y", pos.y);
            posTag.putDouble("z", pos.z);
            partTag.put("position", posTag);
            CompoundTag intentionTag = new CompoundTag();
            pos = part.getIntention();
            intentionTag.putDouble("x", pos.x);
            intentionTag.putDouble("y", pos.y);
            intentionTag.putDouble("z", pos.z);
            partTag.put("intention", intentionTag);
            parts.put(part.name, partTag);
        }
        tag.put("parts", parts);
        ListTag portalList = new ListTag();
        for (Pair<Portal, Portal> pair : this.portals) {
            Portal a = pair.getFirst();
            Portal b = pair.getSecond();
            if (a == null || b == null || ! a.isAlive() || ! b.isAlive()) continue;
            CompoundTag pairTag = new CompoundTag();
            pairTag.putUUID("first", a.getUUID());
            pairTag.putUUID("second", b.getUUID());
            CompoundTag posA = new CompoundTag();
            posA.putDouble("x", a.position().x);
            posA.putDouble("y", a.position().y);
            posA.putDouble("z", a.position().z);
            pairTag.put("firstPos", posA);
            CompoundTag posB = new CompoundTag();
            posB.putDouble("x", b.position().x);
            posB.putDouble("y", b.position().y);
            posB.putDouble("z", b.position().z);
            pairTag.put("secondPos", posB);
            portalList.add(pairTag);
        }
        tag.put("portals", portalList);
    }

    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("shield")) this.setShield(tag.getFloat("shield"));
        if (tag.contains("phase")) this.setPhase(tag.getInt("phase"));
        if (tag.contains("action")) {
            try {
                this.setAction(ChorusTerminatorActivitiesGoal.Action.valueOf(tag.getString("action")));
            } catch (IllegalArgumentException ignored) {
            }
        }
        if (tag.contains("parts")) {
            CompoundTag parts = tag.getCompound("parts");
            for (ChorusTerminatorPart part : this.subEntities) {
                if (!parts.contains(part.name)) continue;
                CompoundTag partTag = parts.getCompound(part.name);
                part.cooldown = partTag.getInt("cooldown");
                if (partTag.contains("position")) {
                    CompoundTag posTag = partTag.getCompound("position");
                    part.setPos(posTag.getDouble("x"), posTag.getDouble("y"), posTag.getDouble("z"));
                }
                if (partTag.contains("intention")) {
                    CompoundTag intentionTag = partTag.getCompound("intention");
                    part.setIntention(new Vec3(intentionTag.getDouble("x"), intentionTag.getDouble("y"), intentionTag.getDouble("z")));
                }
            }
        }
        if (tag.contains("portals")) {
            this.portals.clear();
            this.pendingPortals.clear();
            ListTag portalList = tag.getList("portals", Tag.TAG_COMPOUND);
            for (int i = 0; i < portalList.size(); i++) {
                CompoundTag pairTag = portalList.getCompound(i);
                if (pairTag.hasUUID("first") && pairTag.hasUUID("second")) {
                    this.pendingPortals.add(new Pair<>(pairTag.getUUID("first"), pairTag.getUUID("second")));
                }
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (homePos == null) homePos = this.blockPosition();
        this.actionTime ++;
        Arrays.stream(this.getParts()).forEach(Entity::tick);
        if (! this.level.isClientSide) {
            if (this.tickCount % 2 == 0) ChorusTerminatorSyncPacket.sync(this);
            Vec3 center = Vec3.ZERO;
            for (ChorusTerminatorPart part : this.legs) {
                center = center.add(part.position());
            }
            float l = 1f / (this.legs.length);
            center = center.multiply(l, l, l);
            this.setPos(center.add(0, 0.01, 0));
            if (getAction() == ChorusTerminatorActivitiesGoal.Action.DEFENCE) {
                this.bossEvent.setProgress(this.getShield() / MAX_SHIELD);
                this.bossEvent.setColor(BossEvent.BossBarColor.PINK);
                this.bossEvent.setOverlay(BossEvent.BossBarOverlay.PROGRESS);
            } else {
                this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
                this.bossEvent.setColor(BossEvent.BossBarColor.PURPLE);
                this.bossEvent.setOverlay(BossEvent.BossBarOverlay.NOTCHED_6);
            }
        }
        this.setDeltaMovement(this.body.getDeltaMovement());
        if (this.getLevel() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.COMPOSTER
                    , this.homePos.getX() + .5, this.homePos.getY() + .5, this.homePos.getZ() + .5, 1
                    , 0, 0, 0, 0);
            AABB aabb = new AABB(homePos.getX() - 22, homePos.getY() - 3, homePos.getZ() - 22
                    , homePos.getX() + 22, homePos.getY() + 15, homePos.getZ() + 22);
            serverLevel.getEntitiesOfClass(ItemEntity.class, aabb, e ->
                            (e.getItem().is(Items.CHORUS_FLOWER) || e.getItem().is(Items.CHORUS_FRUIT)) && ! e.hasPickUpDelay())
                    .forEach(i -> {
                i.discard();
                this.addShield(10);
            });
            if (this.portals.isEmpty() && this.pendingPortals.isEmpty()) {
                resetPortals();
            } else if (! pendingPortals.isEmpty()) {
                Iterator<Pair<UUID, UUID>> it = pendingPortals.iterator();
                while (it.hasNext()) {
                    Pair<UUID, UUID> pair = it.next();
                    Entity first = serverLevel.getEntity(pair.getFirst());
                    Entity second = serverLevel.getEntity(pair.getSecond());
                    if (first instanceof Portal portalA && second instanceof Portal portalB
                            && portalA.isAlive() && portalB.isAlive()) {
                        this.portals.add(new Pair<>(portalA, portalB));
                        it.remove();
                    } else if (first != null || second != null) {
                        if (first instanceof Portal p) p.discard();
                        if (second instanceof Portal p) p.discard();
                        it.remove();
                    }
                }
            }
            if (this.tickCount % 20 == 0) addShield(2);
        }
    }

    @Override
    public void die(DamageSource source) {
        this.bossEvent.removeAllPlayers();
        this.removePortals();
        super.die(source);
    }
    @Override
    public void remove(Entity.RemovalReason reason) {
        this.removePortals();
        super.remove(reason);
    }

    private void resetPortals() {
        portals.add(Portal.createPair(level, Vec3.atBottomCenterOf(this.homePos.offset(0, 0, -21))
                , Vec3.atBottomCenterOf(this.homePos.offset(0, 0, 21))));
        portals.add(Portal.createPair(level, Vec3.atBottomCenterOf(this.homePos.offset(21, 0, 0))
                , Vec3.atBottomCenterOf(this.homePos.offset(-21, 0, 0))));
        portals.add(Portal.createPair(level, Vec3.atBottomCenterOf(this.homePos.offset(21, 0, -19))
                , Vec3.atBottomCenterOf(this.homePos.offset(-21, 0, -19))));
        portals.add(Portal.createPair(level, Vec3.atBottomCenterOf(this.homePos.offset(21, 0, 19))
                , Vec3.atBottomCenterOf(this.homePos.offset(-21, 0, 19))));
        portals.add(Portal.createPair(level, Vec3.atBottomCenterOf(this.homePos.offset(19, 0, 21))
                , Vec3.atBottomCenterOf(this.homePos.offset(19, 0, -21))));
        portals.add(Portal.createPair(level, Vec3.atBottomCenterOf(this.homePos.offset(-19, 0, 21))
                , Vec3.atBottomCenterOf(this.homePos.offset(-19, 0, -21))));
    }

    public void removePortals() {
        for (Pair<Portal, Portal> pair : this.portals) {
            if (pair.getFirst() != null && pair.getFirst().isAlive()) pair.getFirst().setState(2);
            if (pair.getSecond() != null && pair.getSecond().isAlive()) pair.getSecond().setState(2);
        }
        this.portals.clear();
        this.pendingPortals.clear();
    }

    @Override
    public boolean isOnGround() {
        return Arrays.stream(this.legs).anyMatch(Predicate.not(EntityUtil::isLeavingGround));
    }

    @Override
    public void setCustomName(@Nullable Component name) {
        super.setCustomName(name);
        this.bossEvent.setName(this.getDisplayName());
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    public Player getLastHurtByPlayer() {
        return this.lastHurtByPlayer;
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean canRide(Entity entity) {
        return false;
    }

    public Vec3 getNearestStandablePos(Vec3 intension, Vec3 center) {
        final double cx = center.x, cy = center.y, cz = center.z;
        final double ix = intension.x, iy = intension.y, iz = intension.z;

        final int yMin = (int) Math.floor(cy - 5) + 1;
        final int yMax = (int) Math.ceil(cy) - 1;
        final int xMin = (int) Math.max(Math.ceil(ix - 4), Math.ceil(cx - 4));
        final int xMax = (int) Math.min(Math.ceil(ix + 4), Math.floor(cx + 4));
        final int zMin = (int) Math.max(Math.ceil(iz - 4), Math.ceil(cz - 4));
        final int zMax = (int) Math.min(Math.ceil(iz + 4), Math.floor(cz + 4));
        List<Pair<Double, BlockPos>> candidates = new ArrayList<>(1024);

        for (int y = yMin; y <= yMax; y++) {
            final double dy = y - cy;
            final double remSqr = 36 - dy * dy;
            for (int x = xMin; x <= xMax; x++) {
                final double dx = x - cx;
                final double dxSqr = dx * dx;
                if (dxSqr >= remSqr) continue;
                for (int z = zMin; z <= zMax; z++) {
                    final double dz = z - cz;
                    final double horiSqr = dxSqr + dz * dz;
                    if (horiSqr <= 8) continue;
                    final double ex = x - ix, ey = y - iy, ez = z - iz;
                    candidates.add(new Pair<>((ex * ex + ez * ez) * 8 - ey, new BlockPos(x, y, z)));
                }
            }
        }
        candidates.sort(Comparator.comparingDouble(Pair::getFirst));

        BlockPos blockedPos = new BlockPos(0, -64, 0);
        for (Pair<Double, BlockPos> p : candidates) {
            BlockPos pos = p.getSecond();
            BlockState blockState = level.getBlockState(pos);
            if (level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()) continue;
            boolean upperBlocked = blockedPos.getX() == pos.getX() && blockedPos.getZ() == pos.getZ() && blockedPos.getY() - pos.getY() > 0;
            if (upperBlocked) continue;
            for (int i = 1; i < 4; i ++) {
                if (! level.getBlockState(pos.offset(0, i, 0)).getCollisionShape(level, pos.offset(0, i, 0)).isEmpty()) {
                    upperBlocked = true;
                    break;
                }
            }
            blockedPos = pos;
            if (upperBlocked) continue;
            double maxY = Math.max(0, blockState.getCollisionShape(level, pos).max(Direction.Axis.Y));
            if (maxY <= 0) continue;
            return Vec3.atBottomCenterOf(pos).add(0, maxY, 0);
        }
        return intension;
    }

    @Override
    public PathNavigation getNavigation() {
        return super.getNavigation();
    }

    public static class ChorusTerminatorPathNavigation extends GroundPathNavigation {

        public ChorusTerminatorPathNavigation(Mob p_26448_, Level p_26449_) {
            super(p_26448_, p_26449_);
        }

        @Override
        protected PathFinder createPathFinder(int maxVisitedNodes) {
            this.nodeEvaluator = new ChorusTerminatorNodeEvaluator();
            this.nodeEvaluator.setCanPassDoors(true);
            return new PathFinder(this.nodeEvaluator, maxVisitedNodes);
        }

        public void tick() {
            super.tick();
            if (this.path == null || this.path.isDone()) return;
            if (Vec3.atCenterOf(this.path.getNextNode().asBlockPos()).multiply(1, 0, 1)
                    .distanceToSqr(getTempMobPos().multiply(1, 0, 1)) < 8) this.path.advance();
        }
    }

    public static class ChorusTerminatorNodeEvaluator extends WalkNodeEvaluator {

        public BlockPathTypes getBlockPathType(BlockGetter level, int x, int y, int z) {
            BlockPathTypes types = super.getBlockPathType(level, x, y, z);
            if (types == BlockPathTypes.UNPASSABLE_RAIL) {
                types = BlockPathTypes.OPEN;
            } else if (types == BlockPathTypes.FENCE) {
                types = BlockPathTypes.OPEN;
            } else if (types == BlockPathTypes.BLOCKED) {
                Block block = level.getBlockState(new  BlockPos(x, y, z)).getBlock();
                if (block == Blocks.CHORUS_FLOWER || block == Blocks.CHORUS_PLANT) {
                    types = BlockPathTypes.OPEN;
                }
            }
            return types;
        }
    }

    public static class ChorusTerminatorMoveControl extends MoveControl {
        public final ChorusTerminatorBoss mob;

        public ChorusTerminatorMoveControl(ChorusTerminatorBoss mob) {
            super(mob);
            this.mob = mob;
        }

        public void tick() {
            Vec3 mobPos = new Vec3(wantedX, wantedY, wantedZ);
            ((ServerLevel) this.mob.level).sendParticles(ParticleTypes.FLAME
                    , wantedX, wantedY + 0.3, wantedZ,
                    1, 0, 0, 0, 0);
            Path path = mob.navigation.path;
            if (path != null) {
                for (int i = path.getNextNodeIndex(); i < path.getNodeCount(); i ++) {
                    Node node = path.getNode(i);
                    ((ServerLevel) this.mob.level).sendParticles(ParticleTypes.SOUL
                            , node.x + 0.5, node.y + 1.3, node.z + 0.5,
                            1, 0, 0, 0, 0);
                }
            }
            if (this.operation == Operation.STRAFE) {
                this.mob.setZza(0);
                this.mob.setXxa(0);
                this.mob.setYya(0);
            } else if (this.operation == Operation.MOVE_TO) {
                this.operation = Operation.WAIT;
                double dx = this.wantedX - this.mob.getX();
                double dz = this.wantedZ - this.mob.getZ();
                float f9 = (float)(Mth.atan2(dz, dx) * (double)(180F / (float)Math.PI)) - 90.0F;
                this.mob.setYRot(this.rotlerp(this.mob.getYRot(), f9, 90.0F));
                int stepLen = 5;
                int steps = 2;
                if (this.mob.getAction() == ChorusTerminatorActivitiesGoal.Action.DASH
                        || this.mob.getAction() == ChorusTerminatorActivitiesGoal.Action.PRE_DASH) {
                    if (this.mob.tickCount % stepLen == 0 || this.mob.tickCount % stepLen == 2) {
                        for (int leg = 0; leg < this.mob.legs.length; leg ++) {
                            if (this.mob.tickCount % (steps * stepLen) / stepLen != leg % steps || leg / 2 == this.mob.tickCount % stepLen / 2) continue;
                            Vec3 looking = this.mob.eye.position().subtract(this.mob.body.position());
                            double angle = Math.IEEEremainder(Math.atan2(-looking.x, looking.z), Math.PI / 2)
                                    + Math.PI / 2 * (leg - 0.5);
                            Vec2 offset = new Vec2((float) (- 2.5 * Math.sin(angle)), (float) (2.5 * Math.cos(angle)));
                            this.mob.legs[leg].setIntention(this.mob.getNearestStandablePos(
                                    new Vec3(wantedX + offset.x, wantedY, wantedZ + offset.y), mobPos));
                        }
                    }
                } else {
                    stepLen = 8;
                    steps = 4;
                    if (this.mob.tickCount % stepLen == 0) {
                        for (int leg = 0; leg < this.mob.legs.length; leg ++) {
                            if (this.mob.tickCount % (steps * stepLen) / stepLen != leg % steps) continue;
                            Vec3 looking = this.mob.eye.position().subtract(this.mob.body.position());
                            double angle = Math.IEEEremainder(Math.atan2(-looking.x, looking.z), Math.PI / 2)
                                    + Math.PI / 2 * (leg - 0.5);
                            Vec2 offset = new Vec2((float) (- 2.5 * Math.sin(angle)), (float) (2.5 * Math.cos(angle)));
                            this.mob.legs[leg].setIntention(this.mob.getNearestStandablePos(
                                    new Vec3(wantedX + offset.x, wantedY, wantedZ + offset.y), mobPos));
                        }
                    }
                }
            } else if (this.operation == Operation.JUMPING) {
                if (this.mob.getAction() != ChorusTerminatorActivitiesGoal.Action.PRE_JUMP && this.mob.getAction() != ChorusTerminatorActivitiesGoal.Action.JUMP) {
                    this.operation = Operation.WAIT;
                }
            } else if (this.operation == Operation.WAIT) {
                boolean stiff = this.mob.getAction() == ChorusTerminatorActivitiesGoal.Action.STIFF
                        || this.mob.getAction() == ChorusTerminatorActivitiesGoal.Action.DEFENCE_STIFF;
                boolean defending = this.mob.getAction() == ChorusTerminatorActivitiesGoal.Action.DEFENCE
                        || this.mob.getAction() == ChorusTerminatorActivitiesGoal.Action.DEFENCE_SHOOT
                        || this.mob.getAction() == ChorusTerminatorActivitiesGoal.Action.DEFENCE_SUMMON;
                if (! stiff && this.mob.tickCount % 10 == 0) {
                    int leg = this.mob.tickCount % 40 / 10;
                    float dist = defending ? 2f : 2.5f;
                    Vec2 offset = new Vec2((leg == 0 || leg == 1) ? -dist : dist, (leg == 1 || leg == 2) ? -dist : dist);
                    mob.legs[leg].setIntention(this.mob.getNearestStandablePos(
                            new Vec3(mob.position().x + offset.x, mob.position().y, mob.position().z + offset.y), mobPos));
                }
            }
        }

        public void jumpTo(Vec3 position) {
            for (int leg = 0; leg < 4; leg ++) {
                Vec2 offset = new Vec2((leg == 0 || leg == 1) ? -2.5f : 2.5f, (leg == 1 || leg == 2) ? -2.5f : 2.5f);
                mob.legs[leg].setIntention(this.mob.getNearestStandablePos(
                        new Vec3(position.x + offset.x, position.y, position.z + offset.y), position));
            }
            this.operation = Operation.JUMPING;
        }
    }
}
