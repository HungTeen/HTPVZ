package com.hungteen.pvz.common.entity.plants;

import com.hungteen.pvz.api.Skill;
import com.hungteen.pvz.api.interfaces.IMushroom;
import com.hungteen.pvz.common.entity.SimplePlant;
import com.hungteen.pvz.common.entity.ai.goal.AttractEnemyGoal;
import com.hungteen.pvz.common.entity.plants.base.ShooterPlant;
import com.hungteen.pvz.common.network.ClientProxy;
import com.hungteen.pvz.common.register.PVZDamageSource;
import com.hungteen.pvz.common.register.PVZMobEffects;
import com.hungteen.pvz.util.EntityUtil;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Set;

public class HypnoShroom extends SimplePlant implements IMushroom {
    public static final EntityDataAccessor<Boolean> IS_SLEEPING = SynchedEntityData.defineId(HypnoShroom.class, EntityDataSerializers.BOOLEAN);

    public AnimationState idleAnimationState = new AnimationState();
    public AnimationState sleepAnimationState = new AnimationState();
    public static List<Skill> staticSkillList = List.of(
    );

    public HypnoShroom(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);
    }


    //entity settings
    public static AttributeSupplier.Builder createAttributes() {
        return ShooterPlant.createAttributes()
                .add(Attributes.MAX_HEALTH, 2D)
                .add(Attributes.FOLLOW_RANGE, 2D);
    }
    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new AttractEnemyGoal(this));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(IS_SLEEPING, false);
    }
    @Override
    public List<Skill> getStaticSkillList(){
        return staticSkillList;
    }

    public void tick() {
        super.tick();
        if (this.isSleeping()) {
            if (! this.sleepAnimationState.isStarted()) {
                this.sleepAnimationState.start(this.tickCount);
                this.idleAnimationState.stop();
            }
        } else {
            if (! this.idleAnimationState.isStarted()) {
                this.idleAnimationState.start(this.tickCount);
                this.sleepAnimationState.stop();
            }
        }
    }
    @Override
    public Set<TagKey<Block>> getAcceptableTags() {
        return getMushroomAcceptableTags();
    }
    @Override
    public void die(DamageSource damageSource) {
        Entity entity = damageSource.getEntity();
        if (entity instanceof LivingEntity target && damageSource.getDirectEntity() == entity) {
            if (! this.isSleeping() && ! EntityUtil.isTeammate(this, entity) && PVZDamageSource.isEating(damageSource) && this.distanceToSqr(entity) < 2 && ! (entity instanceof Player)) {
                PVZMobEffects.hypnotizeWithTeam(target, this, 2000);
                this.setPose(Pose.USING_TONGUE);//to let client identify.
                this.discard();
            }
        }
        super.die(damageSource);
    }

    @Override
    public void onClientRemoval() {
        if (this.getPose() == Pose.USING_TONGUE) {
            for (int i = 0; i < 60; i ++) {
                Vec3 pos = this.position();
                Particle particle = ClientProxy.MC.levelRenderer.addParticleInternal(ParticleTypes.ENTITY_EFFECT.getType(), false,
                        pos.x + (random.nextFloat() - 0.5) * 3, pos.y + random.nextFloat() * 1.5, pos.z + (random.nextFloat() - 0.5) * 3,
                        0, 0, 0);
                if (particle != null) {
                    int color = PotionUtils.getColor(List.of(new MobEffectInstance(PVZMobEffects.HYPNOTISED.get())));
                    float r = (float)(color >> 16 & 255) / 255.0F;
                    float g = (float)(color >> 8 & 255) / 255.0F;
                    float b = (float)(color & 255) / 255.0F;
                    particle.setColor(r, g, b);
                }
            }
        }
        super.onClientRemoval();
    }

    @Override
    public void startSleeping(BlockPos p_21141_) {
        this.entityData.set(IS_SLEEPING, true);
    }
    @Override
    public void stopSleeping() {
        if (this.shouldWakeUp()) { //avoid situations waken by enemy hurt.
            this.entityData.set(IS_SLEEPING, false);
        }
    }
    @Override
    public boolean isSleeping() {
        return this.entityData.get(IS_SLEEPING);
    }

    /**Overriding {@link LivingEntity#checkBedExists() checkBedExists()} to avoid mushroom from awaking in tick().*/
    @Override
    public boolean checkBedExists() {
        return true;
    }

    @Override
    public void fallAsleep() {
        this.startSleeping(this.getRootBlockPos());
    }

    @Override
    public void wakeUp() {
        this.stopSleeping();
    }
}
