package com.hungteen.pvz.common.entity.zombies;

import com.hungteen.pvz.common.item.JackInTheBoxItem;
import com.hungteen.pvz.common.register.PVZItems;
import com.hungteen.pvz.common.register.PVZSoundEvents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

import javax.annotation.Nullable;

public class JackInABoxZombie extends PVZZombie implements PowerableMob {

    private static final EntityDataAccessor<Boolean> IS_POWERED = SynchedEntityData.defineId(JackInABoxZombie.class, EntityDataSerializers.BOOLEAN);

    public JackInABoxZombie(EntityType<? extends Zombie> p_34271_, Level p_34272_) {
        super(p_34271_, p_34272_);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(IS_POWERED, false);
    }

    @Override
    public void thunderHit(ServerLevel p_32286_, LightningBolt p_32287_) {
        super.thunderHit(p_32286_, p_32287_);
        this.entityData.set(IS_POWERED, true);
        if (this.getItemInHand(InteractionHand.MAIN_HAND).is(PVZItems.JACK_IN_THE_BOX.get())) {
            this.setItemInHand(InteractionHand.MAIN_HAND, PVZItems.CHARGED_JACK_IN_THE_BOX.get().getDefaultInstance());
        }
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

    public static AttributeSupplier.Builder createAttributes() {
        return PVZZombie.createAttributes().add(Attributes.MOVEMENT_SPEED, 0.28F);
    }

    @Override
    public boolean isPowered() {
        return this.getEntityData().get(IS_POWERED);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag p_32304_) {
        super.addAdditionalSaveData(p_32304_);
        if (this.entityData.get(IS_POWERED)) {
            p_32304_.putBoolean("powered", true);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag p_32296_) {
        super.readAdditionalSaveData(p_32296_);
        this.entityData.set(IS_POWERED, p_32296_.getBoolean("powered"));
    }

    /**Not only jack-in-a-box is acceptable, but the item should be in main hand.*/
    public static class JackInABoxZombieUseItemGoal extends Goal {
        private final Mob mob;
        public JackInABoxZombieUseItemGoal(Mob mob) {
            this.mob = mob;
        }
        @Override
        public boolean canUse() {
            return mob.getTicksFrozen() <= 0 && ! mob.getMainHandItem().isEmpty() && mob.getMainHandItem().getUseDuration() > 0 &&
                    (! mob.isUsingItem() || mob.getUseItemRemainingTicks() > mob.getMainHandItem().getUseDuration() - 60) &&
                    mob.tickCount > 160 && (mob.isUsingItem() || mob.getRandom().nextFloat() < (mob.getTarget() != null && mob.getTarget().distanceToSqr(mob) < 25 ? 0.01F : 0.001F));
        }

        public void start() {
            super.start();
            if (this.mob instanceof JackInABoxZombie zombie && zombie.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof JackInTheBoxItem)
                this.mob.playSound(PVZSoundEvents.JACK_IN_A_BOX_ZOMBIE_SURPRISE.get());
            this.mob.startUsingItem(InteractionHand.MAIN_HAND);
        }

        public void stop() {
            super.stop();
            this.mob.releaseUsingItem();
        }

    }
}
