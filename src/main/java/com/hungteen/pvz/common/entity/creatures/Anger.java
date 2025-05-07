package com.hungteen.pvz.common.entity.creatures;

import com.hungteen.pvz.api.events.TeammateTestingEvent;
import com.hungteen.pvz.common.entity.plants.base.ShooterPlant;
import com.hungteen.pvz.common.entity.zombies.TacoImp;
import com.hungteen.pvz.common.register.PVZEntities;
import com.hungteen.pvz.util.EntityUtil;
import com.hungteen.pvz.util.Util;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;


public class Anger extends FlyingMob {
    public int maxLife = 20;
    public boolean friendlyFire = true;
    public Anger(EntityType<? extends FlyingMob> p_218310_, Level p_218311_) {
        super(p_218310_, p_218311_);
    }
    public Anger(Level p_218311_) {
        this(PVZEntities.ANGER.get(), p_218311_);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 2.0D)
                .add(Attributes.FLYING_SPEED, 0.6F)
                .add(Attributes.ATTACK_DAMAGE, 1D);
    }
    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new AngerLittingGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, LivingEntity.class,
                true, (entity) -> EntityUtil.checkCanEntityBeAttack(this, entity)));
    }

    @SubscribeEvent
    public static void onPlantCheckTeammate(TeammateTestingEvent event) {
        //won't be regarded as target by shooters/pults.
        if (event.A instanceof Anger || event.B instanceof Anger) {
            Entity other = event.A instanceof Anger ? event.B : event.A;
            if (! event.currentResult) {
                event.currentResult = other instanceof ShooterPlant;
            }
        }
    }
    public void tick() {
        this.noPhysics = true;
        super.tick();
        this.noPhysics = false;
        if (level.isClientSide) {
            Vec3 movement = this.getDeltaMovement();
            level.addParticle(ParticleTypes.LAVA,
                    getX(), getY() + 0.2, getZ(),
                    - movement.x * 0.25 + random.nextFloat() * 0.15 - 0.075,
                    - movement.y * 0.25 + random.nextFloat() * 0.15,
                    - movement.z * 0.25 + random.nextFloat() * 0.15 - 0.075);

            int i = 0;
            while (i < 3) {
                i ++;
                level.addParticle(ParticleTypes.FLAME,
                        getX(), getY() + 0.2, getZ(),
                        - movement.x * 0.25 + random.nextFloat() * 0.15 - 0.075,
                        - movement.y * 0.25 + random.nextFloat() * 0.15,
                        - movement.z * 0.25 + random.nextFloat() * 0.15 - 0.075);
            }
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Life", this.maxLife);
        tag.putBoolean("FriendlyFire", this.friendlyFire);
    }
    @Override
    public void readAdditionalSaveData(CompoundTag tag){
        super.readAdditionalSaveData(tag);
        if (tag.contains("Life")) {
            maxLife =tag.getInt("Life");
        }
        if (tag.contains("PreciseStrike")) {
            friendlyFire =tag.getBoolean("FriendlyFire");
        }
    }

    protected void doPush(Entity entity) {
    }
    @Override
    public boolean fireImmune() {
        return true;
    }

    public class AngerLittingGoal extends Goal {
        private final Anger anger;
        public AngerLittingGoal(Anger anger) {
            this.anger = anger;
        }

        @Override
        public boolean canUse() {
            return true;
        }

        @Override
        public void tick() {
            LivingEntity target = anger.getTarget();
            if (target != null) {
                anger.lookAt(target, 10.0F, 10.0F);
                anger.setDeltaMovement(anger.getDeltaMovement().add(0, Math.signum(target.getY() - anger.getY() + 0.6F) * Math.max(Math.abs(target.getY() - anger.getY() + 0.6F) / 40F, 0.1), 0));
            }
            anger.setDeltaMovement(anger.getLookAngle().normalize().scale(2).add(anger.getDeltaMovement().normalize()).normalize().scale(anger.getAttributeValue(Attributes.FLYING_SPEED)));
            if (anger.tickCount > anger.maxLife || level.getBlockState(this.anger.blockPosition()).isSuffocating(level, anger.blockPosition())) {
                anger.discard();
            }
            if (anger.tickCount % 3 <= 1) {
                List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, anger.getBoundingBox().inflate(1.3, 0.2, 1.3).move(0, -0.4, 0));
                entities.forEach((entity) -> {
                    if (Util.hasBlockBetween(level, entity.position(), anger.position())) {
                        return;
                    }
                    if (! EntityUtil.isTeammate(anger, entity)) {
                        entity.hurt(DamageSource.ON_FIRE, (float) anger.getAttribute(Attributes.ATTACK_DAMAGE).getValue());
                        entity.setSecondsOnFire(6);
                    } else if (friendlyFire) {
                        entity.setSecondsOnFire(6);
                    }
                });
                if (net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(anger.level, anger)) {
                    for (int x = -2; x < 3; x ++) {
                        for (int z = -2; z < 3; z ++) {
                            for (int y = -2; y < 3; y ++) {
                                int dist = Math.abs(x) + Math.abs(y) + Math.abs(z) + 1;
                                if (dist < 6 && anger.random.nextInt(dist) == 0 && level.getBlockState(this.anger.blockPosition().offset(x, y, z)).is(BlockTags.SNOW)) {
                                    level.setBlock(this.anger.blockPosition().offset(x, y, z), Blocks.AIR.defaultBlockState(), 3);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
