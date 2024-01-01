package com.hungteen.pvz.common.entity.plants.base;

import com.hungteen.pvz.common.entity.PVZPlant;
import com.hungteen.pvz.common.entity.Sun;
import com.hungteen.pvz.util.EntityUtil;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;

public abstract class PlantProducerEntity extends PVZPlant {


	public PlantProducerEntity(EntityType< ? extends Mob > type, Level worldIn){
            super(type, worldIn);
            this.setAttackTime(this,200);//the first gen just need 10 seconds CD.
        }

        @Override
        protected void registerGoals () {
            super.registerGoals();
            this.goalSelector.addGoal(0, new ProducerGenGoal(this));
        }


        /**
         * sun produce plant gen sun
         * such as sunflower or sunshroom
         */
        protected void genSun ( int num, int cnt){
            Sun.spawnSunsRandomlyByAmount(level, this.blockPosition(), num, num / cnt, 2);
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
        public abstract int getGenCD ();

        public int getAnimGenCD () {
            return 20;
        }


        /**
         * is producer going to gen, use for render sunflower sun layer.
         */
        public boolean isPlantInGen () {
            return this.getAttackTime(this) <= 10 ;
        }

        static class ProducerGenGoal extends Goal {

            private final PlantProducerEntity producer;

            public ProducerGenGoal(PlantProducerEntity entity) {
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
                final int time = this.producer.getAttackTime(this);
                if (time <= 1) {
                    this.producer.genSomething();
                    this.producer.setAttackTime(this,this.producer.getGenCD());
                } else {
                    this.producer.setAttackTime(this,Math.max(0, time - 1));
                }
            }
        }

}