package com.hungteen.pvz.common.entity.zombies;

import com.hungteen.pvz.api.interfaces.IPlant;
import com.hungteen.pvz.common.network.ChorusTerminatorSyncPacket;
import com.hungteen.pvz.common.register.PVZAttributes;
import com.hungteen.pvz.util.EntityUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.entity.PartEntity;

import javax.annotation.Nullable;
import java.util.Arrays;

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

    public AnimationState idleAnimationState = new AnimationState();

    private static final EntityDataAccessor<Boolean> PHASE_2 = SynchedEntityData.defineId(ChorusTerminatorBoss.class, EntityDataSerializers.BOOLEAN);
    private final ServerBossEvent bossEvent = (ServerBossEvent) (new ServerBossEvent(this.getDisplayName(), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS)).setDarkenScreen(false);

    public ChorusTerminatorBoss(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.eye = new ChorusTerminatorPart(this, ChorusTerminatorPart.Type.EYE, "eye", 1F, 1F, false);
        this.body = new ChorusTerminatorPart(this, ChorusTerminatorPart.Type.BODY, "body", 3.5F, 4.5F, false);
        this.mouth = new ChorusTerminatorPart(this, ChorusTerminatorPart.Type.MOUTH, "mouth", 3F, 1F, false);
        this.legLeftFront = new ChorusTerminatorPart(this, ChorusTerminatorPart.Type.LEG, "leg_left_front", 1F, 2.5F, true);
        this.legRightFront = new ChorusTerminatorPart(this, ChorusTerminatorPart.Type.LEG, "leg_right_front", 1F, 2.5F, true);
        this.legLeftBack = new ChorusTerminatorPart(this, ChorusTerminatorPart.Type.LEG, "leg_left_back", 1F, 2.5F, true);
        this.legRightBack = new ChorusTerminatorPart(this, ChorusTerminatorPart.Type.LEG, "leg_right_back", 1F, 2.5F, true);
        this.subEntities = new ChorusTerminatorPart[]{this.eye, this.body, this.mouth, this.legLeftFront, this.legRightFront, this.legLeftBack, this.legRightBack};
        this.legs = new ChorusTerminatorPart[]{this.legLeftFront, this.legRightFront, this.legLeftBack, this.legRightBack};
        Arrays.stream(this.subEntities).forEach(p -> p.setPos(this.position().add(0, 2, 0)));
        this.noPhysics = true;
        this.noCulling = true;
        this.setNoGravity(true);

        this.setId(ENTITY_COUNTER.getAndAdd(this.subEntities.length + 1) + 1);
        this.idleAnimationState.start(this.tickCount);
    }

    @Override
    public void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(PHASE_2, false);
    }

    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();
        this.legLeftFront.setPos(this.position().add(-4.8, 0, 2.5));
        this.legLeftBack.setPos(this.position().add(-2.5, 0, -2.5));
        this.legRightFront.setPos(this.position().add(2, 1, 2.5));
        this.legRightBack.setPos(this.position().add(2.5, 0, -2.5));
        this.body.setPos(this.position().add(0, 3, 0));
        this.eye.setPos(body.position().add(0, 1.1, 1.75));
        this.mouth.setPos(body.position());
    }

    public void setPhase2(boolean on) {
        this.entityData.set(PHASE_2, on);
    }

    public boolean isPhase2() {
        return entityData.get(PHASE_2);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 800D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.95D)
                .add(Attributes.ARMOR, 20D)
                .add(Attributes.ARMOR_TOUGHNESS, 20D)
                .add(Attributes.FOLLOW_RANGE, 40D)
                .add(ForgeMod.ENTITY_GRAVITY.get(), 0.04D)
                .add(PVZAttributes.PLANT_HURT_RESISTANCE.get(), 0.5D);
    }

    @Override
    protected void registerGoals() {
//        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0D, false));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 32.0F));
//        this.goalSelector.addGoal(3, new RandomStrollGoal(this, 0.8D));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, false,
                entity -> EntityUtil.checkCanEntityBeAttack(this, entity)));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Mob.class, true,
                entity -> entity instanceof IPlant && EntityUtil.checkCanEntityBeAttack(this, entity)));
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
        Vec3 dMovement = this.getDeltaMovement();
        boolean result = super.hurt(source, damage);
        dMovement = this.getDeltaMovement().subtract(dMovement);
        float strength = (float) dMovement.distanceTo(Vec3.ZERO);
        dMovement = source.getEntity() == null ? dMovement : part.position().subtract(source.getEntity().position()).normalize().multiply(strength, strength, strength);
        part.setDeltaMovement(part.getDeltaMovement().add(dMovement));
        part.hasImpulse = true;
        this.setDeltaMovement(dMovement);
        return result;
    }

    public boolean hurt(DamageSource damageSource, float damage) {
        return ! this.level.isClientSide && this.hurt(this.body, damageSource, damage);
    }

    @Override
    public void tick() {
        super.tick();
        this.body.setDeltaMovement(this.getDeltaMovement());
        Arrays.stream(this.getParts()).forEach(Entity::tick);
        if (! this.level.isClientSide) {
            if (this.tickCount % 2 == 0) ChorusTerminatorSyncPacket.sync(this);
            Vec3 center = Vec3.ZERO;
            for (ChorusTerminatorPart part : this.legs) {
                center = center.add(part.position());
            }
            float l = 1f / (this.legs.length);
            center = center.multiply(l, l, l);
            this.setPos(center);
            this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
        }
        this.setDeltaMovement(this.body.getDeltaMovement());
    }

    private void t(ChorusTerminatorPart part) {
    }

    @Override
    public void die(DamageSource source) {
        this.bossEvent.removeAllPlayers();
        super.die(source);
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

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }
}
