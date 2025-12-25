package com.hungteen.pvz.common.entity.zombies;

import com.hungteen.pvz.util.EntityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

import java.util.UUID;

public class LavaDiverZombie extends SnorkelZombie {
    static final UUID LAVA_ATTRIBUTE_MODIFIER = UUID.fromString("f0bdf6c1-8496-d83f-e83d-19c5ff4f17de");
    public LavaDiverZombie(EntityType<? extends Zombie> p_34271_, Level p_34272_) {
        super(p_34271_, p_34272_);
        this.setPathfindingMalus(BlockPathTypes.LAVA, 0.0F);
        this.setPathfindingMalus(BlockPathTypes.DANGER_FIRE, 0.0F);
        this.setPathfindingMalus(BlockPathTypes.DAMAGE_FIRE, 0.0F);
    }
    public static AttributeSupplier.Builder createAttributes() {
        return SnorkelZombie.createAttributes().add(Attributes.MAX_HEALTH, 40.0D);
    }
    public static boolean checkSpawnRules(EntityType<PVZZombie> p_218956_, ServerLevelAccessor p_218957_, MobSpawnType p_218958_, BlockPos p_218959_, RandomSource p_218960_) {
        if (! p_218957_.getFluidState(p_218959_.below()).is(FluidTags.LAVA)) {
            return false;
        } else {
            return p_218957_.getDifficulty() != Difficulty.PEACEFUL;
        }
    }
    public void tick() {
        if (! this.level.isClientSide) {
            if (this.isInLava()) {
                EntityUtil.addModifierToAttribute(this, Attributes.MOVEMENT_SPEED,
                        new AttributeModifier(LAVA_ATTRIBUTE_MODIFIER, "lava bonus", 5, AttributeModifier.Operation.MULTIPLY_BASE));
            } else {
                EntityUtil.removeModifierFromAttribute(this, Attributes.MOVEMENT_SPEED, LAVA_ATTRIBUTE_MODIFIER);
            }
        }
        super.tick();
    }

    public boolean doHurtTarget(Entity p_34276_) {
        boolean flag = super.doHurtTarget(p_34276_);
        if (flag) {
            float f = this.level.getCurrentDifficultyAt(this.blockPosition()).getEffectiveDifficulty();
            p_34276_.setSecondsOnFire(4 * (int)f);
        }

        return flag;
    }
    @Override
    boolean wantsToSwim() {
        return this.isInLava() ? this.getTarget() == null : super.wantsToSwim();
    }

    @Override
    public void travel(Vec3 p_32394_) {
        if (this.isInLava()) {
            CollisionContext collisioncontext = CollisionContext.of(this);
            if (! collisioncontext.isAbove(LiquidBlock.STABLE_SHAPE, this.blockPosition(), true) ||
                    this.level.getFluidState(this.blockPosition().above()).is(FluidTags.LAVA)) {
                this.setDeltaMovement(this.getDeltaMovement().scale(0.5D).add(0.0D, this.getPose() != Pose.SWIMMING ? 0.05D : -0.02D, 0.0D));
                if (this.getPose() == Pose.SWIMMING) {
                    this.moveRelative(-0.02F, p_32394_);
                }
            }
        }
        super.travel(p_32394_);
    }
}
