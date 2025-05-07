package com.hungteen.pvz.common.entity.zombies;

import com.hungteen.pvz.common.register.PVZItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

import javax.annotation.Nullable;

public class JackInABoxZombie extends PVZZombie {
    public JackInABoxZombie(EntityType<? extends Zombie> p_34271_, Level p_34272_) {
        super(p_34271_, p_34272_);
    }
    @Override
    protected void addBehaviourGoals() {
        super.addBehaviourGoals();
        this.goalSelector.addGoal(1, new JackInABoxZombieUseItemGoal(this));
    }
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData data, @Nullable CompoundTag tag) {
        SpawnGroupData spawnGroupData = super.finalizeSpawn(level, difficulty, spawnType, data, tag);
        this.setItemInHand(InteractionHand.MAIN_HAND, PVZItems.JACK_IN_THE_BOX.get().getDefaultInstance());
        return spawnGroupData;
    }

    /**Not only jack-in-a-box is acceptable, but the item should be in main hand.*/
    public static class JackInABoxZombieUseItemGoal extends Goal {
        private Mob mob;
        public JackInABoxZombieUseItemGoal(Mob mob) {
            this.mob = mob;
        }
        @Override
        public boolean canUse() {
            return mob.getTicksFrozen() <= 0 && ! mob.getMainHandItem().isEmpty() && mob.getMainHandItem().getUseDuration() > 0 &&
                    (! mob.isUsingItem() || mob.getUseItemRemainingTicks() > mob.getMainHandItem().getUseDuration() - 60) &&
                    mob.tickCount > 100 && (mob.isUsingItem() || mob.getRandom().nextFloat() < (mob.getTarget() != null && mob.getTarget().distanceToSqr(mob) < 25 ? 0.005F : 0.0001F));
        }

        public void start() {
            super.start();
            this.mob.startUsingItem(InteractionHand.MAIN_HAND);
        }

        public void stop() {
            super.stop();
            this.mob.releaseUsingItem();
        }

    }
}
