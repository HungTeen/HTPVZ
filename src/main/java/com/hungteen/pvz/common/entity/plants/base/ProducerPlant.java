package com.hungteen.pvz.common.entity.plants.base;

import com.hungteen.pvz.common.entity.SimplePlant;
import com.hungteen.pvz.common.entity.Sun;
import com.hungteen.pvz.common.tags.PVZBiomeTags;
import com.hungteen.pvz.util.EntityUtil;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public abstract class ProducerPlant extends SimplePlant {
    public AnimationState idleAnimationState = new AnimationState();
    public AnimationState produceAnimationState = new AnimationState();
    protected static final EntityDataAccessor<Boolean> POSE = SynchedEntityData.defineId(ProducerPlant.class, EntityDataSerializers.BOOLEAN);


	public ProducerPlant(EntityType< ? extends Mob > type, Level worldIn){
        super(type, worldIn);
        this.setAttackTime(120);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new ProducerGenGoal(this));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
    }


    //animate realted.
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(POSE, false);
    }
    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> p_219422_) {
        if (POSE.equals(p_219422_)) {
            if (entityData.get(POSE)) {
                this.idleAnimationState.stop();
                this.produceAnimationState.start(this.tickCount);
            } else {
                this.produceAnimationState.stop();
                this.idleAnimationState.start(this.tickCount);
            }
        }

        super.onSyncedDataUpdated(p_219422_);
    }

    public void setupPresentationAnim() {
        this.idleAnimationState.start(this.tickCount);
    }

    //sun produce related.
    /**
     * sun produce plant gen sun
     * such as sunflower or sunshroom
     */
    protected void genSun(int num, int cnt) {
        Sun.spawnSunsWithEffectsByAmount(level, this.blockPosition(), num, num / cnt, 0.25F);
        EntityUtil.playSound(this, SoundEvents.EXPERIENCE_ORB_PICKUP);
    }

    /**
     * produce something ,like sunflower produce sun.
     * {@link ProducerGenGoal#tick()}
     */
    protected abstract void genSomething();


    /**
     * get next produce CD.
     * {@link ProducerGenGoal#tick()}
     */
    public abstract int getGenCD();


    /**
     * is producer going to gen, use for render sunflower sun layer.
     */
    public boolean isPlantInGen() {
        return this.getAttackTime() <= 10 ;
    }

    static class ProducerGenGoal extends Goal {

        private final ProducerPlant producer;

        public ProducerGenGoal(ProducerPlant entity) {
            this.producer = entity;
        }

        @Override
        public boolean canUse() {
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            return true;
        }

        @Override
        public void stop() {
        }

        @Override
        public void tick() {
            if (!this.producer.isEffectiveAi()) {
                return;
            }
            this.producer.setAttackTime(Math.min(this.producer.getGenCD(), this.producer.getAttackTime()));
            final int time = this.producer.getAttackTime();
            if (time <= 1) {
                this.producer.genSomething();
                this.producer.setAttackTime(this.producer.getGenCD());
            } else if (! producer.level.getBiome(producer.blockPosition()).is(PVZBiomeTags.UNABLE_SUN_PRODUCTION)){
                this.producer.setAttackTime(Math.max(0, time - 1));
            }
            producer.entityData.set(POSE, this.producer.getGenCD() - time < 10 || time < 10);
        }
    }
}