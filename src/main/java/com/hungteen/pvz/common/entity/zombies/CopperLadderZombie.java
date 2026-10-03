package com.hungteen.pvz.common.entity.zombies;

import com.hungteen.pvz.common.entity.ai.goal.CopperLadderBuildGoal;
import com.hungteen.pvz.common.register.PVZItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

import javax.annotation.Nullable;

/**
 * @author PangTeen
 * @program HTPVZ
 * @create 2026/10/3 15:00
 **/
public class CopperLadderZombie extends PVZZombie {

    private static final int DEFAULT_LADDER_COUNT = 64;
    private int ladderCount = DEFAULT_LADDER_COUNT;

    public CopperLadderZombie(EntityType<? extends Zombie> p_34271_, Level p_34272_) {
        super(p_34271_, p_34272_);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PVZZombie.createAttributes()
                .add(Attributes.MAX_HEALTH, 24.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    @Override
    protected void addBehaviourGoals() {
        super.addBehaviourGoals();
        this.goalSelector.addGoal(2, new CopperLadderBuildGoal(this));
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData data, @Nullable CompoundTag tag) {
        SpawnGroupData spawnGroupData = super.finalizeSpawn(level, difficulty, spawnType, data, tag);
        this.setLeftHanded(false);
        this.setItemInHand(InteractionHand.MAIN_HAND, PVZItems.COPPER_LADDER.get().getDefaultInstance());
        return spawnGroupData;
    }

    public boolean canBuildLadder() {
        return this.ladderCount > 0;
    }

    public boolean consumeLadder() {
        if (! this.canBuildLadder()) {
            return false;
        }
        -- this.ladderCount;
        return true;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("ladder_count", this.ladderCount);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("ladder_count")) {
            this.ladderCount = Math.max(0, tag.getInt("ladder_count"));
        }
    }
}
