package com.hungteen.pvz.common.entity.zombies;

import com.hungteen.pvz.common.block.CopperLadderBlock;
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
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

import javax.annotation.Nullable;

/**
 * @author PangTeen
 * @program HTPVZ
 * @create 2026/10/3 15:00
 **/
public class CopperLadderZombie extends PVZZombie {

    private static final int DEFAULT_LADDER_COUNT = 8;

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
        ItemStack ladders = PVZItems.COPPER_LADDER.get().getDefaultInstance();
        ladders.setCount(DEFAULT_LADDER_COUNT);
        this.setItemInHand(InteractionHand.MAIN_HAND, ladders);
        return spawnGroupData;
    }

    public boolean canBuildLadder() {
        return this.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof BlockItem b && b.getBlock() instanceof CopperLadderBlock;
    }

    public boolean consumeLadder() {
        if (! this.canBuildLadder()) {
            return false;
        }
        this.getItemInHand(InteractionHand.MAIN_HAND).shrink(1);
        return true;
    }
}
