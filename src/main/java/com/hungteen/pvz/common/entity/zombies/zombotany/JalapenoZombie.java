package com.hungteen.pvz.common.entity.zombies.zombotany;

import com.hungteen.pvz.common.capability.entity.PVZEntityCapability;
import com.hungteen.pvz.common.entity.creatures.Anger;
import com.hungteen.pvz.common.entity.zombies.PVZZombie;
import com.hungteen.pvz.common.register.PVZEntities;
import com.hungteen.pvz.util.EntityUtil;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.List;

import static com.hungteen.pvz.common.register.PVZDamageSource.teamFilter;
import static com.hungteen.pvz.common.register.PVZDamageSource.transferKiller;

/**
 * 火爆辣椒僵尸 - 接近玩家时会爆炸，向四个方向发射怒妖
 * @see com.hungteen.pvz.common.entity.plants.Jalapeno Jalapeno
 */
public class JalapenoZombie extends PVZZombie implements IZombotany {

    protected static final EntityDataAccessor<Boolean> IS_EXPLODING = SynchedEntityData.defineId(JalapenoZombie.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> EXPLODE_TIME = SynchedEntityData.defineId(JalapenoZombie.class, EntityDataSerializers.INT);
    
    public final AnimationState explodeAnimationState = new AnimationState();

    public JalapenoZombie(EntityType<? extends Zombie> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(EXPLODE_TIME, 0);
        this.entityData.define(IS_EXPLODING, false);
    }
    public static AttributeSupplier.@NotNull Builder createAttributes() {
        return Zombie.createAttributes()
                .add(Attributes.ATTACK_DAMAGE, 2D);
    }
    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> p_219422_) {
        if (IS_EXPLODING.equals(p_219422_)) {
            if (entityData.get(IS_EXPLODING)) {
                this.explodeAnimationState.start(this.tickCount);
            }
        }
        super.onSyncedDataUpdated(p_219422_);
    }

    public void setupPresentationAnim() {
        this.explodeAnimationState.stop();
    }

    @Override
    public void tick() {
        super.tick();
        
        // 添加粒子效果
        if (this.level.isClientSide()) {
            level.addParticle(ParticleTypes.LAVA, getX(), getY() + 1.5, getZ(),
                    random.nextFloat() * 0.15 - 0.075, random.nextFloat() * 0.15, random.nextFloat() * 0.15 - 0.075);
        }
        
        // 更新爆炸倒计时
        if (getExplodeTime() > 0) {
            setExplodeTime(getExplodeTime() - 1);
            if (getExplodeTime() == 0 && !level.isClientSide) {
                this.explode();
            }
        }
    }
    @Override
    public boolean fireImmune() {
        return true;
    }

    public void explode() {
        level.explode(this, transferKiller(teamFilter(DamageSource.explosion(this).bypassArmor()), PVZEntityCapability.getOwner(this)), null, this.getX(), this.getY() + 1.5, this.getZ(),
                1F, false, Explosion.BlockInteraction.NONE);
        if (!level.isClientSide) {
            for (Direction direction : List.of(Direction.EAST, Direction.WEST, Direction.SOUTH, Direction.NORTH)) {
                Anger anger = new Anger(level);
                anger.setPos(this.position().add(0, 1, 0));
                anger.getCapability(PVZEntityCapability.CAP).ifPresent(cap -> cap.setOwner(this));
                anger.yRot = direction.toYRot();
                level.addFreshEntity(anger);
                anger.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(this.getAttributeValue(Attributes.ATTACK_DAMAGE) * 8);
                anger.targetSelector.disableControlFlag(Goal.Flag.TARGET);
                anger.getAttribute(Attributes.FLYING_SPEED).setBaseValue(1F);
            }
        }
        this.discard();
    }

    public int getExplodeTime() {
        return entityData.get(EXPLODE_TIME);
    }

    public void setExplodeTime(int time) {
        entityData.set(EXPLODE_TIME, time);
    }

    public boolean isExploding() {
        return entityData.get(IS_EXPLODING);
    }

    public void setExploding(boolean exploding) {
        entityData.set(IS_EXPLODING, exploding);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new JalapenoZombieExplodeGoal(this));
    }

    @Override
    public EntityType<?> getPlantType() {
        return PVZEntities.JALAPENO.get();
    }
    @Override
    public Vec3 getPlantHeadOffset() {
        return new Vec3(0, 9, -1);
    }
    public static class JalapenoZombieExplodeGoal extends Goal {
        private final JalapenoZombie zombie;
        
        public JalapenoZombieExplodeGoal(JalapenoZombie zombie) {
            this.zombie = zombie;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }
        
        @Override
        public boolean canUse() {
            LivingEntity target = zombie.getTarget();
            return EntityUtil.isEntityValid(target) && 
                   zombie.distanceToSqr(target) < 36.0 && // 6格距离内
                   zombie.hasLineOfSight(target) && 
                   !zombie.isExploding();
        }
        
        @Override
        public void start() {
            zombie.setExplodeTime(40); // 爆炸倒计时
            zombie.setExploding(true);
            zombie.getNavigation().stop();
            EntityUtil.playSound(zombie, SoundEvents.CREEPER_PRIMED);
        }
        
        @Override
        public boolean canContinueToUse() {
            return zombie.getExplodeTime() > 0;
        }
        
        @Override
        public void tick() {
            LivingEntity target = zombie.getTarget();
            if (EntityUtil.isEntityValid(target)) {
                zombie.getLookControl().setLookAt(target, 30.0F, 30.0F);
            }
        }
    }
} 